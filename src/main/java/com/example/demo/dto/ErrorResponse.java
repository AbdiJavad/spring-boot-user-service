package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Ø§Ø³ØªØ§Ù†Ø¯Ø§Ø±Ø¯ Ù¾Ø§Ø³Ø® Ø®Ø·Ø§ Ø¨Ø±Ø§ÛŒ ØªÙ…Ø§Ù…ÛŒ Ø¨Ø®Ø´â€ŒÙ‡Ø§ÛŒ Ø³ÛŒØ³ØªÙ….
 * Ø§ÛŒÙ† Ø³Ø§Ø®ØªØ§Ø± Ø¨Ø§Ø¹Ø« Ù…ÛŒâ€ŒØ´ÙˆØ¯ Ú©Ù„Ø§ÛŒÙ†Øª Ù‡Ù…ÛŒØ´Ù‡ Ø¨Ø¯Ø§Ù†Ø¯ Ø¨Ø§ Ú†Ù‡ ÙØ±Ù…ØªÛŒ Ø±ÙˆØ¨Ø±Ùˆ Ø§Ø³Øª.
 */
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
