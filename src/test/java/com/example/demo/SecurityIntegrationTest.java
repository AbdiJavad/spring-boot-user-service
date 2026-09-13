package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Ø¯Ø³ØªØ±Ø³ÛŒ Ø¨Ø¯ÙˆÙ† ØªÙˆÚ©Ù† Ø¨Ù‡ Ø§Ù†Ø¯Ù¾ÙˆÛŒÙ†Øª Ù…Ø­Ø§ÙØ¸Øªâ€ŒØ´Ø¯Ù‡ Ø¨Ø§ÛŒØ¯ 401 Ø¨Ø±Ú¯Ø±Ø¯Ø§Ù†Ø¯")
    void shouldReturn401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Ø«Ø¨Øªâ€ŒÙ†Ø§Ù… Ú©Ø§Ø±Ø¨Ø± Ø¬Ø¯ÛŒØ¯ Ø¨Ø§ Ø¯Ø§Ø¯Ù‡â€ŒÙ‡Ø§ÛŒ Ù…Ø¹ØªØ¨Ø± Ø¨Ø§ÛŒØ¯ Ù…ÙˆÙÙ‚ÛŒØªâ€ŒØ¢Ù…ÛŒØ² Ø¨Ø§Ø´Ø¯")
    void shouldRegisterUserSuccessfully() throws Exception {
        String userJson = """
                {
                    "name": "Jovan",
                    "email": "jovan.new@example.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.email").value("jovan.new@example.com"));
    }

    @Test
    @DisplayName("Ø«Ø¨Øªâ€ŒÙ†Ø§Ù… Ø¨Ø§ Ø§ÛŒÙ…ÛŒÙ„ ØªÚ©Ø±Ø§Ø±ÛŒ Ø¨Ø§ÛŒØ¯ Ø¨Ø§ Ø®Ø·Ø§ÛŒ 409 Ù…ÙˆØ§Ø¬Ù‡ Ø´ÙˆØ¯")
    void shouldFailWhenRegisteringDuplicateEmail() throws Exception {
        User existingUser = User.builder()
                .name("Existing")
                .email("duplicate@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build();
        userRepository.save(existingUser);

        String duplicateUserJson = """
                {
                    "name": "Jovan Duplicate",
                    "email": "duplicate@example.com",
                    "password": "Password123!"
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUserJson))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Ø¯Ø³ØªØ±Ø³ÛŒ Ø¨Ù‡ Ø§Ù†Ø¯Ù¾ÙˆÛŒÙ†Øª Ù…Ø­Ø§ÙØ¸Øªâ€ŒØ´Ø¯Ù‡ Ø¨Ø§ ØªÙˆÚ©Ù† Ù…Ø¹ØªØ¨Ø± JWT")
    void shouldAccessProtectedEndpointWithValidJwtToken() throws Exception {
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "Jovan Admin",
                "jovan.auth@example.com",
                "SecurePass123!",
                null
        );
        userService.registerUser(registrationDto);

        LoginRequest loginDto = new LoginRequest("jovan.auth@example.com", "SecurePass123!");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String jwtToken = JsonPath.read(responseContent, "$.accessToken");

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jovan.auth@example.com"))
                .andExpect(jsonPath("$.name").value("Jovan Admin"));
    }

    @Test
    @DisplayName("Normal user cannot delete other users - Should return 403 Forbidden")
    void normalUser_CannotDeleteOtherUsers_ShouldReturnForbidden() throws Exception {
        User normalUser = userRepository.save(User.builder()
                .name("Normal User")
                .email("normal@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        User targetUser = userRepository.save(User.builder()
                .name("Target User")
                .email("target@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        String userToken = jwtService.generateToken(normalUser);

        mockMvc.perform(delete("/api/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin user can delete other users - Should return 204 No Content")
    void adminUser_CanDeleteOtherUsers_ShouldReturnNoContent() throws Exception {
        User adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_ADMIN)
                .build());

        User targetUser = userRepository.save(User.builder()
                .name("Target User To Delete")
                .email("target.delete@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        String adminToken = jwtService.generateToken(adminUser);

        mockMvc.perform(delete("/api/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return new tokens when refresh token is valid and rotation works")
    void shouldReturnNewTokensWhenRefreshTokenIsValid() throws Exception {
        // 1. Arrange
        String email = "refresh.test@example.com";
        userService.registerUser(new UserRegistrationDto("RefreshUser", email, "Password123!", Role.ROLE_USER));

        LoginRequest loginRequest = new LoginRequest(email, "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String firstRefreshToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.refreshToken");

        // 2. Act: Ù…Ø±Ø­Ù„Ù‡ Ø§ÙˆÙ„ Ø±ÙØ±Ø´
        MvcResult firstRefreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(firstRefreshToken))))
                .andExpect(status().isOk())
                .andReturn();

        // *** Ø§ÛŒÙ†Ø¬Ø§ Ø¨Ø³ÛŒØ§Ø± Ù…Ù‡Ù… Ø§Ø³Øª: Ø¨Ø§ÛŒØ¯ Ø§Ø¨ØªØ¯Ø§ ØªÙˆÚ©Ù† Ø¬Ø¯ÛŒØ¯ Ø±Ø§ Ø§Ø² Ù¾Ø§Ø³Ø®Ù Ù…Ø±Ø­Ù„Ù‡ Ø§ÙˆÙ„ Ø§Ø³ØªØ®Ø±Ø§Ø¬ Ú©Ù†ÛŒ ***
        String secondRefreshToken = JsonPath.read(firstRefreshResult.getResponse().getContentAsString(), "$.refreshToken");

        // 3. Act: Ù…Ø±Ø­Ù„Ù‡ Ø¯ÙˆÙ… Ø±ÙØ±Ø´ (Ø­Ø§Ù„Ø§ Ú©Ù‡ Ù…ØªØºÛŒØ± Ø¨Ø§Ù„Ø§ ØªØ¹Ø±ÛŒÙ Ø´Ø¯Ù‡ØŒ Ø§ÛŒÙ†Ø¬Ø§ Ù‚Ø±Ù…Ø² Ù†Ù…ÛŒâ€ŒØ´ÙˆØ¯)
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshTokenRequest(secondRefreshToken))))
                .andExpect(status().isOk());


    }
}
