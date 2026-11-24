package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.exception.EmailAlreadyExistsException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MeterRegistry meterRegistry;
    private final Counter registrationSuccessCounter;
    private final Counter registrationFailureCounter;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, MeterRegistry meterRegistry) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.meterRegistry = meterRegistry;
        if (meterRegistry != null) {
            this.registrationSuccessCounter = Counter.builder("user.registration.total")
                    .tag("status", "success")
                    .description("Total number of successful user registrations")
                    .register(meterRegistry);
            this.registrationFailureCounter = Counter.builder("user.registration.total")
                    .tag("status", "failure")
                    .description("Total number of failed user registrations")
                    .register(meterRegistry);
        } else {
            this.registrationSuccessCounter = null;
            this.registrationFailureCounter = null;
        }
    }

    public UserDTO registerUser(UserRegistrationDto dto) {
        try {
            if (userRepository.existsByEmail(dto.email())) {
                throw new EmailAlreadyExistsException("Email already in use: " + dto.email());
            }

            User user = new User();
            user.setName(dto.name());
            user.setEmail(dto.email());
            user.setPassword(passwordEncoder.encode(dto.password()));
            user.setRole(dto.role() != null ? dto.role() : Role.ROLE_USER);

            User savedUser = userRepository.save(user);

            if (registrationSuccessCounter != null) {
                registrationSuccessCounter.increment();
            }

            return mapToDTO(savedUser);
        } catch (Exception e) {
            if (registrationFailureCounter != null) {
                registrationFailureCounter.increment();
            }
            throw e;
        }
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserDTO mapToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return dto;
    }
}
