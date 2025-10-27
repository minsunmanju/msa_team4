package com.lgcns.studify.user.entity;

import com.lgcns.studify.common.BaseTimeEntity;
import com.lgcns.studify.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

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
}