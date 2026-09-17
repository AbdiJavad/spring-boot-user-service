package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
        @JsonProperty("accessToken")
        String accessToken,

        @JsonProperty("token")
        String token,

        @JsonProperty("type")
        String type,

        @JsonProperty("refreshToken")
        String refreshToken
) {
    public static AuthResponse of(String accessToken, String refreshToken) {
        return new AuthResponse(accessToken, accessToken, "Bearer", refreshToken);
    }
}