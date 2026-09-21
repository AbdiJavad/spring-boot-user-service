package com.example.demo.dto;

public record TokenResponse(
        String accessToken,         // Ã˜Â§Ã›Å’Ã™â€  Ã™â€¡Ã™â€¦Ã˜Â§Ã™â€  accessToken Ã˜Â§Ã˜Â³Ã˜Âª ÃšÂ©Ã™â€¡ Ã˜ÂªÃ˜Â³Ã˜Âª Ã˜Â¯Ã™â€ Ã˜Â¨Ã˜Â§Ã™â€žÃ˜Â´ Ã™â€¦Ã›Å’Ã¢â‚¬Å’ÃšÂ¯Ã˜Â±Ã˜Â¯Ã˜Â¯
        String refreshToken,
        long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn);
    }
}
