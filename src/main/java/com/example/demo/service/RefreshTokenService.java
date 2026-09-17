package com.example.demo.service;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.refresh-token-validity-seconds:604800}")
    private long refreshTokenValiditySeconds;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder().user(user).build());

        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        // Ø¨Ø±Ø§ÛŒ Ø§Ø¹ØªØ¨Ø§Ø± Ø³Ù†Ø¬ÛŒ Ø³Ø§Ø¯Ù‡ Ø§Ø² Ù…ØªØ¯ Ù‚Ø¯ÛŒÙ…ÛŒ Ø§Ø³ØªÙØ§Ø¯Ù‡ Ù…ÛŒâ€ŒÚ©Ù†ÛŒÙ…
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }
        return refreshToken;
    }

    @Transactional
    public RefreshToken rotate(String token) {
        // Û±. ÙˆØ§Ú©Ø´ÛŒ Ø¨Ø§ Ø§Ø³ØªÙØ§Ø¯Ù‡ Ø§Ø² Ù…ØªØ¯ Ø¨Ù‡ÛŒÙ†Ù‡â€ŒØ´Ø¯Ù‡ (Ù‡Ù…Ø±Ø§Ù‡ Ø¨Ø§ User)
        RefreshToken refreshToken = refreshTokenRepository.findByTokenWithUser(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Û². Ø¨Ø±Ø±Ø³ÛŒ ÙˆØ¶Ø¹ÛŒØª ÙØ¹Ù„ÛŒ
        if (refreshToken.isRevoked()) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // Û³. Ú†Ø±Ø®Ø´ ØªÙˆÚ©Ù† (ØªÙˆÙ„ÛŒØ¯ Ù…Ù‚Ø¯Ø§Ø± Ø¬Ø¯ÛŒØ¯ Ùˆ ØªÙ…Ø¯ÛŒØ¯ Ø§Ù†Ù‚Ø¶Ø§)
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds));

        // Ø°Ø®ÛŒØ±Ù‡ Ø¯Ø± Ø¯ÛŒØªØ§Ø¨ÛŒØ³
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revoke(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }


    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

}
