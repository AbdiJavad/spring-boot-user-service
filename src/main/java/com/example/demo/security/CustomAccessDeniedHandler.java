package com.example.demo.security;

import com.example.demo.exception.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        // استفاده از ObjectMapper سیستم یا ثبت ماژول زمان جاوا برای سریالایز Instant
        this.objectMapper = objectMapper.copy().registerModule(new JavaTimeModule());
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/access-denied")
                .title("Forbidden")
                .status(HttpServletResponse.SC_FORBIDDEN)
                .detail("You do not have permission to access this resource.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
