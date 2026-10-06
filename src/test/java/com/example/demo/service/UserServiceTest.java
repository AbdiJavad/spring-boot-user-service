package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private UserRepository userRepository = mock(UserRepository.class);
    private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private MeterRegistry meterRegistry = mock(MeterRegistry.class);
    private Counter counter = mock(Counter.class);

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Explicitly stub the meterRegistry
        when(meterRegistry.counter(anyString())).thenReturn(counter);
        
        // Manual constructor injection to ensure complete control
        userService = new UserService(userRepository, passwordEncoder, meterRegistry);
    }

    @Test
    void testRegisterUser() {
        UserRegistrationDto dto = new UserRegistrationDto("Javad", "javad@example.com", "password");
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail(dto.email());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        
        UserDTO result = userService.registerUser(dto);
        
        assertNotNull(result);
    }
}