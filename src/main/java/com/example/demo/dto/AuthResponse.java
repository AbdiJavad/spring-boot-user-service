package com.example.demo.dto;

public record AuthResponse(
        String token,
        String type
) {
    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}
