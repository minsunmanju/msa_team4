package com.lgcns.studify_be.team.dto;


import jakarta.validation.constraints.*;

public record TeamUpdateRequest(
        @NotBlank(message = "팀 이름은 필수입니다.")
        @Size(max = 50, message = "팀 이름은 최대 50자까지 가능합니다.")
        String name,

        @Size(max = 200, message = "설명은 최대 200자까지 가능합니다.")
        String description,

        @NotNull(message = "ownerId는 필수입니다.")
        @Positive(message = "ownerId는 양수여야 합니다.")
        Long ownerId,

        @NotNull(message = "maxMembers는 필수입니다.")
        @Min(value = 1, message = "최소 인원은 1명입니다.")
        @Max(value = 1000, message = "최대 인원은 1000명입니다.")
        Integer maxMembers
) {}


// 여기다 묶는 이유 -> 요청 스펙을 DTO에 붙이면 컨트롤러/서비스가 더 단정해짐
// Swagger에서 필수값/형식 바로 노출 -> 프론트/QA가 테스트하기 수월