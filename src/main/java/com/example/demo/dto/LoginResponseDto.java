package com.example.demo.dto;

public record LoginResponseDto(
        String token,
        String tokenType,
        long expiresIn
) {
    public static LoginResponseDto of(String token, long expiresIn) {
        return new LoginResponseDto(token, "Bearer", expiresIn);
    }
}
