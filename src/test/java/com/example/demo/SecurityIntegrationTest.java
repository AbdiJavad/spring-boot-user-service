package com.example.demo;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.ratelimit.RateLimitFilter;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class SecurityIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

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
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() throws Exception {
        // فعال‌سازی کامل زنجیره امنیتی و متد سکیوریتی روی MockMvc
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        SecurityContextHolder.clearContext();

        // ۱. پاکسازی داده‌های وابسته برای جلوگیری از نقض کلید خارجی
        try {
            jdbcTemplate.execute("DELETE FROM refresh_tokens");
        } catch (Exception ignored) {
        }
        userRepository.deleteAll();

        // ۲. عبور ترافیک از فیلتر RateLimit به لایه‌های بعدی
        doAnswer(invocation -> {
            ServletRequest req = invocation.getArgument(0);
            ServletResponse res = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(rateLimitFilter).doFilter(any(), any(), any());
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
                .andExpect(status().isCreated())
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
        UserRegistrationDto registerDto = new UserRegistrationDto("Jovan Admin", "jovan.auth@example.com", "Password123!");
        userService.registerUser(registerDto);

        LoginRequest loginDto = new LoginRequest("jovan.auth@example.com", "Password123!");

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
    @WithMockUser(username = "normal@example.com", roles = {"USER"})
    void normalUser_CannotDeleteOtherUsers_ShouldReturnForbidden() throws Exception {
        User targetUser = userRepository.save(User.builder()
                .name("Target User")
                .email("target@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        mockMvc.perform(delete("/api/users/" + targetUser.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Admin user can delete other users - Should return 204 No Content")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void adminUser_CanDeleteOtherUsers_ShouldReturnNoContent() throws Exception {
        User targetUser = userRepository.save(User.builder()
                .name("Target User To Delete")
                .email("target.delete@example.com")
                .password(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build());

        mockMvc.perform(delete("/api/users/" + targetUser.getId()))
                .andExpect(status().isNoContent());
    }
}
