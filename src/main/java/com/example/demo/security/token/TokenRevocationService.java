package com.example.demo.security.token;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository repository;

    public TokenRevocationService(RevokedTokenRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        return repository.existsByJti(jti);
    }

    @Transactional
    public void revoke(String jti, Instant tokenExpiry) {
        if (jti != null && !repository.existsByJti(jti)) {
            repository.save(new RevokedToken(jti, tokenExpiry, Instant.now()));
        }
    }
}