package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.model.User;
import com.example.demo.security.JwtService;
import com.example.demo.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. احراز هویت کاربر
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. بارگذاری اطلاعات کاربر از دیتابیس
        var userDetails = userDetailsService.loadUserByUsername(request.email());
        var user = (User) userDetails;

        // 3. تولید توکن‌های جدید
        var accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        // 4. بازگرداندن پاسخ حاوی هر دو توکن
        return ResponseEntity.ok(AuthResponse.of(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // چرخه Refresh Token (Rotation):
        // توکن قدیمی باطل شده و یک جفت توکن جدید (Access + Refresh) صادر می‌شود.
        var newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        var user = newRefreshToken.getUser();
        var accessToken = jwtService.generateToken(user);

        // فرض بر این است که TokenResponse.of(access, refresh, expiry) تعریف شده است
        return ResponseEntity.ok(TokenResponse.of(accessToken, newRefreshToken.getToken(), 3600L));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        // ابطال رفرش توکن در دیتابیس برای جلوگیری از سوءاستفاده
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
