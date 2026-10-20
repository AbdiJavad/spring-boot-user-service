package com.example.demo.service;

import com.example.demo.dto.RefreshTokenResult;
import com.example.demo.exception.InvalidRefreshTokenException;
import com.example.demo.model.RefreshToken;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenValiditySeconds", 604800L);

        testUser = User.builder()
                .id(1L)
                .name("Max Mustermann")
                .email("developer@example.com")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Should create new refresh token successfully")
    void shouldCreateRefreshTokenSuccessfully() {
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenResult result = refreshTokenService.createRefreshToken(testUser);

        assertThat(result).isNotNull();
        assertThat(result.rawToken()).isNotBlank();
        assertThat(result.refreshToken().getUser()).isEqualTo(testUser);
        assertThat(result.refreshToken().getTokenHash()).isEqualTo(refreshTokenService.hashToken(result.rawToken()));
        assertThat(result.refreshToken().getFamilyId()).isNotBlank();
        assertThat(result.refreshToken().isRevoked()).isFalse();
        assertThat(result.refreshToken().getExpiryDate()).isAfter(Instant.now());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should rotate valid refresh token successfully")
    void shouldRotateRefreshTokenSuccessfully() {
        String rawToken = "sample-raw-token-123";
        String tokenHash = refreshTokenService.hashToken(rawToken);
        String familyId = UUID.randomUUID().toString();

        RefreshToken existingToken = RefreshToken.builder()
                .id(10L)
                .user(testUser)
                .tokenHash(tokenHash)
                .familyId(familyId)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenResult result = refreshTokenService.rotate(rawToken);

        assertThat(result).isNotNull();
        assertThat(result.rawToken()).isNotBlank();
        assertThat(result.refreshToken().getTokenHash()).isNotEqualTo(tokenHash);
        assertThat(result.refreshToken().getFamilyId()).isEqualTo(familyId);
        assertThat(existingToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Should detect token reuse/theft and revoke family")
    void shouldDetectTokenReuseAndRevokeFamily() {
        String rawToken = "compromised-token";
        String tokenHash = refreshTokenService.hashToken(rawToken);
        String familyId = UUID.randomUUID().toString();

        RefreshToken compromisedToken = RefreshToken.builder()
                .id(10L)
                .user(testUser)
                .tokenHash(tokenHash)
                .familyId(familyId)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(compromisedToken));

        assertThatThrownBy(() -> refreshTokenService.rotate(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("Token reuse detected");

        verify(refreshTokenRepository).revokeTokenFamily(familyId);
    }
}
