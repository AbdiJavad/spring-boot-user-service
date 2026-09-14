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

    // Ù…ØªØ¯â€ŒÙ‡Ø§ÛŒ Ù‚Ø¨Ù„ÛŒ (Ø­ÛŒØ§ØªÛŒ Ø¨Ø±Ø§ÛŒ Ø³Ø§ÛŒØ± Ø¨Ø®Ø´â€ŒÙ‡Ø§ÛŒ Ø³ÛŒØ³ØªÙ…)
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByUser(User user);
    void deleteByUser(User user);

    // Ù…ØªØ¯ Ø¬Ø¯ÛŒØ¯ Ø¨Ø±Ø§ÛŒ Ø­Ù„ Ù…Ø´Ú©Ù„ LazyInitializationException
    // Ø§ÛŒÙ† Ù…ØªØ¯ User Ø±Ø§ Ù‡Ù…Ø²Ù…Ø§Ù† Ø¨Ø§ RefreshToken ÙˆØ§Ú©Ø´ÛŒ (Fetch) Ù…ÛŒâ€ŒÚ©Ù†Ø¯
    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshToken> findByTokenWithUser(@Param("token") String token);
}
