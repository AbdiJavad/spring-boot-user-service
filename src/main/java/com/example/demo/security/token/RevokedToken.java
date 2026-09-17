package com.example.demo.security.token;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revoked_tokens", indexes = {
    @Index(name = "idx_revoked_tokens_jti", columnList = "jti", unique = true),
    @Index(name = "idx_revoked_tokens_expiry", columnList = "expiry_at")
})
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String jti;

    @Column(name = "expiry_at", nullable = false)
    private Instant expiryAt;

    @Column(name = "revoked_at", nullable = false)
    private Instant revokedAt;

    protected RevokedToken() {}

    public RevokedToken(String jti, Instant expiryAt, Instant revokedAt) {
        this.jti = jti;
        this.expiryAt = expiryAt;
        this.revokedAt = revokedAt;
    }

    public Long getId() { return id; }
    public String getJti() { return jti; }
    public Instant getExpiryAt() { return expiryAt; }
    public Instant getRevokedAt() { return revokedAt; }
}