package com.lgcns.studify_be.auth;

import com.lgcns.studify_be.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Refresh Token JPA 리포지토리
 * - 유저 기준/토큰 기준 조회 및 삭제 제공
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** 유저 기준으로 토큰 조회 (유저당 1개 정책이라 Optional 단건) */
    Optional<RefreshToken> findByUser(User user);

    /** 토큰 문자열로 조회 (refresh API에서 사용) */
    Optional<RefreshToken> findByToken(String token);

    /** 로그아웃 시 유저 기준으로 삭제 */
    void deleteByUser(User user);
}
