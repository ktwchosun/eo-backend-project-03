package com.example.board.domain.user;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 50, message = "Nickname must be between 2 and 50 characters")
    private String nickname;

    @NotBlank(message = "Email is required")
    @Size(min = 6, max = 100, message = "Email must be between 6 and 100 characters")
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$", message = "Invalid email format")
    private String email;


    public static UserDto from(@NotNull UserEntity userEntity) {
        if (userEntity == null) {
            throw new IllegalArgumentException("UserEntity cannot be null");
        }

        return UserDto.builder()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .build();
    }
}


