package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // ÃƒËœÃ‚Â§Ãƒâ€ºÃ…â€™Ãƒâ„¢Ã¢â‚¬Â  ÃƒËœÃ‚Â±ÃƒËœÃ‚Â§ import ÃƒÅ¡Ã‚Â©Ãƒâ„¢Ã¢â‚¬Â 
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // ÃƒËœÃ‚Â§Ãƒâ€ºÃ…â€™Ãƒâ„¢Ã¢â‚¬Â ÃƒËœÃ‚Â¬ÃƒËœÃ‚Â§ Ãƒâ„¢Ã¢â‚¬Å¾ÃƒËœÃ‚Â§ÃƒËœÃ‚Â²Ãƒâ„¢Ã¢â‚¬Â¦ Ãƒâ„¢Ã¢â‚¬Â Ãƒâ€ºÃ…â€™ÃƒËœÃ‚Â³ÃƒËœÃ‚Âª ÃƒÅ¡Ã¢â‚¬Â Ãƒâ€ºÃ…â€™ÃƒËœÃ‚Â²Ãƒâ€ºÃ…â€™ ÃƒËœÃ‚Â¨Ãƒâ„¢Ã¢â‚¬Â Ãƒâ„¢Ã‹â€ Ãƒâ€ºÃ…â€™ÃƒËœÃ‚Â³Ãƒâ€ºÃ…â€™! ÃƒËœÃ‚Â¬ÃƒËœÃ‚Â§ÃƒËœÃ‚Â¯Ãƒâ„¢Ã‹â€ Ãƒâ€ºÃ…â€™ ÃƒËœÃ‚Â§ÃƒËœÃ‚Â³Ãƒâ„¢Ã‚Â¾ÃƒËœÃ‚Â±Ãƒâ€ºÃ…â€™Ãƒâ„¢Ã¢â‚¬Â ÃƒÅ¡Ã‚Â¯ Ãƒâ„¢Ã¢â‚¬Â¡Ãƒâ„¢Ã¢â‚¬Â¦Ãƒâ€ºÃ…â€™Ãƒâ„¢Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã…â€™ÃƒËœÃ‚Â¬ÃƒËœÃ‚Â§ÃƒËœÃ‚Â³ÃƒËœÃ‚Âª.
    List<User> findByName(String name);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email); // ÃƒËœÃ‚Â§Ãƒâ€ºÃ…â€™Ãƒâ„¢Ã¢â‚¬Â  ÃƒËœÃ‚Â®ÃƒËœÃ‚Â· ÃƒËœÃ‚Â±ÃƒËœÃ‚Â§ ÃƒËœÃ‚Â§ÃƒËœÃ‚Â¶ÃƒËœÃ‚Â§Ãƒâ„¢Ã‚ÂÃƒâ„¢Ã¢â‚¬Â¡ ÃƒÅ¡Ã‚Â©Ãƒâ„¢Ã¢â‚¬Â 
}
