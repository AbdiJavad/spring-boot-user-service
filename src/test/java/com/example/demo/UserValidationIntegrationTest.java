package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("User Validation Integration Tests (RFC 7807)")
class UserValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("POST /api/users/register - Validation Scenarios")
    class RegistrationValidationTests {

        @Test
        @DisplayName("Should return 400 with invalidParams when fields are blank or invalid")
        void shouldReturnProblemDetailWhenValidationFails() throws Exception {
            Map<String, String> invalidPayload = new HashMap<>();
            invalidPayload.put("name", "A");
            invalidPayload.put("email", "invalid-email-format");
            invalidPayload.put("password", "123");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidPayload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(header().string("Content-Type", startsWith("application/problem+json")))
                    .andExpect(jsonPath("$.type", is("https://api.example.com/errors/validation-failed")))
                    .andExpect(jsonPath("$.title", is("Validation Failed")))
                    .andExpect(jsonPath("$.status", is(400)))
                    .andExpect(jsonPath("$.detail", is("Your request parameters didn't validate.")))
                    .andExpect(jsonPath("$.path", is("/api/users/register")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()))
                    .andExpect(jsonPath("$.invalidParams", hasSize(3)))
                    .andExpect(jsonPath("$.invalidParams[*].name", containsInAnyOrder("name", "email", "password")));
        }

        @Test
        @DisplayName("Should return 400 when required fields are null or empty")
        void shouldReturnProblemDetailWhenRequiredFieldsAreEmpty() throws Exception {
            Map<String, String> emptyPayload = Map.of(
                    "name", "",
                    "email", "",
                    "password", ""
            );

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(emptyPayload)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.invalidParams", not(empty())));
        }

        @Test
        @DisplayName("Should return 201 Created when payload is valid")
        void shouldReturnCreatedWhenPayloadIsValid() throws Exception {
            Map<String, String> validPayload = Map.of(
                    "name", "Max Mustermann",
                    "email", "max.mustermann@example.de",
                    "password", "SecurePassword123!"
            );

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPayload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email", is("max.mustermann@example.de")))
                    .andExpect(jsonPath("$.name", is("Max Mustermann")));
        }
    }
}
