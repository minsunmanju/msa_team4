package com.lgcns.studify_be.auth;

import com.lgcns.studify_be.security.JwtTokenProvider;
import com.lgcns.studify_be.user.User;
import com.lgcns.studify_be.user.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // 🔥 리프레시 토큰 저장소

    /**
     * 로그인 → Access/Refresh 토큰 발급 + RefreshToken DB 저장
     */
    @PostMapping("/login")
    public ResponseEntity<TokenRes> login(@RequestBody LoginReq req) {
        // 1) 사용자 인증
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // 2) 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(req.getEmail());
        String refreshTokenStr = jwtTokenProvider.createRefreshToken(req.getEmail());

        // 3) 유저 조회
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 4) Refresh Token 저장 (유저당 1개 → 있으면 업데이트)
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
                .orElse(RefreshToken.builder().user(user).build());
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 예시: 7일 유효
        refreshTokenRepository.save(refreshToken);

        // 5) 반환
        return ResponseEntity.ok(new TokenRes(accessToken, refreshTokenStr));
    }

    /**
     * Refresh Token → Access Token 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshReq req) {
        // 1) Refresh Token 값으로 조회
        RefreshToken saved = refreshTokenRepository.findByToken(req.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰"));

        // 2) 만료 체크
        if (saved.getExpiryDate().isBefore(LocalDateTime.now())) {
            return ResponseEntity.status(401).body("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 3) 새 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(saved.getUser().getEmail());

        return ResponseEntity.ok(new TokenRes(newAccessToken, saved.getToken()));
    }

    /**
     * 로그아웃 → 해당 유저의 Refresh Token 삭제
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        // 1) 헤더 검사
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }

        // 2) 액세스 토큰 추출
        String accessToken = authHeader.substring("Bearer ".length());

        // 3) 토큰 subject(이메일) 추출
        String email = jwtTokenProvider.getSubject(accessToken);

        // 4) 유저 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 5) Refresh Token 삭제
        refreshTokenRepository.deleteByUser(user);

        return ResponseEntity.ok("로그아웃 완료: 리프레시 토큰 삭제됨");
    }

    // ---------------- DTO ----------------

    @Data
    static class LoginReq {
        private String email;
        private String password;
    }

    @Data
    static class RefreshReq {
        private String refreshToken;
    }

    @Data
    static class TokenRes {
        private final String accessToken;
        private final String refreshToken;
    }
}
