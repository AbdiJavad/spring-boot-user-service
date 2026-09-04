package com.example.demo.dto; // یا پکیج پروژه خودت

public record UserResponseDto(
        Long id,
        String name,
        String email
) {
    // یک متد استاتیک کمکی (Mapper) برای تبدیل Entity به DTO
    public static UserResponseDto fromEntity(com.example.demo.model.User user) {
        return new UserResponseDto(
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }
}
