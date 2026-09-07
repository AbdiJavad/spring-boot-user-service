package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testGetUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setName("Jovan");
        user.setEmail("jovan@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("Jovan", result.getName());
        assertEquals("jovan@example.com", result.getEmail());

        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserById_NotFound() {
        // ۱. به Mockito می‌گیم: وقتی آی‌دیِ 99 رو جستجو کردیم، هیچی برنگردون (Optional.empty)
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // ۲. چک می‌کنیم که آیا سیستم خطای درست رو پرتاب می‌کنه؟
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(99L);
        });
    }

}
