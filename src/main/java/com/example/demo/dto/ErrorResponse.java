package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * استاندارد پاسخ خطا برای تمامی بخش‌های سیستم.
 * این ساختار باعث می‌شود کلاینت همیشه بداند با چه فرمتی روبرو است.
 */
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}
