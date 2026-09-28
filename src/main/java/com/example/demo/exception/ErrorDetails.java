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

    // GetterÃ™â€¡Ã˜Â§ (Swagger Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ Ã˜ÂªÃ™Ë†Ã™â€žÃ›Å’Ã˜Â¯ Ã™â€¦Ã˜Â³Ã˜ÂªÃ™â€ Ã˜Â¯Ã˜Â§Ã˜Âª Ã˜Â¨Ã™â€¡ Ã˜Â§Ã›Å’Ã™â€ Ã¢â‚¬Å’Ã™â€¡Ã˜Â§ Ã™â€ Ã›Å’Ã˜Â§Ã˜Â² Ã˜Â¯Ã˜Â§Ã˜Â±Ã˜Â¯)
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
}
