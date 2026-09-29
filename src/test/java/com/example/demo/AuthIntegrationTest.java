package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.TokenResponse;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication & Refresh Token Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Û±. Ø§ÛŒØ²ÙˆÙ„Ù‡â€ŒØ³Ø§Ø²ÛŒ Ù…Ø­ÛŒØ· ØªØ³Øª
        userRepository.deleteAll();

        // Û². Ø¢Ù…Ø§Ø¯Ù‡â€ŒØ³Ø§Ø²ÛŒ Ú©Ø§Ø±Ø¨Ø± ØªØ³Øª
        User testUser = User.builder()
                .name("Test User")
                .email("testuser@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(testUser);

        loginRequest = new LoginRequest("testuser@example.com", "password123");
    }

    @Test
    @DisplayName("Full Auth Lifecycle: Login -> Refresh -> Logout")
    void testFullAuthLifecycle() throws Exception {

        // 1. ÙˆØ±ÙˆØ¯ Ùˆ Ø¯Ø±ÛŒØ§ÙØª Access Ùˆ Refresh Token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String responseJson = loginResult.getResponse().getContentAsString();
        TokenResponse tokens = objectMapper.readValue(responseJson, TokenResponse.class);
        String initialAccessToken = tokens.accessToken();
        String initialRefreshToken = tokens.refreshToken();

        assertNotNull(initialAccessToken);
        assertNotNull(initialRefreshToken);

        // 2. Ù…Ø±Ø­Ù„Ù‡ Refresh Token Ø¨Ø§ DTO Ø§Ø³ØªØ§Ù†Ø¯Ø§Ø±Ø¯
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(initialRefreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String newTokensJson = refreshResult.getResponse().getContentAsString();
        TokenResponse newTokens = objectMapper.readValue(newTokensJson, TokenResponse.class);

        assertNotNull(newTokens.accessToken());
        assertFalse(newTokens.accessToken().isBlank());
        assertNotEquals(initialRefreshToken, newTokens.refreshToken(), "Refresh token must rotate");


        // 3. Ù…Ø±Ø­Ù„Ù‡ Logout (Ø§Ø±Ø³Ø§Ù„ Ø¨Ø¯Ù†Ù‡ Ù…Ø¹ØªØ¨Ø± Ø´Ø§Ù…Ù„ RefreshToken Ø¬Ø¯ÛŒØ¯)
        RefreshTokenRequest logoutReq = new RefreshTokenRequest(newTokens.refreshToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutReq)))
                .andExpect(status().isNoContent());

        // 4. Ø§Ø¹ØªØ¨Ø§Ø±Ø³Ù†Ø¬ÛŒ Ø§Ø¨Ø·Ø§Ù„: ØªÙˆÚ©Ù† Ù‚Ø¯ÛŒÙ…ÛŒ Ø¯ÙˆØ±Ø§Ù†Ø¯Ø§Ø®ØªÙ‡ Ø´Ø¯Ù‡ (initialRefreshToken) Ø¯ÛŒÚ¯Ø± Ù†Ø¨Ø§ÛŒØ¯ Ú©Ø§Ø± Ú©Ù†Ø¯
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());
    }
}
