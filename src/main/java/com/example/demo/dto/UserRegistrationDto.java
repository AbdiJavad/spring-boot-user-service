package com.example.demo.dto;

import com.example.demo.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDto(
    @NotBlank(message = "Name cannot be blank") 
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    String name,

    @NotBlank(message = "Email cannot be blank") 
    @Email(message = "Invalid email format") 
    String email,

    @NotBlank(message = "Password cannot be blank") 
    @Size(min = 6, message = "Password must be at least 6 characters") 
    String password,

    Role role
) {
    public UserRegistrationDto(String name, String email, String password) {
        this(name, email, password, null);
    }
}