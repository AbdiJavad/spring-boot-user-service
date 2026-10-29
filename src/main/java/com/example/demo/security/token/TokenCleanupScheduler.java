package com.example.demo.security.token;

import java.time.Duration;
import java.time.Instant;

import com.example.demo.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Cleanup expired access tokens (revoked_tokens) and expired refresh tokens (refresh_tokens).
     * Protected by ShedLock for Kubernetes/multi-replica production environments.
     */
    @Transactional
    @Scheduled(cron = "${app.security.token-cleanup-cron:0 0 3 * * *}")
    @SchedulerLock(
        name = "TokenCleanupScheduler_cleanupExpiredTokens",
        lockAtLeastFor = "15s",
        lockAtMostFor = "5m"
    )
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        Instant start = Instant.now();

        int deletedRevoked = revokedTokenRepository.deleteByExpiryAtBefore(now);
        int deletedRefresh = refreshTokenRepository.deleteByExpiryDateBefore(now);

        Duration took = Duration.between(start, Instant.now());

        if (deletedRevoked > 0 || deletedRefresh > 0) {
            log.info("Token cleanup completed in {} ms: deleted {} revoked access tokens, {} expired refresh tokens",
                    took.toMillis(), deletedRevoked, deletedRefresh);
        } else {
            log.debug("Token cleanup completed in {} ms: no expired tokens to delete", took.toMillis());
        }
    }
}
