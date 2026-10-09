package com.example.demo.ratelimit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class RateLimitService {

    private final Cache<String, Bucket> cache;

    public RateLimitService() {
        this.cache = Caffeine.newBuilder()
                .expireAfterAccess(Duration.ofMinutes(10))
                .maximumSize(1000)
                .build();
    }

    public Bucket resolveBucket(String key) {
        return cache.get(key, this::createNewBucket);
    }

    private Bucket createNewBucket(String key) {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))))
                .build();
    }
}
