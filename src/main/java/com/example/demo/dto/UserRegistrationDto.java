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
        @Email(message = "Bitte eine gÃ¼ltige E-Mail-Adresse eingeben")
        String email,

        @NotBlank(message = "Passwort darf nicht leer sein")
        @Size(min = 6, message = "Passwort muss mindestens 6 Zeichen lang sein")
        String password,

        Role role
) {
        // Overloaded Constructor Ø¨Ø±Ø§ÛŒ Ù¾Ø´ØªÛŒØ¨Ø§Ù†ÛŒ Ø§Ø² Û³ Ù¾Ø§Ø±Ø§Ù…ØªØ± (Ù¾ÛŒØ´â€ŒÙØ±Ø¶ Ø¨Ø¯ÙˆÙ† Role ØµØ±ÛŒØ­)
        public UserRegistrationDto(String name, String email, String password) {
                this(name, email, password, null);
        }
}
