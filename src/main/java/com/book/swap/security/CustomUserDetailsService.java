package com.book.swap.security;

import com.book.swap.models.entities.DbUsers;
import com.book.swap.repository.UserRepository;
import com.book.swap.utils.books.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;
    private final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DbUsers user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<GrantedAuthority> authorities = getAuthorities(user);

        logger.debug("Loaded user: {} with roles {}", username,
                authorities.stream().map(GrantedAuthority::getAuthority).toList());

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!user.isEnabled())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }

    /**
     * Maps numeric role IDs from DbUsers to Spring Security ROLE_* authorities.
     */
    private Set<GrantedAuthority> getAuthorities(DbUsers user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            logger.warn("User {} has no roles assigned, defaulting to ROLE_GUEST", user.getEmail());
            return Set.of(new SimpleGrantedAuthority("ROLE_GUEST"));
        }

        return user.getRoles().stream()
                .map(this::mapRoleCodeToAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    /**
     * Helper to map numeric role codes to ROLE_* constants.
     */
    private String mapRoleCodeToAuthority(int code) {
        return switch (code) {
            case Constants.ROLE.ADMIN -> "ROLE_ADMIN";
            case Constants.ROLE.MODERATOR -> "ROLE_MODERATOR";
            case Constants.ROLE.USER -> "ROLE_USER";
            default -> "ROLE_GUEST";
        };
    }

    // --------------------------------------------------------------------------------------------
    // JWT Parsing Utilities (optional - only if you plan to extract roles from a JWT directly)
    // --------------------------------------------------------------------------------------------

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.warn("Base64 decode failed, falling back to UTF-8 key: {}", e.getMessage());
            byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        List<Integer> roleIds = claims.get("roles", List.class);

        if (roleIds == null || roleIds.isEmpty()) {
            logger.warn("No roles found in JWT");
            return List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
        }

        return roleIds.stream()
                .map(this::mapRoleCodeToAuthority)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }
}
