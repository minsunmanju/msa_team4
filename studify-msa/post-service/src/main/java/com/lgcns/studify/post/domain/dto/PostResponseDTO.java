package com.lgcns.studify.post.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.lgcns.studify.post.domain.entity.Position;
import com.lgcns.studify.post.domain.entity.Post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostResponseDTO {
    
    private Long postId;
    private String title;
    private String content;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer recruitmentCount;
    private List<String> techStack;
    private LocalDateTime deadline;
    private String status;
    private String meetingType;
    private String duration;
    private List<String> position;
    private Long authorId;
    private String nickname; // User Service에서 조회해야 함

    public static PostResponseDTO fromEntity(Post post) {
        String categoryValue = post.getCategory() != null ? post.getCategory().getValue() : null;
        String statusValue = post.getStatus() != null ? post.getStatus().getValue() : null;
        List<String> positionValues = post.getPosition() != null
                ? post.getPosition().stream().map(Position::getValue).collect(Collectors.toList())
                : null;

        return PostResponseDTO.builder()
                            .authorId(post.getAuthorId())
                            .postId(post.getPostId())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .category(categoryValue)
                            .createdAt(post.getCreatedAt())
                            .updatedAt(post.getUpdatedAt())
                            .recruitmentCount(post.getRecruitmentCount())
                            .techStack(post.getTechStack())
                            .deadline(post.getDeadline())
                            .status(statusValue)
                            .meetingType(post.getMeetingType())
                            .duration(post.getDuration())
                            .position(positionValues)
                            .build();
    }
}