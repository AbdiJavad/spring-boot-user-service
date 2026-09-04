package com.example.demo;

import com.example.demo.service.UserService;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.dto.LoginRequest;
import com.jayway.jsonpath.JsonPath;
import org.springframework.test.web.servlet.MvcResult;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("دسترسی بدون توکن به اندپوینت محافظت‌شده باید 401 برگرداند")
    void shouldReturn401WhenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ثبت‌نام کاربر جدید با داده‌های معتبر باید موفقیت‌آمیز باشد")
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
    @DisplayName("ثبت‌نام با ایمیل تکراری باید با خطای 409 مواجه شود")
    void shouldFailWhenRegisteringDuplicateEmail() throws Exception {
        // ایجاد یک کاربر اولیه در دیتابیس
        User existingUser = new User();
        existingUser.setName("Existing");
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPassword(passwordEncoder.encode("Password123!"));
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
    void shouldAccessProtectedEndpointWithValidJwtToken() throws Exception {
        // 1. Arrange: ثبت نام کاربر تستی با UserRegistrationDto
        UserRegistrationDto registerDto = new UserRegistrationDto("Jovan Admin", "jovan.auth@example.com", "SecurePass123!");
        userService.registerUser(registerDto);

        // آماده‌سازی اطلاعات لاگین با LoginRequest
        LoginRequest loginDto = new LoginRequest("jovan.auth@example.com", "SecurePass123!");

        // 2. Act: لاگین و دریافت توکن
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // استخراج رشته توکن از خروجی لاگین
        String responseContent = loginResult.getResponse().getContentAsString();
        String jwtToken = JsonPath.read(responseContent, "$.token");

        // 3. Act & Assert: دسترسی به /api/users/me با هدر Bearer Token
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("jovan.auth@example.com"))
                .andExpect(jsonPath("$.name").value("Jovan Admin"));
    }

    @Test
    @DisplayName("ورود موفقیت‌آمیز باید یک JWT Token معتبر برگرداند")
    void shouldAuthenticateAndReturnJwt() throws Exception {
        // ایجاد کاربر برای ورود
        User user = new User();
        user.setName("Login User");
        user.setEmail("login@example.com");
        user.setPassword(passwordEncoder.encode("SecretPass123"));
        userRepository.save(user);

        String loginJson = """
                {
                    "email": "login@example.com",
                    "password": "SecretPass123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }
}
