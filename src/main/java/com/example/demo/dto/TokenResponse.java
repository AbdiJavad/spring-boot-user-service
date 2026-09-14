package com.example.demo.dto;

public record TokenResponse(
        String accessToken,         // Ø§ÛŒÙ† Ù‡Ù…Ø§Ù† accessToken Ø§Ø³Øª Ú©Ù‡ ØªØ³Øª Ø¯Ù†Ø¨Ø§Ù„Ø´ Ù…ÛŒâ€ŒÚ¯Ø±Ø¯Ø¯
        String refreshToken,
        long expiresIn
) {
    public static TokenResponse of(String accessToken, String refreshToken, long expiresIn) {
        return new TokenResponse(accessToken, refreshToken, expiresIn);
    }
}
