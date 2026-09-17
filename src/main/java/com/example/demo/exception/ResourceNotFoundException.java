package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus Ø±Ø§ Ø­Ø°Ù Ú©Ù†! Ø§ÛŒÙ† Ø¯Ù„ÛŒÙ„ Ø§Ø±ÙˆØ± ÛµÛ°Û° Ø§Ø³Øª.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}