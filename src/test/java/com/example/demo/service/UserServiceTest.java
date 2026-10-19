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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests - Branch & Edge Case Coverage")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private Counter registrationCounter;

    private UserService userService;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("user.registration.count")).thenReturn(registrationCounter);
        userService = new UserService(userRepository, passwordEncoder, meterRegistry);
    }

    @Nested
    @DisplayName("registerUser Branch Scenarios")
    class RegisterUserTests {

        @Test
        @DisplayName("Branch: Should successfully register user with default ROLE_USER when role is null")
        void shouldRegisterUserWithDefaultRoleWhenRoleIsNull() {
            UserRegistrationDto dto = new UserRegistrationDto("Hans", "hans@example.de", "Secret123!", null);

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(passwordEncoder.encode(dto.password())).thenReturn("hashedSecret");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(1L);
                return u;
            });

            UserDTO result = userService.registerUser(dto);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getRole()).isEqualTo(Role.ROLE_USER.name());
            verify(registrationCounter, times(1)).increment();
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Branch: Should register user with specified role (e.g. ROLE_ADMIN)")
        void shouldRegisterUserWithExplicitRole() {
            UserRegistrationDto dto = new UserRegistrationDto("Admin", "admin@example.de", "Admin123!", Role.ROLE_ADMIN);

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(passwordEncoder.encode(dto.password())).thenReturn("hashedAdmin");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                return u;
            });

            UserDTO result = userService.registerUser(dto);

            assertThat(result.getRole()).isEqualTo(Role.ROLE_ADMIN.name());
        }

        @Test
        @DisplayName("Branch: Should throw EmailAlreadyExistsException when email is taken")
        void shouldThrowExceptionWhenEmailExists() {
            UserRegistrationDto dto = new UserRegistrationDto("Max", "taken@example.de", "Pw123!", Role.ROLE_USER);

            when(userRepository.existsByEmail(dto.email())).thenReturn(true);

            assertThatThrownBy(() -> userService.registerUser(dto))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("Email already in use: taken@example.de");

            verify(userRepository, never()).save(any());
            verify(registrationCounter, never()).increment();
        }

        @Test
        @DisplayName("Branch: Constructor with null MeterRegistry should not fail and ignore metrics")
        void shouldHandleNullMeterRegistryGracefully() {
            UserService serviceWithoutMetrics = new UserService(userRepository, passwordEncoder, null);
            UserRegistrationDto dto = new UserRegistrationDto("Eva", "eva@example.de", "Secret123!", Role.ROLE_USER);

            when(userRepository.existsByEmail(dto.email())).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(3L);
                return u;
            });

            UserDTO result = serviceWithoutMetrics.registerUser(dto);
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("getUserById Branch Scenarios")
    class GetUserByIdTests {

        @Test
        @DisplayName("Branch: Should return UserDTO when user is found")
        void shouldReturnUserWhenFound() {
            User user = new User();
            user.setId(10L);
            user.setName("Lukas");
            user.setEmail("lukas@example.de");
            user.setRole(Role.ROLE_USER);

            when(userRepository.findById(10L)).thenReturn(Optional.of(user));

            UserDTO result = userService.getUserById(10L);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getEmail()).isEqualTo("lukas@example.de");
        }

        @Test
        @DisplayName("Branch: Should throw ResourceNotFoundException when user is not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 99");
        }
    }

    @Nested
    @DisplayName("deleteUser Branch Scenarios")
    class DeleteUserTests {

        @Test
        @DisplayName("Branch: Should delete user when id exists")
        void shouldDeleteUserWhenExists() {
            when(userRepository.existsById(5L)).thenReturn(true);

            userService.deleteUser(5L);

            verify(userRepository, times(1)).deleteById(5L);
        }

        @Test
        @DisplayName("Branch: Should throw ResourceNotFoundException when deleting non-existent id")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            when(userRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found with id: 999");

            verify(userRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("getAllUsers & mapToDTO Scenarios")
    class GetAllUsersTests {

        @Test
        @DisplayName("Branch: Should map list of users and handle null role correctly")
        void shouldMapUsersAndHandleNullRole() {
            User userWithoutRole = new User();
            userWithoutRole.setId(1L);
            userWithoutRole.setName("NoRole");
            userWithoutRole.setEmail("norole@example.de");
            userWithoutRole.setRole(null);

            when(userRepository.findAll()).thenReturn(List.of(userWithoutRole));

            List<UserDTO> users = userService.getAllUsers();

            assertThat(users).hasSize(1);
            assertThat(users.get(0).getRole()).isNull();
        }
    }
}
