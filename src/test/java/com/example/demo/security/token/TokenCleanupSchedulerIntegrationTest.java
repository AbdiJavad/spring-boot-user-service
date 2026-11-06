package com.example.demo.security.token;

import com.example.demo.AbstractIntegrationTest;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TokenCleanupSchedulerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
        refreshTokenRepository.deleteAll();

        testUser = userRepository.findByEmail("scheduler-test@example.com")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Test User")
                        .email("scheduler-test@example.com")
                        .password("hashedPassword123")
                        .role(Role.ROLE_USER)
                        .build()));
    }

    @Test
    @DisplayName("cleanupExpiredTokens should delete only expired revoked and refresh tokens")
    void cleanupExpiredTokens_shouldDeleteOnlyExpiredTokens() {
        Instant now = Instant.now();

        RevokedToken expiredRevoked = new RevokedToken(
                "expired-jti",
                now.minus(2, ChronoUnit.HOURS),
                now.minus(3, ChronoUnit.HOURS)
        );

        RevokedToken activeRevoked = new RevokedToken(
                "active-jti",
                now.plus(2, ChronoUnit.HOURS),
                now.minus(1, ChronoUnit.HOURS)
        );

        revokedTokenRepository.save(expiredRevoked);
        revokedTokenRepository.save(activeRevoked);

        RefreshToken expiredRefresh = RefreshToken.builder()
                .tokenHash("expired-refresh-hash")
                .familyId("family-1")
                .revoked(true)
                .expiryDate(now.minus(1, ChronoUnit.DAYS))
                .user(testUser)
                .build();

        RefreshToken revokedButNotExpired = RefreshToken.builder()
                .tokenHash("revoked-not-expired-refresh-hash")
                .familyId("family-2")
                .revoked(true)
                .expiryDate(now.plus(5, ChronoUnit.DAYS))
                .user(testUser)
                .build();

        RefreshToken validRefresh = RefreshToken.builder()
                .tokenHash("valid-refresh-hash")
                .familyId("family-3")
                .revoked(false)
                .expiryDate(now.plus(7, ChronoUnit.DAYS))
                .user(testUser)
                .build();

        refreshTokenRepository.save(expiredRefresh);
        refreshTokenRepository.save(revokedButNotExpired);
        refreshTokenRepository.save(validRefresh);

        tokenCleanupScheduler.cleanupExpiredTokens();

        assertThat(revokedTokenRepository.existsByJti("expired-jti")).isFalse();
        assertThat(revokedTokenRepository.existsByJti("active-jti")).isTrue();

        assertThat(refreshTokenRepository.findByTokenHash("expired-refresh-hash")).isEmpty();
        assertThat(refreshTokenRepository.findByTokenHash("revoked-not-expired-refresh-hash")).isPresent();
        assertThat(refreshTokenRepository.findByTokenHash("valid-refresh-hash")).isPresent();
    }
}
