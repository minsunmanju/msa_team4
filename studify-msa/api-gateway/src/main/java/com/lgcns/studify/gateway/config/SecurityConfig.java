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
                .pathMatchers("/api/v1/posts/**").permitAll()
                .pathMatchers("/api/v1/post/**").permitAll()
                .pathMatchers("/api/v1/users/**").permitAll()  // 회원가입 허용
                .pathMatchers("/api/v1/auth/**").permitAll()   // 인증 허용
                .pathMatchers("/api/auth/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
            );

        return http.build();
    }
}
