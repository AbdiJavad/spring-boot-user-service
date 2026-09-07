package com.example.demo.dto;

import com.example.demo.model.Role;
import com.example.demo.model.User;

public record UserResponseDto(
        Long id,
        String name,
        String email,
        Role role
) {
    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}