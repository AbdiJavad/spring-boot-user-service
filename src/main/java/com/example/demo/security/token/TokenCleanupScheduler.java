package com.example.demo.security.token;

import java.time.Duration;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Cleanup expired revoked tokens to prevent table/index bloat.
     *
     * Runs daily at 03:00 server time.
     * If you deploy in multiple instances, consider adding a distributed lock (e.g., ShedLock).
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupExpiredRevokedTokens() {
        Instant start = Instant.now();
        Instant now = start;

        long deleted = revokedTokenRepository.deleteByExpiryAtBefore(now);

        Duration took = Duration.between(start, Instant.now());
        if (deleted > 0) {
            log.info("Token cleanup: deleted {} expired revoked tokens in {} ms", deleted, took.toMillis());
        } else {
            log.debug("Token cleanup: no expired revoked tokens to delete ({} ms)", took.toMillis());
        }
    }
}
