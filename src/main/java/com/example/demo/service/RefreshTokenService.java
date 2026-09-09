package com.example.demo.service;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.security.refresh-token-validity-seconds:604800}")
    private long refreshTokenValiditySeconds;

    /**
     * یک توکن جدید با مقدار ۲۵۶ بیتی (base64url) می‌سازد
     * و رابط آن را با کاربر ثبت می‌کند.
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(generateSecureToken())
                .expiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds))
                .revoked(false)
                .user(user)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * بررسی می‌کند که توکن:
     *  ۱) در پایگاه‌داده وجود داشته باشد
     *  ۲) هنوز باطل نشده (revoked = false)
     *  ۳) منقضی نشده باشد
     *  در صورت موفقی، Reference Object را برمی‌گرداند.
     */
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
     * Rotation: توکن ورودی باطل می‌شود (logical delete)
     * و یک توکن جدید پشتیبانی‌شده برای همان کاربر ساخته قرار می‌گیرد.
     */
    @Transactional
    public RefreshToken rotate(String token) {
        RefreshToken oldToken = validateRefreshToken(token);
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        return createRefreshToken(oldToken.getUser());
    }

    /**
     * Logout: توکن باطل می‌شود و از گردش خارج می‌شود.
     */
    @Transactional
    public void revoke(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * تولید یک توکن random با طول ۳۲ بایت → ۴۳ کاراکتر base64url.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        var secureRandom = new java.security.SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
