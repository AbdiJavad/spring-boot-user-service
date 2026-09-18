package com.example.demo.security.token;

import java.time.Duration;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    /**
     * Cleanup expired revoked tokens to prevent table and index bloat.
     * Protected by ShedLock for Kubernetes/multi-replica production environments.
     */
    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    @SchedulerLock(
        name = "TokenCleanupScheduler_cleanupExpiredRevokedTokens",
        lockAtLeastFor = "15s",
        lockAtMostFor = "5m"
    )
    public void cleanupExpiredRevokedTokens() {
        Instant start = Instant.now();
        long deleted = revokedTokenRepository.deleteByExpiryAtBefore(start);
        Duration took = Duration.between(start, Instant.now());

        if (deleted > 0) {
            log.info("Token cleanup: deleted {} expired revoked tokens in {} ms", deleted, took.toMillis());
        } else {
            log.debug("Token cleanup: no expired revoked tokens to delete ({} ms)", took.toMillis());
        }
    }
}