package com.example.demo.dto;

public record TokenResponse(
        String token,         // این همان accessToken است که تست دنبالش می‌گردد
        String refreshToken,
        long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn);
    }
}
