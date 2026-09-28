package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Ã˜Â§Ã›Å’Ã™â€  Ã˜Â±Ã˜Â§ import ÃšÂ©Ã™â€ 
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Ã˜Â§Ã›Å’Ã™â€ Ã˜Â¬Ã˜Â§ Ã™â€žÃ˜Â§Ã˜Â²Ã™â€¦ Ã™â€ Ã›Å’Ã˜Â³Ã˜Âª Ãšâ€ Ã›Å’Ã˜Â²Ã›Å’ Ã˜Â¨Ã™â€ Ã™Ë†Ã›Å’Ã˜Â³Ã›Å’! Ã˜Â¬Ã˜Â§Ã˜Â¯Ã™Ë†Ã›Å’ Ã˜Â§Ã˜Â³Ã™Â¾Ã˜Â±Ã›Å’Ã™â€ ÃšÂ¯ Ã™â€¡Ã™â€¦Ã›Å’Ã™â€ Ã¢â‚¬Å’Ã˜Â¬Ã˜Â§Ã˜Â³Ã˜Âª.
    List<User> findByName(String name);
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email); // Ã˜Â§Ã›Å’Ã™â€  Ã˜Â®Ã˜Â· Ã˜Â±Ã˜Â§ Ã˜Â§Ã˜Â¶Ã˜Â§Ã™ÂÃ™â€¡ ÃšÂ©Ã™â€ 
}
