package com.lgcns.studify_be.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService; // 아래 3-4에서 만듦

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        System.out.println("=== JWT Filter 시작 ===");
        System.out.println("Request URI: " + requestURI);
        System.out.println("HTTP Method: " + method);

        String token = resolveToken(request);
        System.out.println("Token 추출 결과: " + (token != null ? "토큰 있음 (길이: " + token.length() + ")" : "토큰 없음"));

        if (token != null) {
            boolean isValid = jwtTokenProvider.validateToken(token);
            System.out.println("Token 검증 결과: " + isValid);

            if (isValid) {
                String username = jwtTokenProvider.getSubject(token);
                System.out.println("Token에서 추출한 username: " + username);

                try {
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    System.out.println("UserDetails 조회 성공: " + user.getUsername());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    System.out.println("인증 설정 완료");
                } catch (Exception e) {
                    System.out.println("UserDetails 조회 실패: " + e.getMessage());
                }
            } else {
                System.out.println("유효하지 않은 토큰");
            }
        }
        System.out.println("=== JWT Filter 종료 ===");
        
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        System.out.println("Authorization Header: " + bearer);
        System.out.println("Authorization Header: " + bearer);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            String token = bearer.substring(7);
            System.out.println("추출된 토큰 앞 20글자: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
            return token;
        }
        System.out.println("Bearer 토큰 형식이 아님");
        return null;
    }
}