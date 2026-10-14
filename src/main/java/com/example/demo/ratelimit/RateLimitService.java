package com.example.demo.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> cache;
    private final long capacity;
    private final Duration refillDuration;

    public RateLimitService(
            @Value("${rate-limiter.capacity:10}") long capacity,
            @Value("${rate-limiter.duration-minutes:1}") long durationMinutes) {
        this.capacity = capacity;
        this.refillDuration = Duration.ofMinutes(durationMinutes);
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(10_000)
                .build();
    }

    public Bucket resolveBucket(String key) {
        return cache.get(key, this::createNewBucket);
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillDuration)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
