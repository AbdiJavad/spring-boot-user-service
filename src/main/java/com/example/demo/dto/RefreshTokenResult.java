package com.example.demo.dto;

import com.example.demo.model.RefreshToken;

public record RefreshTokenResult(
        RefreshToken refreshToken,
        String rawToken
) {}
