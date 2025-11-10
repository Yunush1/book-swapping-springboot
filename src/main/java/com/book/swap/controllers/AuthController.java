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
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO request, HttpServletResponse httpServletResponse) throws Exception {
        AuthResponse authResponse = authService.login(request);
        cookieUtil.addRefreshTokenCookie(httpServletResponse, authResponse.getRefreshToken());
        return ResponseEntity.ok(Map.of("success", authResponse.isSuccess(), "accessToken", authResponse.getAccessToken(), "user", authResponse.getUser()));
    }

    @GetMapping("/token/validate/{token}")
    public ResponseEntity<TokenValidationResponse> validateAccessToken(@PathVariable("token") String token) throws Exception {
        return ResponseEntity.ok(tokenService.validateAccessToken(token));
    }

    @GetMapping("/token/refresh")
    public ResponseEntity<?> getNewAccessToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        AuthResponse authResponse = tokenService.refreshAccessToken(refreshToken);
        cookieUtil.addRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok(Map.of("success", authResponse.isSuccess(), "accessToken", authResponse.getAccessToken()));
    }

    @GetMapping("/token/logout")
    public ResponseEntity<?> logoutSession(HttpServletResponse response, HttpServletRequest request) throws Exception {
        String accessToken = request.getHeader("Authorization").substring(7);
        tokenService.invalidateSession(accessToken);
        cookieUtil.deleteRefreshTokenCookie(response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
