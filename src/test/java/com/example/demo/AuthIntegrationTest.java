package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.TokenResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Authentication & Refresh Token Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() {
        // فرض بر این است که یک کاربر تست در دیتابیس دارید یا در فرآیند تست ساخته می‌شود
        // برای سادگی، اینجا اطلاعات یک کاربر فرضی را ست می‌کنیم
        loginRequest = new LoginRequest("testuser@example.com", "password123");
    }

    @Test
    @DisplayName("Full Auth Lifecycle: Login -> Refresh -> Logout")
    void testFullAuthLifecycle() throws Exception {

        // 1. مرحله Login
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        // استخراج توکن‌ها از پاسخ JSON
        String responseJson = loginResult.getResponse().getContentAsString();
        // (در محیط واقعی بهتر است از یک DTO برای پارس کردن استفاده کنید)
        // اینجا برای خلاصه شدن، فرض می‌کنیم پارس شده‌اند:
        TokenResponse tokens = objectMapper.readValue(responseJson, TokenResponse.class);
        this.accessToken = tokens.accessToken();
        this.refreshToken = tokens.refreshToken();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);

        // 2. مرحله Refresh Token
        // ایجاد درخواست برای رفرش کردن (باید refreshToken را در بدنه بفرستیم)
        // فرض می‌کنیم RefreshTokenRequest یک record ساده است: record RefreshTokenRequest(String refreshToken) {}
        var refreshRequest = java.util.Map.of("refreshToken", refreshToken);

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String newTokensJson = refreshResult.getResponse().getContentAsString();
        TokenResponse newTokens = objectMapper.readValue(newTokensJson, TokenResponse.class);

        assertNotEquals(accessToken, newTokens.accessToken(), "Access token should rotate");
        assertNotEquals(refreshToken, newTokens.refreshToken(), "Refresh token should rotate");

        // 3. مرحله Logout
        // استفاده از توکن جدید برای خروج
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + newTokens.accessToken())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // 4. مرحله نهایی: تست ابطال (Verification)
        // تلاش برای استفاده از رفرش توکن قدیمی (که باید با خطا مواجه شود)
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
        // نکته: بسته به پیاده‌سازی شما، ممکن است 401 یا 403 باشد
    }
}
