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

    @Value("${app.security.refresh-token-validity-seconds:604800}") // پیش‌فرض ۷ روز
    private long refreshTokenValiditySeconds;

    /**
     * ایجاد یا جایگزینی Refresh Token برای کاربر (با توجه به OneToOne بودن رابطه)
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElseGet(() -> RefreshToken.builder().user(user).build());

        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * اعتبارسنجی توکن
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
     * چرخش توکن (Rotation): اعتبارسنجی و تولید مقدار جدید
     */
    @Transactional
    public RefreshToken rotate(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);

        // تولید توکن امن جدید و تمدید انقضا روی همان موجودیت
        refreshToken.setToken(generateSecureToken());
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenValiditySeconds));
        refreshToken.setRevoked(false);

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * ابطال توکن هنگام Logout
     */
    @Transactional
    public void revoke(String token) {
        RefreshToken refreshToken = validateRefreshToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * تولید ۲۵۶ بیت رشته تصادفی امن URL-safe
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
