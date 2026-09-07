package com.example.demo.repository;

import com.example.demo.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository; // اصلاح شد
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // پیدا کردن توکن برای تمدید (Refresh)
    Optional<RefreshToken> findByToken(String token);

    // پیدا کردن تمام توکن‌های یک کاربر برای ابطال در زمان لاگ‌اوت یا تغییر پسورد
    void deleteByUserUserId(Long userId);
}
