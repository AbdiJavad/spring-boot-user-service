package com.example.demo.repository;

import com.example.demo.model.RefreshToken;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // Ã™â€¦Ã˜ÂªÃ˜Â¯Ã¢â‚¬Å’Ã™â€¡Ã˜Â§Ã›Å’ Ã™â€šÃ˜Â¨Ã™â€žÃ›Å’ (Ã˜Â­Ã›Å’Ã˜Â§Ã˜ÂªÃ›Å’ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ Ã˜Â³Ã˜Â§Ã›Å’Ã˜Â± Ã˜Â¨Ã˜Â®Ã˜Â´Ã¢â‚¬Å’Ã™â€¡Ã˜Â§Ã›Å’ Ã˜Â³Ã›Å’Ã˜Â³Ã˜ÂªÃ™â€¦)
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);

    // Ã™â€¦Ã˜ÂªÃ˜Â¯ Ã˜Â¬Ã˜Â¯Ã›Å’Ã˜Â¯ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ Ã˜Â­Ã™â€ž Ã™â€¦Ã˜Â´ÃšÂ©Ã™â€ž LazyInitializationException
    // Ã˜Â§Ã›Å’Ã™â€  Ã™â€¦Ã˜ÂªÃ˜Â¯ User Ã˜Â±Ã˜Â§ Ã™â€¡Ã™â€¦Ã˜Â²Ã™â€¦Ã˜Â§Ã™â€  Ã˜Â¨Ã˜Â§ RefreshToken Ã™Ë†Ã˜Â§ÃšÂ©Ã˜Â´Ã›Å’ (Fetch) Ã™â€¦Ã›Å’Ã¢â‚¬Å’ÃšÂ©Ã™â€ Ã˜Â¯
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);
}
