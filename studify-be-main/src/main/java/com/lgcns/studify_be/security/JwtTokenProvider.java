package com.lgcns.studify_be.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final Key key;
    private final long accessValidity;
    private final long refreshValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessValidity,
            @Value("${jwt.refresh-token-validity}") long refreshValidity
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }

    public String createAccessToken(String subject) {
        return createToken(subject, accessValidity);
    }

    public String createRefreshToken(String subject) {
        return createToken(subject, refreshValidity);
    }

    private String createToken(String subject, long validity) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validity);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // ✅ 추가: 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 파싱 성공 → 유효
        } catch (ExpiredJwtException e) {
            System.out.println("토큰 만료: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("토큰 유효하지 않음: " + e.getMessage());
            return false;
        }
    }

    // ✅ 추가: 토큰 만료일자 확인
    public Date getExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
