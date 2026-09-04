package com.example.demo;

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

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
