package com.lgcns.studify_be.post.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.lgcns.studify_be.application.domain.entity.ApplicationEntity;
import com.lgcns.studify_be.comment.domain.entity.CommentEntity;
import com.lgcns.studify_be.post.domain.dto.PostRequestDTO;
import com.lgcns.studify_be.user.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PostEntity {
    
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ApplicationEntity> applications = new ArrayList<>();

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
                                           .map(Position::from) // Position enum에 from(String) 메서드 필요
                                           .toList();
        this.position = new ArrayList<>(positions);
        }
    }
}