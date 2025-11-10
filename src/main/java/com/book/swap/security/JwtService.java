package com.book.swap.security;

import com.book.swap.models.entities.DbUsers;
import com.book.swap.utils.books.CacheService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final CacheService cacheService;
    private final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private Key getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            System.err.println("Base64 decode failed, using UTF-8: " + e.getMessage());
            byte[] keyBytes = jwtProperties.secret.getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get("sessionId", String.class));
    }

    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", String.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate access token with user details, roles, and session
     */
    public String generateAccessToken(DbUsers user, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("sessionId", sessionId);
        claims.put("roles", user.getRoles());
        claims.put("type", "ACCESS");

        return buildToken(claims, user.getEmail(), jwtProperties.accessTokenExpiration);
    }

    /**
     * Generate refresh token with minimal claims
     */
    public String generateRefreshToken(DbUsers user, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("sessionId", sessionId);
        claims.put("type", "REFRESH");

        return buildToken(claims, user.getEmail(), jwtProperties.refreshTokenExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            String subject,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        logger.debug("Checking token for username {}, {}", username, userDetails.getUsername());
        boolean isValid  = isTokenExpired(token);
        logger.debug("Token expired for isValid {}", isValid);
        return (username.equals(userDetails.getUsername()) && !isValid && isValidSessionId(token));
    }

    public boolean isValidSessionId(String token) {
        final String sessionId = extractSessionId(token);
        DbUsers user = cacheService.getSession(sessionId);
        if (user == null) {
            logger.error("Invalid session id {}", sessionId);
            return false;
        }
        logger.debug("Session id {}, {}", sessionId, user.getSessionId());
        return Objects.equals(sessionId, user.getSessionId());
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getAccessTokenExpiration() {
        return jwtProperties.accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return jwtProperties.refreshTokenExpiration;
    }
}