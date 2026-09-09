package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;   // ⬅️ این سطر را اضافه کن
import com.example.demo.dto.TokenResponse;
import com.example.demo.model.User;
import com.example.demo.security.JwtService;
import com.example.demo.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
            var userDetails = userDetailsService.loadUserByUsername(request.email());
            var user = (User) userDetails;
            var accessToken = jwtService.generateToken(user);
            var refreshToken = refreshTokenService.createRefreshToken(user);
            return ResponseEntity.ok(AuthResponse.of(accessToken, refreshToken.getToken()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Ungültige E-Mail oder Passwort");
        } catch (Exception e) {
            throw new IllegalStateException("Unerwarteter Fehler beim Login", e);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        var newRefreshToken = refreshTokenService.rotate(request.refreshToken());
        var user = newRefreshToken.getUser();
        var accessToken = jwtService.generateToken(user);
        return ResponseEntity.ok(TokenResponse.of(accessToken, newRefreshToken.getToken(), 3600L));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.revoke(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}
