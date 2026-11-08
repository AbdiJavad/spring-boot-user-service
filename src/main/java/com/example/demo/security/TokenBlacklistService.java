package com.example.demo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    public TokenBlacklistService(StringRedisTemplate redisTemplate, JwtService jwtService) {
        this.redisTemplate = redisTemplate;
        this.jwtService = jwtService;
    }

    public void blacklistToken(String token) {
        try {
            String jti = jwtService.extractJti(token);
            if (jti == null || jti.isBlank()) {
                log.warn("Cannot blacklist token: missing jti");
                return;
            }

            Date expiration = jwtService.extractExpiration(token);
            long ttlMillis = expiration.getTime() - System.currentTimeMillis();

            if (ttlMillis > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + jti,
                        "revoked",
                        ttlMillis,
                        TimeUnit.MILLISECONDS
                );
                log.info("Blacklisted token with jti: {} for {} ms", jti, ttlMillis);
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token", e);
        }
    }

    public boolean isBlacklisted(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        try {
            Boolean hasKey = redisTemplate.hasKey(BLACKLIST_PREFIX + jti);
            return Boolean.TRUE.equals(hasKey);
        } catch (Exception e) {
            log.error("Redis connection failure while checking blacklist for jti: {}", jti, e);
            // Fail-open or fail-closed based on security policy; usually fail-open to avoid breaking auth on redis blip
            return false;
        }
    }
}
