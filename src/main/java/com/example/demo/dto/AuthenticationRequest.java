package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
        @NotBlank(message = "E-Mail darf nicht leer sein")
        @Email(message = "Bitte eine gültige E-Mail-Adresse eingeben")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        String password
) {
}
