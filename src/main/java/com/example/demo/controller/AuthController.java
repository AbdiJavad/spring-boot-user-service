package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.TokenBlacklistService;
import com.example.demo.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserRepository userRepository;

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900; // 15 minutes

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        String accessToken = jwtService.generateToken(user);
        var refreshTokenResult = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(
                TokenResponse.of(accessToken, refreshTokenResult.rawToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var newRefreshTokenResult = refreshTokenService.rotate(request.refreshToken());
        User user = newRefreshTokenResult.refreshToken().getUser();
        String accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(
                TokenResponse.of(accessToken, newRefreshTokenResult.rawToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest servletRequest
    ) {
        // 1. باطل کردن Refresh Token در دیتابیس
        refreshTokenService.revoke(request.refreshToken());

        // 2. بلک‌لیست کردن Access Token جاری در Redis (Zero Trust Hardening)
        String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(accessToken);
        }

        return ResponseEntity.noContent().build();
    }
}
