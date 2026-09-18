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
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(generateSecureToken())
                .expiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
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

    /**
     * Rotates a refresh token according to OAuth 2.0 Security BCP (RFC 9700, sect. 4.14.2):
     * the old token row is preserved and marked as revoked (enabling reuse/theft detection),
     * and a brand-new token row is issued for the same user.
     */
    @Transactional
    public RefreshToken rotate(String token) {
        // 1. Look up the current (non-revoked) token together with its user
        RefreshToken oldToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));

        // 2. Validate current state
        if (oldToken.isRevoked()) {
            // Reuse of a revoked token indicates possible theft; revoke the whole family later.
            throw new IllegalArgumentException("Refresh token has been revoked (possible reuse detected)");
        }
        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired");
        }

        // 3. Revoke the old token row (kept in DB for reuse detection / audit)
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // 4. Issue a brand-new token row for the same user
        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .token(generateSecureToken())
                .expiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(newToken);
    }

    @Transactional
    public void revoke(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
