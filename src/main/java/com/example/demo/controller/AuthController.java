package com.example.demo.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + request.email()));

        String accessToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user);

        return ResponseEntity.ok(
                TokenResponse.of(accessToken, refreshToken.getToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        User user = newRefreshToken.getUser();
        String accessToken = jwtService.generateToken(user);

        return ResponseEntity.ok(
                TokenResponse.of(accessToken, newRefreshToken.getToken(), ACCESS_TOKEN_EXPIRES_IN_SECONDS)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
