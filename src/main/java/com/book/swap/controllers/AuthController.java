package com.book.swap.controllers;

import com.book.swap.models.dto.*;
import com.book.swap.models.entities.DbUsers;
import com.book.swap.security.CookieUtil;
import com.book.swap.security.JwtService;
import com.book.swap.services.UserService;
import com.book.swap.services.auth.AuthService;
import com.book.swap.services.auth.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(authService.register(userDTO));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginDTO request,
            HttpServletResponse httpServletResponse,
            @RequestHeader(value = "Client-Type", required = false) String clientType) throws Exception {
        AuthResponse authResponse = authService.login(request);
        if ("WEB".equalsIgnoreCase(clientType)) {
            cookieUtil.addRefreshTokenCookie(httpServletResponse, authResponse.getRefreshToken());
            authResponse.setRefreshToken(null);
            return ResponseEntity.ok(authResponse);
        }
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/token/validate")
    public ResponseEntity<TokenValidationResponse> validateAccessToken(HttpServletRequest request) throws Exception {
        String token = request.getHeader("Authorization").split(" ")[1];
        TokenValidationResponse tokenValidationResponse = tokenService.validateAccessToken(token);
        return ResponseEntity.status(tokenValidationResponse.isValid() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED).body(tokenValidationResponse);
    }

    @GetMapping("/token/refresh")
    public ResponseEntity<?> refreshAccessToken(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Client-Type", required = false, defaultValue = "APP") String clientType,
            @RequestParam(value = "refreshToken", required = false) String refreshTokenParam
    ) {
        try {
            String refreshToken = resolveRefreshToken(request, clientType, refreshTokenParam);

            AuthResponse authResponse = tokenService.refreshAccessToken(refreshToken);

            if (isWebClient(clientType)) {
                cookieUtil.addRefreshTokenCookie(response, authResponse.getRefreshToken());
                authResponse.setRefreshToken(null);
            }

            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.builder().success(false).message(e.getMessage()).build());
        }
    }


    @GetMapping("/token/logout")
    public ResponseEntity<?> logoutSession(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String accessToken = request.getHeader("Authorization").substring(7);
        tokenService.invalidateSession(accessToken);
        cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    /*
     * Helper function
     * */
    private String resolveRefreshToken(HttpServletRequest request, String clientType, String refreshTokenParam) {
        if (isWebClient(clientType)) {
            return cookieUtil.getRefreshTokenFromCookie(request)
                    .orElseThrow(() -> new IllegalArgumentException("Refresh token cookie not found"));
        }

        String token = (refreshTokenParam != null && !refreshTokenParam.isBlank())
                ? refreshTokenParam
                : request.getHeader("refreshToken");

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Refresh token missing from mobile request");
        }

        return token;
    }

    private boolean isWebClient(String clientType) {
        return "WEB".equalsIgnoreCase(clientType);
    }

}

