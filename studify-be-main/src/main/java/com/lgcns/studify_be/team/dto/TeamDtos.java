package com.lgcns.studify_be.team.dto;


import jakarta.validation.constraints.*;

public class TeamDtos {

    // 생성 요청 DTO
    public record Create(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 200) String description
    ) {}

    // 수정 요청 DTO
    public record Update(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 200) String description
    ) {}

    // 응답 DTO
    public record Response(
        Long id,
        String name,
        String description,
        String createdAt,
        String updatedAt
    ) {}
}
