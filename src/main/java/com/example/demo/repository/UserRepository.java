package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Ø§ÛŒÙ† Ø±Ø§ import Ú©Ù†
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Ø§ÛŒÙ†Ø¬Ø§ Ù„Ø§Ø²Ù… Ù†ÛŒØ³Øª Ú†ÛŒØ²ÛŒ Ø¨Ù†ÙˆÛŒØ³ÛŒ! Ø¬Ø§Ø¯ÙˆÛŒ Ø§Ø³Ù¾Ø±ÛŒÙ†Ú¯ Ù‡Ù…ÛŒÙ†â€ŒØ¬Ø§Ø³Øª.
    List<User> findByName(String name);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email); // Ø§ÛŒÙ† Ø®Ø· Ø±Ø§ Ø§Ø¶Ø§ÙÙ‡ Ú©Ù†
}
