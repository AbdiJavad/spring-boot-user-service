package com.example.demo.service;

import com.example.demo.dto.RefreshTokenResult;
import com.example.demo.exception.InvalidRefreshTokenException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.security.refresh-token-validity-seconds:604800}")
    private long refreshTokenValiditySeconds;

    @Transactional
    public RefreshTokenResult createRefreshToken(User user) {
        return createRefreshTokenWithFamily(user, null);
    }

    @Transactional
    public RefreshTokenResult createRefreshTokenWithFamily(User user, String familyId) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        String resolvedFamilyId = (familyId != null && !familyId.isBlank()) ? familyId : UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds))
                .revoked(false)
                .familyId(resolvedFamilyId)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        return new RefreshTokenResult(saved, rawToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }
        return refreshToken;
    }

    @Transactional
    public RefreshTokenResult rotate(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (oldToken.isRevoked()) {
            // Token Reuse / Theft Detection: Revoke entire token family!
            refreshTokenRepository.revokeTokenFamily(oldToken.getFamilyId());
            throw new InvalidRefreshTokenException("Token reuse detected! Entire token family has been revoked.");
        }

        if (oldToken.getExpiryDate().isBefore(Instant.now())) {
            oldToken.setRevoked(true);
            refreshTokenRepository.save(oldToken);
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        // Mark old token as revoked
        oldToken.setRevoked(true);

        // Generate new token in the same family
        String newRawToken = generateSecureToken();
        String newTokenHash = hashToken(newRawToken);
        oldToken.setReplacedByToken(newTokenHash);
        refreshTokenRepository.save(oldToken);

        RefreshToken newToken = RefreshToken.builder()
                .user(oldToken.getUser())
                .tokenHash(newTokenHash)
                .expiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds))
                .revoked(false)
                .familyId(oldToken.getFamilyId())
                .build();

        RefreshToken saved = refreshTokenRepository.save(newToken);
        return new RefreshTokenResult(saved, newRawToken);
    }

    @Transactional
    public void revoke(String rawToken) {
        String tokenHash = hashToken(rawToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
