package com.lgcns.studify_be.user;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.lgcns.studify_be.application.domain.entity.ApplicationEntity;
import com.lgcns.studify_be.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "users",
    indexes = { @Index(name = "uk_users_email", columnList = "email", unique = true) }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "applicant", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<ApplicationEntity> applications = new ArrayList<>();
}
