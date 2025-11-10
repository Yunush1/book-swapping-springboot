package com.book.swap.services.auth;

import com.book.swap.models.dto.AuthResponse;
import com.book.swap.models.dto.TokenValidationResponse;
import com.book.swap.models.dto.UserDTO;
import com.book.swap.models.entities.DbUsers;
import com.book.swap.repository.UserRepository;
import com.book.swap.security.JwtService;
import com.book.swap.services.exceptions.InvalidTokenException;
import com.book.swap.services.exceptions.NotFoundException;
import com.book.swap.utils.books.CacheService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CacheService cacheService;
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    /**
     * Refresh access token using refresh token
     * @param refreshToken The refresh token
     * @return New AuthResponse with new access token
     * @throws InvalidTokenException if token is invalid or expired
     * @throws NotFoundException     if user not found
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshAccessToken(String refreshToken) throws Exception {
        logger.info("Attempting to refresh access token");

        // Validate refresh token format
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.error("Refresh token is null or empty");
            throw new InvalidTokenException("Refresh token cannot be empty");
        }

        try {
            // Extract claims from refresh token
            String userEmail = jwtService.extractUsername(refreshToken);
            String  sessionId = jwtService.extractSessionId(refreshToken);
            logger.info("Extracted user email: {} from refresh token", userEmail);

            // Verify token type
            String tokenType = jwtService.extractClaim(refreshToken, claims ->
                    claims.get("type", String.class));

            if (!"REFRESH".equals(tokenType)) {
                logger.error("Invalid token type. Expected REFRESH, got: {}", tokenType);
                throw new InvalidTokenException("Invalid token type. Only refresh tokens are allowed");
            }

            // Check if token is expired
            if (jwtService.isTokenExpired(refreshToken)) {
                logger.error("Refresh token has expired for user: {}", userEmail);
                throw new InvalidTokenException("Refresh token has expired. Please login again");
            }

            // Fetch user from database
            DbUsers user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> {
                        logger.error("User not found with email: {}", userEmail);
                        return new NotFoundException("User not found: " + userEmail);
                    });

            // Verify user account status
            if (!user.isEnabled()) {
                logger.error("User account is disabled: {}", userEmail);
                throw new InvalidTokenException("User account is disabled");
            }
            logger.info("This is session id {}", sessionId);
            // Validate session from cache
            DbUsers cachedUser = cacheService.getSession(sessionId);

            if (cachedUser == null) {
                logger.error("Session not found in cache for sessionId: {}", sessionId);
                throw new InvalidTokenException("Invalid session. Please login again");
            }

            // Verify session belongs to the correct user
            if (!cachedUser.getId().equals(user.getId())) {
                logger.error("Session user mismatch. Expected: {}, Got: {}",
                        user.getId(), cachedUser.getId());
                throw new InvalidTokenException("Invalid session");
            }
            logger.info("Refresh token has expired for user: {}", userEmail);
            // Generate new access token with same session
            String newAccessToken = jwtService.generateAccessToken(user, sessionId);

            logger.info("Access token refreshed successfully for user: {}", userEmail);

            // Return new tokens
            return AuthResponse.builder()
                    .success(true)
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken) // Return same refresh token
                    .user(convertToUserDTO(user))
                    .build();

        } catch (InvalidTokenException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error refreshing token: {}", e.getMessage(), e);
            throw new InvalidTokenException("Failed to refresh token: " + e.getMessage());
        }
    }

    /**
     * Validate access token and return token information
     *
     * @param accessToken The access token to validate
     * @return TokenValidationResponse with validation status and user info
     */
    @Transactional(readOnly = true)
    public TokenValidationResponse validateAccessToken(String accessToken) {
        logger.info("Validating access token");

        try {
            // Extract claims
            String userEmail = jwtService.extractUsername(accessToken);
            String tokenType = jwtService.extractClaim(accessToken, claims ->
                    claims.get("type", String.class));
            Date expiration = jwtService.extractExpiration(accessToken);
            String userId = jwtService.extractUserId(accessToken);
            String sessionId = jwtService.extractSessionId(accessToken);

            // Verify token type
            if (!"ACCESS".equals(tokenType)) {
                logger.warn("Invalid token type for validation. Expected ACCESS, got: {}", tokenType);
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid token type")
                        .build();
            }

            // Check expiration
            if (jwtService.isTokenExpired(accessToken)) {
                logger.warn("Access token has expired for user: {}", userEmail);
                return TokenValidationResponse.builder()
                        .valid(false)
                        .expired(true)
                        .message("Token has expired")
                        .email(userEmail)
                        .build();
            }

            // Verify user exists
            DbUsers user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new NotFoundException("User not found: " + userEmail));

            // Verify user is enabled
            if (!user.isEnabled()) {
                logger.warn("User account is disabled: {}", userEmail);
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("User account is disabled")
                        .email(userEmail)
                        .build();
            }

            // Validate session
            DbUsers cachedUser = cacheService.getSession(sessionId);

            if (cachedUser == null || !cachedUser.getId().equals(user.getId())) {
                logger.warn("Invalid session for user: {}", userEmail);
                return TokenValidationResponse.builder()
                        .valid(false)
                        .message("Invalid session")
                        .email(userEmail)
                        .build();
            }

            logger.info("Access token validated successfully for user: {}", userEmail);

            return TokenValidationResponse.builder()
                    .valid(true)
                    .expired(false)
                    .email(userEmail)
                    .expiresAt(expiration)
                    .message("Token is valid")
                    .build();
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            return TokenValidationResponse.builder()
                    .valid(false)
                    .message("Token validation failed: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Invalidate session (logout)
     *
     * @param accessToken The access token
     */
    @Transactional
    public void invalidateSession(String accessToken) {
        try {
            String sessionId = jwtService.extractSessionId(accessToken);

            cacheService.invalidateSession(sessionId);
            logger.info("Session invalidated successfully: {}", sessionId);

        } catch (Exception e) {
            logger.error("Error invalidating session: {}", e.getMessage());
            throw new RuntimeException("Failed to invalidate session", e);
        }
    }

    private UserDTO convertToUserDTO(DbUsers user) {
        return com.book.swap.models.dto.UserDTO.builder()
                .email(user.getEmail())
                .role(user.getRoles().iterator().next())
                .fullName(user.getFullName())
                .userName(user.getUsername())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}