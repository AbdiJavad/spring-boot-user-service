package com.example.demo.dto;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {

    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
