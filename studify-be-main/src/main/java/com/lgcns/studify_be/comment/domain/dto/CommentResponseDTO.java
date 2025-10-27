package com.lgcns.studify_be.comment.domain.dto;

import java.time.LocalDateTime;

import com.lgcns.studify_be.comment.domain.entity.CommentEntity;
import com.lgcns.studify_be.user.dto.AuthorDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CommentResponseDTO {
    private Long commentId;
    private String content;
    private AuthorDTO author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponseDTO fromEntity(CommentEntity comment) {
        return CommentResponseDTO.builder()
                            .commentId(comment.getCommentId())
                            .content(comment.getContent())
                            .author(AuthorDTO.fromEntity(comment.getUser()))
                            .createdAt(comment.getCreatedAt())
                            .updatedAt(comment.getUpdatedAt())
                            .build();
    }
}
