package com.lgcns.studify_be.post.domain.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.lgcns.studify_be.comment.domain.dto.CommentResponseDTO;
import com.lgcns.studify_be.post.domain.entity.Position;
import com.lgcns.studify_be.post.domain.entity.PostEntity;

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
    private List<CommentResponseDTO> comments;
    private Integer commentCount;
    private Long authorId;
    private String nickname;

    public static PostResponseDTO fromEntity(PostEntity post) {
        String categoryValue = post.getCategory() != null ? post.getCategory().getValue() : null;
        String statusValue = post.getStatus() != null ? post.getStatus().getValue() : null;
        List<String> positionValues = post.getPosition() != null
                ? post.getPosition().stream().map(Position::getValue).collect(Collectors.toList())
                : null;
        
        List<CommentResponseDTO> commentDTOs = post.getComments() != null ?
                post.getComments().stream()
                    .map(CommentResponseDTO::fromEntity)
                    .toList() :
                new ArrayList<>();


        Integer commentCount = post.getComments() != null ? post.getComments().size() : 0;

        return PostResponseDTO.builder()
                            .authorId(post.getAuthor().getId())
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
                            .comments(commentDTOs)
                            .commentCount(commentCount)
                            .nickname(post.getAuthor().getNickname())
                            .build();
    }
}
