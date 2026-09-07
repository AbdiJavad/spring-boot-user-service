package com.example.demo.service;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User; // User را import کنید
import com.example.demo.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    // private final UserRepository userRepository; // فعلاً این را کامنت می‌کنیم چون User هنوز کامل نیست

    /**
     * ایجاد یک Refresh Token جدید برای یک کاربر مشخص
     * @param user آبجکت User که توکن برای او ایجاد می‌شود
     * @return آبجکت RefreshToken ساخته شده
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) { // پارامتر ورودی را به User تغییر دادیم
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null for creating a refresh token.");
        }

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user) // حالا User را به توکن اختصاص می‌دهیم
                .expiryDate(Instant.now().plusSeconds(604800)) // ۷ روز اعتبار (604800 ثانیه)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * پیدا کردن توکن بر اساس رشته توکن
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * حذف توکن (مثلاً در هنگام Logout)
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }
}
