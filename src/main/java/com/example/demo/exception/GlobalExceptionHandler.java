package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Ø®Ø·Ø§ÛŒ Ø§Ø¹ØªØ¨Ø§Ø±Ø³Ù†Ø¬ÛŒ ÙˆØ±ÙˆØ¯ÛŒâ€ŒÙ‡Ø§ (Validation Errors - 400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Ø³Ø§Ø®Øª Ù„ÛŒØ³Øª Ø®Ø·Ø§Ù‡Ø§ÛŒ Ø§Ø¹ØªØ¨Ø§Ø±Ø³Ù†Ø¬ÛŒ Ø¨Ù‡ ØµÙˆØ±Øª Ø¢Ø±Ø§ÛŒÙ‡â€ŒØ§ÛŒ Ø§Ø² Ø¢Ø¨Ø¬Ú©Øªâ€ŒÙ‡Ø§ (Ø§Ø³ØªØ§Ù†Ø¯Ø§Ø±Ø¯ RFC 9457)
        List<Map<String, String>> invalidParams = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> param = new LinkedHashMap<>();
            param.put("name", error.getField());
            param.put("reason", error.getDefaultMessage());
            invalidParams.add(param);
        }

        Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put("invalidParams", invalidParams);
        additionalProps.put("path", request.getRequestURI());

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/validation-failed")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Your request parameters didn't validate.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .additionalProperties(additionalProps)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(errorResponse);
    }



    // 2. Ø®Ø·Ø§ÛŒ Ø¹Ø¯Ù… Ø¯Ø³ØªØ±Ø³ÛŒ Ùˆ Ù…Ø¬ÙˆØ² Ù†Ø§Ú©Ø§ÙÛŒ (403 Forbidden - Role/Permission Denial)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/access-denied")
                .title("Forbidden")
                .status(HttpStatus.FORBIDDEN.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "You do not have permission to access this resource.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(errorResponse);
    }

    // 3. Ø®Ø·Ø§ÛŒ Ø¹Ø¯Ù… Ø§Ø­Ø±Ø§Ø² Ù‡ÙˆÛŒØª / ØªÙˆÚ©Ù† Ù†Ø§Ù…Ø¹ØªØ¨Ø± (401 Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/unauthorized")
                .title("Unauthorized")
                .status(HttpStatus.UNAUTHORIZED.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "Full authentication is required to access this resource.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(errorResponse);
    }

    // 4. Ø®Ø·Ø§ÛŒ Ø¹Ø¯Ù… ÛŒØ§ÙØªÙ† Ù…Ù†Ø¨Ø¹ (Resource Not Found - 404 Not Found)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/resource-not-found")
                .title("Resource Not Found")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(errorResponse);
    }

    // 5. Ø®Ø·Ø§ÛŒ ØªØ¯Ø§Ø®Ù„/ØªÚ©Ø±Ø§Ø±ÛŒ Ø¨ÙˆØ¯Ù† Ø§ÛŒÙ…ÛŒÙ„ (Conflict - 409 Conflict)
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException ex,
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/email-conflict")
                .title("Email Already Exists")
                .status(HttpStatus.CONFLICT.value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(errorResponse);
    }

    // 6. Ù…Ø¯ÛŒØ±ÛŒØª Ø³Ø§ÛŒØ± Ø®Ø·Ø§Ù‡Ø§ÛŒ Ù¾ÛŒØ´â€ŒØ¨ÛŒÙ†ÛŒâ€ŒÙ†Ø´Ø¯Ù‡ Ø³Ø±ÙˆØ± (Internal Server Error - 500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                .type("https://api.example.com/errors/internal-server-error")
                .title("Internal Server Error")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(ex.getMessage() != null ? ex.getMessage() : "An unexpected internal server error occurred.")
                .instance(request.getRequestURI())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.parseMediaType("application/problem+json"))
                .body(errorResponse);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        if ("Refresh token not found".equals(ex.getMessage()) || (ex.getMessage() != null && ex.getMessage().contains("Refresh token"))) {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .timestamp(Instant.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .title(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .detail(ex.getMessage())
                    .instance(request.getRequestURI())
                    .type("about:blank")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        ApiErrorResponse error = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .title(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .type("about:blank")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }



}
