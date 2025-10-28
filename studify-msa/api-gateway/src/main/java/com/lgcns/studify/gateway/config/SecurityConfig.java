package com.lgcns.studify.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/studify/**").permitAll()
                .pathMatchers("/api/v1/posts/**").permitAll()  // 게시글 API 허용
                .pathMatchers("/api/v1/post/**").permitAll()   // 게시글 API 허용
                .pathMatchers("/api/v1/users/**").permitAll()  // 회원가입 허용
                .pathMatchers("/api/v1/auth/**").permitAll()   // 인증 허용
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().permitAll()  // 모든 요청 허용 (JWT 필터에서 인증 처리)
            );

        return http.build();
    }
}
