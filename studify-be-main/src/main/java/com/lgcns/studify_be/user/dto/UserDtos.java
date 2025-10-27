package com.lgcns.studify_be.user.dto;

import com.lgcns.studify_be.user.UserStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class UserDtos {
    public record Create(
            @Email @NotBlank String email,
            @NotBlank @Size(min=8, max=64) String password,
            @Size(max=50) String nickname
    ) {}
    public record Update(
            @Size(max=50) String nickname,
            UserStatus status,
            @Size(min=8, max=64) String password
    ) {}
    public record Response(
            Long id, String email, String nickname, UserStatus status,
            LocalDateTime lastLoginAt, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}
}
