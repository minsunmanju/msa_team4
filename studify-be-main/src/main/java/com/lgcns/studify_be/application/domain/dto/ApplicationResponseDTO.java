package com.lgcns.studify_be.application.domain.dto;

import java.time.LocalDateTime;

import com.lgcns.studify_be.application.domain.entity.ApplicationEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ApplicationResponseDTO {
    private Long applicationId;
    private Long postId;
    private Long applicantId;
    private String applicantName;
    private String status;
    private LocalDateTime appliedAt;

    public static ApplicationResponseDTO fromEntity(ApplicationEntity entity) {
        return ApplicationResponseDTO.builder()
                .applicationId(entity.getApplicationId())
                .postId(entity.getPost().getPostId())
                .applicantId(entity.getApplicant().getId())
                .applicantName(entity.getApplicant().getNickname())
                .status(entity.getStatus().getValue())
                .appliedAt(entity.getAppliedAt())
                .build();
    }
}
