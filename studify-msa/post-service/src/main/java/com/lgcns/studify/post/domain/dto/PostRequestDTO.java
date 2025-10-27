package com.lgcns.studify.post.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.lgcns.studify.post.domain.entity.Category;
import com.lgcns.studify.post.domain.entity.Position;
import com.lgcns.studify.post.domain.entity.Post;
import com.lgcns.studify.post.domain.entity.PostStatus;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class PostRequestDTO {
    
    private String title;
    private String content;

    @Schema(description = "모집글 카테고리(project | study)", example = "project")
    private String category;
    private Integer recruitmentCount;

    @Schema(description = "기술 스택", example = "[\"Spring\", \"JPA\"]")
    private List<String> techStack;
    
    @Schema(description = "모집글 상태(open | closed)", example = "open")
    private String status;
    private LocalDateTime deadline;
    private String meetingType;
    private String duration;
    
    @Schema(description = "포지션(be | fe | pm | designer | ai | android | ios | web)", example = "[\"be\", \"pm\"]")
    private List<String> position;
    private Long authorId;

    public Post toEntity(Long authorId) {
        return Post.builder()
                        .authorId(authorId)
                        .title(this.title)
                        .content(this.content)
                        .category(Category.from(this.category))
                        .recruitmentCount(this.recruitmentCount)
                        .techStack(this.techStack)
                        .status(PostStatus.from(this.status))
                        .deadline(this.deadline)
                        .meetingType(this.meetingType)
                        .duration(this.duration)
                        .position(Position.fromList(this.position))
                        .build();
    }
}