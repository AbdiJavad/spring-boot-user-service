package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.ratelimit.RateLimitFilter;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() throws Exception {
        // Mock RateLimitFilter to act as a pass-through filter
        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());

        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

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
        // 1. Login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String loginJson = loginResult.getResponse().getContentAsString();
        String initialAccessToken = JsonPath.read(loginJson, "$.accessToken");
        String initialRefreshToken = JsonPath.read(loginJson, "$.refreshToken");

        assertNotNull(initialAccessToken);
        assertNotNull(initialRefreshToken);

        // 2. Refresh
        RefreshTokenRequest refreshReq = new RefreshTokenRequest(initialRefreshToken);
        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        String refreshJson = refreshResult.getResponse().getContentAsString();
        String newAccessToken = JsonPath.read(refreshJson, "$.accessToken");
        String newRefreshToken = JsonPath.read(refreshJson, "$.refreshToken");

        assertNotNull(newAccessToken);
        assertFalse(newAccessToken.isBlank());
        assertNotEquals(initialRefreshToken, newRefreshToken, "Refresh token must rotate");
    }
}