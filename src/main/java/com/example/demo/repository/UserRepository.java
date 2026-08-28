package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // این را import کن

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // اینجا لازم نیست چیزی بنویسی! جادوی اسپرینگ همین‌جاست.
    List<User> findByName(String name);
}
