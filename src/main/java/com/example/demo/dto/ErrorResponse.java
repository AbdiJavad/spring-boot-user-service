package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Ã˜Â§Ã˜Â³Ã˜ÂªÃ˜Â§Ã™â€ Ã˜Â¯Ã˜Â§Ã˜Â±Ã˜Â¯ Ã™Â¾Ã˜Â§Ã˜Â³Ã˜Â® Ã˜Â®Ã˜Â·Ã˜Â§ Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ Ã˜ÂªÃ™â€¦Ã˜Â§Ã™â€¦Ã›Å’ Ã˜Â¨Ã˜Â®Ã˜Â´Ã¢â‚¬Å’Ã™â€¡Ã˜Â§Ã›Å’ Ã˜Â³Ã›Å’Ã˜Â³Ã˜ÂªÃ™â€¦.
 * Ã˜Â§Ã›Å’Ã™â€  Ã˜Â³Ã˜Â§Ã˜Â®Ã˜ÂªÃ˜Â§Ã˜Â± Ã˜Â¨Ã˜Â§Ã˜Â¹Ã˜Â« Ã™â€¦Ã›Å’Ã¢â‚¬Å’Ã˜Â´Ã™Ë†Ã˜Â¯ ÃšÂ©Ã™â€žÃ˜Â§Ã›Å’Ã™â€ Ã˜Âª Ã™â€¡Ã™â€¦Ã›Å’Ã˜Â´Ã™â€¡ Ã˜Â¨Ã˜Â¯Ã˜Â§Ã™â€ Ã˜Â¯ Ã˜Â¨Ã˜Â§ Ãšâ€ Ã™â€¡ Ã™ÂÃ˜Â±Ã™â€¦Ã˜ÂªÃ›Å’ Ã˜Â±Ã™Ë†Ã˜Â¨Ã˜Â±Ã™Ë† Ã˜Â§Ã˜Â³Ã˜Âª.
 */
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
