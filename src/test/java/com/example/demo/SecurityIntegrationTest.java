package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
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
    @DisplayName("دسترسی به اندپوینت محافظت‌شده با توکن معتبر JWT")
    void shouldAccessProtectedEndpointWithValidJwtToken() throws Exception {
        // 1. Arrange: ثبت‌نام کاربر تست با مشخصات یکپارچه
        UserRegistrationDto registrationDto = new UserRegistrationDto(
                "Jovan Admin",
                "jovan.auth@example.com",
                "SecurePass123!",
                null
        );
        userService.registerUser(registrationDto); // ✅ متغیر اصلاح شد

        LoginRequest loginDto = new LoginRequest("jovan.auth@example.com", "SecurePass123!");

        // 2. Act: لاگین و دریافت توکن JWT
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        String jwtToken = JsonPath.read(responseContent, "$.token");

        // 3. Assert: دسترسی به اندپوینت امن /api/users/me
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
        // 1. ایجاد کاربر عادی (درخواست‌دهنده)
        User normalUser = userRepository.save(User.builder()
                .name("Normal User")
                .email("normal@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        // 2. ایجاد کاربر هدف برای حذف
        User targetUser = userRepository.save(User.builder()
                .name("Target User")
                .email("target@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        String userToken = jwtService.generateToken(normalUser);

        // 3. ارسال درخواست DELETE توسط کاربر عادی -> انتظار 403 Forbidden
        mockMvc.perform(delete("/api/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin user can delete other users - Should return 204 No Content")
    void adminUser_CanDeleteOtherUsers_ShouldReturnNoContent() throws Exception {
        // 1. ایجاد کاربر ادمین (درخواست‌دهنده)
        User adminUser = userRepository.save(User.builder()
                .name("Admin User")
                .email("admin@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_ADMIN)
                .build());

        // 2. ایجاد کاربر هدف برای حذف
        User targetUser = userRepository.save(User.builder()
                .name("Target User To Delete")
                .email("target.delete@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        String adminToken = jwtService.generateToken(adminUser);

        // 3. ارسال درخواست DELETE توسط ادمین -> انتظار 204 No Content
        mockMvc.perform(delete("/api/users/" + targetUser.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
