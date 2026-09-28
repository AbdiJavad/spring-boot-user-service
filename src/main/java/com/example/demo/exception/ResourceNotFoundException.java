package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus Ã˜Â±Ã˜Â§ Ã˜Â­Ã˜Â°Ã™Â ÃšÂ©Ã™â€ ! Ã˜Â§Ã›Å’Ã™â€  Ã˜Â¯Ã™â€žÃ›Å’Ã™â€ž Ã˜Â§Ã˜Â±Ã™Ë†Ã˜Â± Ã›ÂµÃ›Â°Ã›Â° Ã˜Â§Ã˜Â³Ã˜Âª.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}