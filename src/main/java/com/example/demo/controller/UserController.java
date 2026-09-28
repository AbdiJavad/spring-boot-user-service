package com.example.demo.controller;

import com.example.demo.dto.UserDTO;
import com.example.demo.dto.UserRegistrationDto;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User createdUser = userService.registerUser(registrationDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.fromEntity(createdUser));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Principal principal) {
        // Ã˜Â­Ã˜Â§Ã™â€žÃ˜Â§ Ã˜Â§Ã˜Â² Ã˜Â·Ã˜Â±Ã›Å’Ã™â€š Ã˜Â§Ã›Å’Ã™â€¦Ã›Å’Ã™â€žÃ™Â Ã™â€¦Ã™Ë†Ã˜Â¬Ã™Ë†Ã˜Â¯ Ã˜Â¯Ã˜Â± Ã˜ÂªÃ™Ë†ÃšÂ©Ã™â€  (Principal)Ã˜Å’ Ã˜Â¯Ã›Å’Ã˜ÂªÃ˜Â§Ã›Å’ Ã˜ÂªÃ˜Â§Ã˜Â²Ã™â€¡ Ã˜Â±Ã˜Â§ Ã˜Â§Ã˜Â² Ã˜Â¯Ã›Å’Ã˜ÂªÃ˜Â§Ã˜Â¨Ã›Å’Ã˜Â³ Ã™â€¦Ã›Å’Ã¢â‚¬Å’ÃšÂ¯Ã›Å’Ã˜Â±Ã›Å’Ã™â€¦
        User currentUser = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(UserResponseDto.fromEntity(currentUser));
    }

    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(Pageable pageable) {
        Page<UserResponseDto> page = userService.getAllUsers(pageable)
                .map(UserResponseDto::fromEntity);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserResponseDto.fromEntity(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        User updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(UserResponseDto.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
