package com.example.demo.dto;

public record AuthResponse(
        String token,
        String type,
        String refreshToken
) {
    public AuthResponse(String token) {
        this(token, "Bearer", null);
    }

    public static AuthResponse of(String accessToken, String refreshToken) {
        return new AuthResponse(accessToken, "Bearer", refreshToken);
    }
}
