package com.lgcns.studify.post.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.lgcns.studify.post.domain.dto.PostRequestDTO;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "posts")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    private Category category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Integer recruitmentCount;

    @ElementCollection
    @CollectionTable(name = "post_tech_stack", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tech")
    private List<String> techStack;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(nullable = false, length = 255)
    private String meetingType;

    @Column(nullable = false, length = 255)
    private String duration;

    @ElementCollection
    @CollectionTable(
        name = "post_positions",
        joinColumns = @JoinColumn(name = "post_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    @Builder.Default
    private List<Position> position = new ArrayList<>();

    // MSA에서는 User Entity와 직접 연결하지 않고 userId만 저장
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    public void update(PostRequestDTO request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.updatedAt = LocalDateTime.now();
        this.recruitmentCount = request.getRecruitmentCount();
        this.techStack = request.getTechStack();
        this.deadline = request.getDeadline();
        this.meetingType = request.getMeetingType();
        this.duration = request.getDuration();

        if (request.getCategory() != null) {
            this.category = Category.from(request.getCategory());
        }
        if (request.getStatus() != null) {
            this.status = PostStatus.from(request.getStatus());
        }

        if (request.getPosition() != null) {
            List<Position> positions = request.getPosition().stream()
                                               .map(Position::from)
                                               .toList();
            this.position = new ArrayList<>(positions);
        }
    }
}