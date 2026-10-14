package com.example.demo.ratelimit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "rate-limiter.enabled=true",
        "rate-limiter.capacity=3",
        "rate-limiter.duration-minutes=1"
})
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should allow requests within limit and return 429 when capacity exceeded")
    void shouldEnforceRateLimitWhenCapacityExceeded() throws Exception {
        String testIp = "192.168.1.100";

        // ارسال ۳ درخواست اول (باید مجاز باشند و از فیلتر عبور کنند)
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", testIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"invalid@test.com\",\"password\":\"wrong\"}"))
                    .andExpect(header().exists("X-Rate-Limit-Remaining"));
        }

        // درخواست چهارم باید با 429 مسدود شود
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", testIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid@test.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("X-Rate-Limit-Retry-After-Seconds"))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message", containsString("Try again in")));
    }

    @Test
    @DisplayName("Should isolate rate limits between different IP addresses")
    void shouldIsolateRateLimitsPerIp() throws Exception {
        String ipBlocked = "10.0.0.1";
        String ipAllowed = "10.0.0.2";

        // مصرف تمام ظرفیت IP اول
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", ipBlocked)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"));
        }

        // IP اول اکنون مسدود است
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", ipBlocked)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"))
                .andExpect(status().isTooManyRequests());

        // اما IP دوم هنوز باید ظرفیت داشته باشد و مسدود نشود
        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", ipAllowed)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@test.com\",\"password\":\"pass\"}"))
                .andExpect(header().string("X-Rate-Limit-Remaining", "2"));
    }

    @Test
    @DisplayName("Should not rate limit whitelisted endpoints like Actuator health")
    void shouldBypassRateLimitForWhitelistedEndpoints() throws Exception {
        String testIp = "172.16.0.50";

        // حتی اگر بیش از ظرفیت به /actuator/health ریکوئست بزنیم، نباید مسدود شود
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/actuator/health")
                            .header("X-Forwarded-For", testIp))
                    .andExpect(status().isOk());
        }
    }
}
