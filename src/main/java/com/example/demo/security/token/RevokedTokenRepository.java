package com.example.demo.security.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Repository
public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {

    boolean existsByJti(String jti);

    @Modifying
    @Transactional
    @Query("DELETE FROM RevokedToken r WHERE r.expiryAt < :cutoff")
    int deleteByExpiryAtBefore(@Param("cutoff") Instant cutoff);
}