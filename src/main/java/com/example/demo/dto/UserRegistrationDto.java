package com.example.demo.dto;

import com.example.demo.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDto(
        @NotBlank(message = "Name darf nicht leer sein")
        @Size(min = 2, max = 50, message = "Name muss zwischen 2 und 50 Zeichen lang sein")
        String name,

        @NotBlank(message = "E-Mail darf nicht leer sein")
        @Email(message = "Bitte eine gÃƒÂ¼ltige E-Mail-Adresse eingeben")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
        String password,

        Role role
) {
        // Overloaded Constructor Ã˜Â¨Ã˜Â±Ã˜Â§Ã›Å’ Ã™Â¾Ã˜Â´Ã˜ÂªÃ›Å’Ã˜Â¨Ã˜Â§Ã™â€ Ã›Å’ Ã˜Â§Ã˜Â² Ã›Â³ Ã™Â¾Ã˜Â§Ã˜Â±Ã˜Â§Ã™â€¦Ã˜ÂªÃ˜Â± (Ã™Â¾Ã›Å’Ã˜Â´Ã¢â‚¬Å’Ã™ÂÃ˜Â±Ã˜Â¶ Ã˜Â¨Ã˜Â¯Ã™Ë†Ã™â€  Role Ã˜ÂµÃ˜Â±Ã›Å’Ã˜Â­)
        public UserRegistrationDto(String name, String email, String password) {
                this(name, email, password, null);
        }
}
