package com.example.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource; // <--- این Import را اضافه کن
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "management.endpoints.web.exposure.include=health,info,metrics")
class ActuatorSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should block unauthenticated access to /actuator/health")
    void whenUnauthenticated_thenAccessToActuatorShouldBeBlocked() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isUnauthorized()); // یا isForbidden بسته به تنظیمات AuthenticationEntryPoint
    }

    @Test
    @DisplayName("Should forbid USER role from accessing /actuator/health")
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserRole_thenAccessToActuatorShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role to access /actuator/health")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminRole_thenAccessToActuatorShouldBeAllowed() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should forbid USER role from accessing /actuator/metrics")
    @WithMockUser(username = "user", roles = {"USER"})
    void whenUserRole_thenAccessToMetricsShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should allow ADMIN role to access /actuator/metrics")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminRole_thenAccessToMetricsShouldBeAllowed() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
    }
}
