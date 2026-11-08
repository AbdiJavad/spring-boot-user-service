package com.example.demo.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenBlacklistService Unit Tests")
class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private JwtService jwtService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("should blacklist token successfully when jti and expiration are valid")
    void shouldBlacklistTokenSuccessfully() {
        String token = "dummy.jwt.token";
        String jti = "test-jti-123";
        Date futureExpiry = new Date(System.currentTimeMillis() + 60000); // 1 minute in future

        when(jwtService.extractJti(token)).thenReturn(jti);
        when(jwtService.extractExpiration(token)).thenReturn(futureExpiry);

        tokenBlacklistService.blacklistToken(token);

        verify(valueOperations, times(1)).set(
                eq("blacklist:token:" + jti),
                eq("revoked"),
                anyLong(),
                eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    @DisplayName("should return true when token jti is present in Redis blacklist")
    void shouldReturnTrueWhenTokenIsBlacklisted() {
        String jti = "test-jti-456";
        when(redisTemplate.hasKey("blacklist:token:" + jti)).thenReturn(true);

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(jti);

        assertThat(isBlacklisted).isTrue();
    }

    @Test
    @DisplayName("should return false when token jti is not present in Redis blacklist")
    void shouldReturnFalseWhenTokenIsNotBlacklisted() {
        String jti = "test-jti-789";
        when(redisTemplate.hasKey("blacklist:token:" + jti)).thenReturn(false);

        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(jti);

        assertThat(isBlacklisted).isFalse();
    }
}
