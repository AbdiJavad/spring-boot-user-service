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

        // Ã›Â±. Ã˜Â±ÃšÂ©Ã™Ë†Ã˜Â±Ã˜Â¯ Ã™â€¦Ã™â€ Ã™â€šÃ˜Â¶Ã›Å’ (Ã›Â² Ã˜Â³Ã˜Â§Ã˜Â¹Ã˜Âª Ã™Â¾Ã›Å’Ã˜Â´)
        RevokedToken expiredToken = new RevokedToken("expired-1", now.minus(2, ChronoUnit.HOURS), now.minus(3, ChronoUnit.HOURS));
        // Ã›Â². Ã˜Â±ÃšÂ©Ã™Ë†Ã˜Â±Ã˜Â¯ Ã™ÂÃ˜Â¹Ã˜Â§Ã™â€ž (Ã›Â² Ã˜Â³Ã˜Â§Ã˜Â¹Ã˜Âª Ã˜Â¢Ã›Å’Ã™â€ Ã˜Â¯Ã™â€¡)
        RevokedToken activeToken = new RevokedToken("active-1", now.plus(2, ChronoUnit.HOURS), now.minus(10, ChronoUnit.MINUTES));

        revokedTokenRepository.save(expiredToken);
        revokedTokenRepository.save(activeToken);
        revokedTokenRepository.flush(); // Ã˜Â§Ã˜Â·Ã™â€¦Ã›Å’Ã™â€ Ã˜Â§Ã™â€  Ã˜Â§Ã˜Â² Ã˜Â§Ã˜Â¹Ã™â€¦Ã˜Â§Ã™â€ž Ã˜Â¯Ã˜Â± Ã˜Â¯Ã›Å’Ã˜ÂªÃ˜Â§Ã˜Â¨Ã›Å’Ã˜Â³

        tokenCleanupScheduler.cleanupExpiredRevokedTokens();

        assertThat(revokedTokenRepository.count()).isEqualTo(1);
        assertThat(revokedTokenRepository.existsByJti("active-1")).isTrue();
        assertThat(revokedTokenRepository.existsByJti("expired-1")).isFalse();
    }
}
