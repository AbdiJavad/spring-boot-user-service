package com.example.demo.service;

import com.example.demo.exception.InvalidRefreshTokenException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests - Full Branch Coverage")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenValiditySeconds", 604800L);
        testUser = new User();
        testUser.setId(100L);
        testUser.setEmail("dev@example.de");
        testUser.setName("Developer Javad");
    }

    @Nested
    @DisplayName("createRefreshToken Scenarios")
    class CreateRefreshTokenTests {
        @Test
        @DisplayName("Should create, persist, and return a valid unrevoked RefreshToken")
        void shouldCreateRefreshTokenSuccessfully() {
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken token = refreshTokenService.createRefreshToken(testUser);

            assertThat(token).isNotNull();
            assertThat(token.getUser()).isEqualTo(testUser);
            assertThat(token.getToken()).isNotBlank();
            assertThat(token.isRevoked()).isFalse();
            assertThat(token.getExpiryDate()).isAfter(Instant.now());
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
        }
    }

    @Nested
    @DisplayName("validateRefreshToken Branch Scenarios")
    class ValidateRefreshTokenTests {
        @Test
        @DisplayName("Branch: Should return token when valid")
        void shouldValidateValidTokenSuccessfully() {
            String tokenValue = "valid-token-xyz";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .user(testUser)
                    .revoked(false)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            RefreshToken result = refreshTokenService.validateRefreshToken(tokenValue);
            assertThat(result).isEqualTo(token);
        }

        @Test
        @DisplayName("Branch: Should throw when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(refreshTokenRepository.findByToken("non-existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken("non-existent"))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Invalid refresh token");
        }

        @Test
        @DisplayName("Branch: Should throw when token is revoked")
        void shouldThrowWhenTokenIsRevoked() {
            String tokenValue = "revoked-token";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .revoked(true)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(tokenValue))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Refresh token has been revoked");
        }

        @Test
        @DisplayName("Branch: Should throw when token is expired")
        void shouldThrowWhenTokenIsExpired() {
            String tokenValue = "expired-token";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .revoked(false)
                    .expiryDate(Instant.now().minus(10, ChronoUnit.MINUTES))
                    .build();

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.validateRefreshToken(tokenValue))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Refresh token has expired");
        }
    }

    @Nested
    @DisplayName("rotate Branch Scenarios")
    class RotateTests {
        @Test
        @DisplayName("Branch: Should rotate valid token")
        void shouldRotateSuccessfully() {
            String oldTokenValue = "old-token-123";
            RefreshToken oldToken = RefreshToken.builder()
                    .token(oldTokenValue)
                    .user(testUser)
                    .revoked(false)
                    .expiryDate(Instant.now().plus(2, ChronoUnit.DAYS))
                    .build();

            when(refreshTokenRepository.findByToken(oldTokenValue)).thenReturn(Optional.of(oldToken));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken newToken = refreshTokenService.rotate(oldTokenValue);

            assertThat(newToken).isNotNull();
            assertThat(newToken.getToken()).isNotEqualTo(oldTokenValue);
            assertThat(newToken.isRevoked()).isFalse();
            assertThat(newToken.getUser()).isEqualTo(testUser);
            assertThat(oldToken.isRevoked()).isTrue();
            verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Branch: Should throw when old token not found during rotate")
        void shouldThrowWhenTokenNotFoundDuringRotate() {
            when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.rotate("unknown"))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Refresh token not found");

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Branch: Should throw when rotating revoked token (reuse detection)")
        void shouldThrowWhenRotatingRevokedToken() {
            String tokenValue = "already-revoked";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .user(testUser)
                    .revoked(true)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.rotate(tokenValue))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Refresh token has been revoked (possible reuse detected)");

            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Branch: Should throw when rotating expired token")
        void shouldThrowWhenRotatingExpiredToken() {
            String tokenValue = "expired-token";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .user(testUser)
                    .revoked(false)
                    .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            assertThatThrownBy(() -> refreshTokenService.rotate(tokenValue))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Refresh token has expired");

            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("revoke Branch Scenarios")
    class RevokeTests {
        @Test
        @DisplayName("Branch: Should revoke existing token successfully")
        void shouldRevokeTokenSuccessfully() {
            String tokenValue = "token-to-revoke";
            RefreshToken token = RefreshToken.builder()
                    .token(tokenValue)
                    .revoked(false)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();

            when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(token));

            refreshTokenService.revoke(tokenValue);

            assertThat(token.isRevoked()).isTrue();
            verify(refreshTokenRepository, times(1)).save(token);
        }

        @Test
        @DisplayName("Branch: Should throw when revoking non-existent token")
        void shouldThrowWhenRevokingNonExistentToken() {
            when(refreshTokenRepository.findByToken("not-found")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> refreshTokenService.revoke("not-found"))
                    .isInstanceOf(InvalidRefreshTokenException.class)
                    .hasMessageContaining("Refresh token not found");

            verify(refreshTokenRepository, never()).save(any());
        }
    }
}
