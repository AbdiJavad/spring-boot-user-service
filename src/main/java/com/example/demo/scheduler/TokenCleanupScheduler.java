package com.example.demo.scheduler;

import com.example.demo.repository.RefreshTokenRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Component
public class TokenCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    public TokenCleanupScheduler(RefreshTokenRepository refreshTokenRepository, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.clock = clock;
    }

    /**
     * Cleans up expired tokens daily at 03:00 AM UTC.
     * Preserves revoked tokens until their natural expiration to support Token Reuse Detection.
     * Protected by ShedLock to ensure single execution across multiple service replicas.
     */
    @Scheduled(cron = "${app.security.token-cleanup-cron:0 0 3 * * *}")
    @SchedulerLock(
            name = "TokenCleanupScheduler_cleanupExpiredTokens",
            lockAtMostFor = "15m",
            lockAtLeastFor = "30s"
    )
    @Transactional
    public void cleanupExpiredTokens() {
        Instant now = clock.instant();
        log.info("Starting scheduled token cleanup for expired tokens before: {}", now);

        int deletedCount = refreshTokenRepository.deleteByExpiryDateBefore(now);

        log.info("Finished token cleanup. Removed {} expired refresh tokens.", deletedCount);
    }
}
