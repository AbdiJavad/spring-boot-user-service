package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            System.out.println("--> Login Versuch für Email: " + request.email());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
            String token = jwtService.generateToken(userDetails);

            System.out.println("--> Token erfolgreich generiert!");
            return ResponseEntity.ok(new AuthResponse(token));

        } catch (BadCredentialsException ex) {
            System.out.println("--> Fehler: Falsches Passwort oder Benutzer nicht gefunden!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Ungültige E-Mail oder Passwort");
        } catch (Exception ex) {
            System.out.println("--> Unerwarteter Fehler: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Fehler: " + ex.getMessage());
        }
    }
}
