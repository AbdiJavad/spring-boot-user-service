package com.example.demo.security.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRevocationService Unit Tests")
class TokenRevocationServiceTest {

    @Mock
    private RevokedTokenRepository repository;

    @InjectMocks
    private TokenRevocationService revocationService;

    @Test
    @DisplayName("isRevoked returns false when jti is null")
    void shouldReturnFalseWhenJtiIsNull() {
        boolean result = revocationService.isRevoked(null);
        assertThat(result).isFalse();
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("isRevoked returns false when jti is blank")
    void shouldReturnFalseWhenJtiIsBlank() {
        boolean result = revocationService.isRevoked("   ");
        assertThat(result).isFalse();
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("isRevoked checks repository when jti is valid and returns true if exists")
    void shouldReturnTrueWhenJtiExistsInRepository() {
        String jti = "valid-jti-123";
        when(repository.existsByJti(jti)).thenReturn(true);

        boolean result = revocationService.isRevoked(jti);

        assertThat(result).isTrue();
        verify(repository, times(1)).existsByJti(jti);
    }

    @Test
    @DisplayName("isRevoked checks repository when jti is valid and returns false if not exists")
    void shouldReturnFalseWhenJtiDoesNotExistInRepository() {
        String jti = "valid-jti-123";
        when(repository.existsByJti(jti)).thenReturn(false);

        boolean result = revocationService.isRevoked(jti);

        assertThat(result).isFalse();
        verify(repository, times(1)).existsByJti(jti);
    }

    @Test
    @DisplayName("revoke does nothing when jti is null")
    void shouldNotRevokeWhenJtiIsNull() {
        revocationService.revoke(null, Instant.now().plusSeconds(3600));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("revoke saves token when jti is valid and not already revoked")
    void shouldRevokeWhenJtiIsValidAndNotYetRevoked() {
        String jti = "new-jti-456";
        Instant expiry = Instant.now().plusSeconds(3600);
        when(repository.existsByJti(jti)).thenReturn(false);

        revocationService.revoke(jti, expiry);

        verify(repository, times(1)).existsByJti(jti);
        verify(repository, times(1)).save(ArgumentMatchers.any(RevokedToken.class));
    }

    @Test
    @DisplayName("revoke does not save token when jti is already revoked")
    void shouldNotSaveWhenJtiIsAlreadyRevoked() {
        String jti = "existing-jti-789";
        Instant expiry = Instant.now().plusSeconds(3600);
        when(repository.existsByJti(jti)).thenReturn(true);

        revocationService.revoke(jti, expiry);

        verify(repository, times(1)).existsByJti(jti);
        verify(repository, never()).save(ArgumentMatchers.any(RevokedToken.class));
    }
}
