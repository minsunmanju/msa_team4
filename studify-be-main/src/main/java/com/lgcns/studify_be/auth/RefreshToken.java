package com.lgcns.studify_be.auth;

import com.lgcns.studify_be.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 * - "유저당 1개" 정책: 같은 유저에 대해 하나만 저장되도록 user_id를 UNIQUE로 강제
 * - 로그인 시 발급/저장, 로그아웃 시 삭제, /refresh 시 검증에 사용
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 토큰 주인 (유저 1 : 토큰 1) */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true) // ★ 유저당 1개 보장
    private User user;

    /** 실제 Refresh Token 값 */
    @Column(nullable = false, length = 512)
    private String token;

    /** 토큰 만료 시각 (서버 정책에 맞게 설정) */
    @Column(nullable = false)
    private LocalDateTime expiryDate;
}
