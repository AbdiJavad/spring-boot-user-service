package com.example.demo.dto; // یا پکیج مربوط به DTOهای شما

import com.example.demo.model.Role;
import com.example.demo.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private Role role; // <-- فیلد نقش

    public static UserResponseDto fromEntity(User user) {
     return UserResponseDto.builder()
                             .id(user.getId())
                             .name(user.getName())
                             .email(user.getEmail())
                           .role(user.getRole())
                            .build();

    }
}
