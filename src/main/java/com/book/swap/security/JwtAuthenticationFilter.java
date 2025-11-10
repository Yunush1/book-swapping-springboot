package com.book.swap.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String path = request.getRequestURI();
        log.debug("➡️ Incoming request path: {}", path);

        // 1️⃣ Skip JWT validation for auth-related endpoints
        if (isAuthEndpoint(path)) {
            log.trace("⏩ Skipping JWT validation for auth endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("🚫 Missing or invalid Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            final String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    setAuthentication(userDetails, request);
                    log.debug("✅ User authenticated successfully: {}", username);
                } else {
                    sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }
            }

        } catch (ExpiredJwtException e) {
            log.warn("⏰ Token expired: {}", e.getMessage());
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
            return;

        } catch (SignatureException e) {
            log.warn("❌ Invalid JWT signature: {}", e.getMessage());
            sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid token signature");
            return;

        } catch (MalformedJwtException e) {
            log.warn("⚠️ Malformed JWT: {}", e.getMessage());
            sendJsonError(response, HttpServletResponse.SC_BAD_REQUEST, "Malformed token");
            return;

        } catch (Exception e) {
            log.error("🔥 Unexpected JWT error: {}", e.getMessage(), e);
            sendJsonError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal authentication error");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Utility to check if the request path should skip JWT validation.
     */
    private boolean isAuthEndpoint(String path) {
        return path.contains("/auth/") || path.startsWith("/api/auth/") || path.equals("/api/") ||path.contains("/actuator");
    }

    /**
     * Sets the authenticated user in the security context.
     */
    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    /**
     * Sends a standardized JSON error response.
     */
    private void sendJsonError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                String.format("{\"status\":false,\"error\":\"%s\"}", message)
        );
    }
}
