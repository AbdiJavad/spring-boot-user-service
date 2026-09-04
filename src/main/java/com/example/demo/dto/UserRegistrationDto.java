package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDto(
        @NotBlank(message = "Name darf nicht leer sein")
        @Size(min = 2, max = 50, message = "Name muss zwischen 2 und 50 Zeichen lang sein")
        String name,

        @NotBlank(message = "E-Mail darf nicht leer sein")
        @Email(message = "Bitte eine gültige E-Mail-Adresse eingeben")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
        String password
) {}
