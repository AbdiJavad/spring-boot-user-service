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
        // Ã›Â±. Ã˜Â§Ã›Å’Ã˜Â²Ã™Ë†Ã™â€žÃ™â€¡Ã¢â‚¬Å’Ã˜Â³Ã˜Â§Ã˜Â²Ã›Å’ Ã™â€¦Ã˜Â­Ã›Å’Ã˜Â· Ã˜ÂªÃ˜Â³Ã˜Âª
        userRepository.deleteAll();

        // Ã›Â². Ã˜Â¢Ã™â€¦Ã˜Â§Ã˜Â¯Ã™â€¡Ã¢â‚¬Å’Ã˜Â³Ã˜Â§Ã˜Â²Ã›Å’ ÃšÂ©Ã˜Â§Ã˜Â±Ã˜Â¨Ã˜Â± Ã˜ÂªÃ˜Â³Ã˜Âª
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

        // 1. Ã™Ë†Ã˜Â±Ã™Ë†Ã˜Â¯ Ã™Ë† Ã˜Â¯Ã˜Â±Ã›Å’Ã˜Â§Ã™ÂÃ˜Âª Access Ã™Ë† Refresh Token
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

        // 2. Ã™â€¦Ã˜Â±Ã˜Â­Ã™â€žÃ™â€¡ Refresh Token Ã˜Â¨Ã˜Â§ DTO Ã˜Â§Ã˜Â³Ã˜ÂªÃ˜Â§Ã™â€ Ã˜Â¯Ã˜Â§Ã˜Â±Ã˜Â¯
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


        // 3. Ã™â€¦Ã˜Â±Ã˜Â­Ã™â€žÃ™â€¡ Logout (Ã˜Â§Ã˜Â±Ã˜Â³Ã˜Â§Ã™â€ž Ã˜Â¨Ã˜Â¯Ã™â€ Ã™â€¡ Ã™â€¦Ã˜Â¹Ã˜ÂªÃ˜Â¨Ã˜Â± Ã˜Â´Ã˜Â§Ã™â€¦Ã™â€ž RefreshToken Ã˜Â¬Ã˜Â¯Ã›Å’Ã˜Â¯)
        RefreshTokenRequest logoutReq = new RefreshTokenRequest(newTokens.refreshToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutReq)))
                .andExpect(status().isNoContent());

        // 4. Ã˜Â§Ã˜Â¹Ã˜ÂªÃ˜Â¨Ã˜Â§Ã˜Â±Ã˜Â³Ã™â€ Ã˜Â¬Ã›Å’ Ã˜Â§Ã˜Â¨Ã˜Â·Ã˜Â§Ã™â€ž: Ã˜ÂªÃ™Ë†ÃšÂ©Ã™â€  Ã™â€šÃ˜Â¯Ã›Å’Ã™â€¦Ã›Å’ Ã˜Â¯Ã™Ë†Ã˜Â±Ã˜Â§Ã™â€ Ã˜Â¯Ã˜Â§Ã˜Â®Ã˜ÂªÃ™â€¡ Ã˜Â´Ã˜Â¯Ã™â€¡ (initialRefreshToken) Ã˜Â¯Ã›Å’ÃšÂ¯Ã˜Â± Ã™â€ Ã˜Â¨Ã˜Â§Ã›Å’Ã˜Â¯ ÃšÂ©Ã˜Â§Ã˜Â± ÃšÂ©Ã™â€ Ã˜Â¯
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized());
    }
}
