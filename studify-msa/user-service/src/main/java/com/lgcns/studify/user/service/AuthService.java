package com.lgcns.studify.user.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lgcns.studify.user.entity.RefreshToken;
import com.lgcns.studify.user.entity.User;
import com.lgcns.studify.user.repository.RefreshTokenRepository;
import com.lgcns.studify.user.repository.UserRepository;
import com.lgcns.studify.user.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse login(String email, String password) {
        // 1) 사용자 인증
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        // 2) 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshTokenStr = jwtTokenProvider.createRefreshToken(email);

        // 3) 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4) Refresh Token 저장 (유저당 1개 → 있으면 업데이트)
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder().user(user).build());
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7일 유효
        refreshTokenRepository.save(refreshToken);

        // 5) 반환
        return new TokenResponse(accessToken, refreshTokenStr);
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenStr) {
        // 1) Refresh Token 값으로 조회
        RefreshToken saved = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));

        // 2) 만료 체크
        if (saved.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 3) 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(saved.getUser().getEmail());

        return new TokenResponse(newAccessToken, saved.getToken());
    }

    @Transactional
    public void logout(String accessToken) {
        // 1) 토큰 subject(이메일) 추출
        String email = jwtTokenProvider.getSubject(accessToken);

        // 2) 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 3) Refresh Token 삭제
        refreshTokenRepository.deleteByUser(user);
    }

    public static class TokenResponse {
        private final String accessToken;
        private final String refreshToken;

        public TokenResponse(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }
    }
}