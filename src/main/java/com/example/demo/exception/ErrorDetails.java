package com.example.demo.exception;

import java.time.LocalDateTime;

public class ErrorDetails {
    private LocalDateTime timestamp;
    private String message;
    private int status;

    public ErrorDetails(LocalDateTime timestamp, String message, int status) {
        this.timestamp = timestamp;
        this.message = message;
        this.status = status;
    }

    // GetterÙ‡Ø§ (Swagger Ø¨Ø±Ø§ÛŒ ØªÙˆÙ„ÛŒØ¯ Ù…Ø³ØªÙ†Ø¯Ø§Øª Ø¨Ù‡ Ø§ÛŒÙ†â€ŒÙ‡Ø§ Ù†ÛŒØ§Ø² Ø¯Ø§Ø±Ø¯)
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
}
