package com.example.demo.security.token;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DemoApplication.class)
@Transactional
class TokenCleanupSchedulerIntegrationTest {

    @Autowired
    private TokenCleanupScheduler tokenCleanupScheduler;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @BeforeEach
    void setUp() {
        revokedTokenRepository.deleteAll();
    }

    @Test
    @DisplayName("cleanupExpiredRevokedTokens should delete only expired tokens")
    void cleanupExpiredRevokedTokens_shouldDeleteOnlyExpiredTokens() {
        Instant now = Instant.now();

        // Û±. Ø±Ú©ÙˆØ±Ø¯ Ù…Ù†Ù‚Ø¶ÛŒ (Û² Ø³Ø§Ø¹Øª Ù¾ÛŒØ´)
        RevokedToken expiredToken = new RevokedToken("expired-1", now.minus(2, ChronoUnit.HOURS), now.minus(3, ChronoUnit.HOURS));
        // Û². Ø±Ú©ÙˆØ±Ø¯ ÙØ¹Ø§Ù„ (Û² Ø³Ø§Ø¹Øª Ø¢ÛŒÙ†Ø¯Ù‡)
        RevokedToken activeToken = new RevokedToken("active-1", now.plus(2, ChronoUnit.HOURS), now.minus(10, ChronoUnit.MINUTES));

        revokedTokenRepository.save(expiredToken);
        revokedTokenRepository.save(activeToken);
        revokedTokenRepository.flush(); // Ø§Ø·Ù…ÛŒÙ†Ø§Ù† Ø§Ø² Ø§Ø¹Ù…Ø§Ù„ Ø¯Ø± Ø¯ÛŒØªØ§Ø¨ÛŒØ³

        tokenCleanupScheduler.cleanupExpiredRevokedTokens();

        assertThat(revokedTokenRepository.count()).isEqualTo(1);
        assertThat(revokedTokenRepository.existsByJti("active-1")).isTrue();
        assertThat(revokedTokenRepository.existsByJti("expired-1")).isFalse();
    }
}
