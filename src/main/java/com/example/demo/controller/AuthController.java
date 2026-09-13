package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final long ACCESS_TOKEN_EXPIRES_IN_SECONDS = 3600L;

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // 1. احراز هویت کاربر در سیکیوریتی
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. دریافت مدل اصلی User از دیتابیس بدون ریسک Cast شدن
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        // 3. صدور جفت توکن Access و Refresh
        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        // 4. بازگرداندن پاسخ
        return ResponseEntity.ok(AuthResponse.of(accessToken, refreshToken.getToken()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        // 1. چرخه Token Rotation (ابطال توکن قدیمی و ایجاد توکن جدید)
        var newRefreshToken = refreshTokenService.rotate(request.refreshToken());

        // 2. استخراج مستقیم User بدون فراخوانی مجدد دیتابیس (با استفاده از JOIN FETCH در Repository)
        User user = newRefreshToken.getUser();

        // 3. صدور Access Token جدید
        String accessToken = jwtService.generateToken(user);

        // 4. بازگرداندن TokenResponse همراه با طول عمر 3600 ثانیه‌ای توکن
        return ResponseEntity.ok(TokenResponse.of(accessToken, newRefreshToken.getToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        // 1. ابطال توکن Refresh در دیتابیس (Revoke)
        refreshTokenService.revoke(request.refreshToken());

        // 2. بازگرداندن 204 No Content طبق استاندارد RESTful
        return ResponseEntity.noContent().build();
    }
}
