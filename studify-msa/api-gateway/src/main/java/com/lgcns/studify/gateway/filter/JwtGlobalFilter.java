package main.java.com.lgcns.studify.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
@Slf4j
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        log.info("JWT Global Filter - Processing request for path: {} method: {}", path, method);
        
        // 인증이 필요하지 않은 경로들
        if (isPublicPath(path, method)) {
            log.info("JWT Global Filter - Public path, skipping authentication: {} {}", method, path);
            return chain.filter(exchange);
        }

        // Authorization 헤더 확인
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("JWT Global Filter - Authorization header: {}", authHeader != null ? "Bearer ***" : "null");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("JWT Global Filter - Missing or invalid Authorization header for path: {}", path);
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        
        try {
            // JWT 토큰 검증
            Claims claims = validateToken(token);
            String userId = claims.get("userId", String.class);
            log.info("JWT Global Filter - Token validated successfully, userId: {}", userId);
            
            // 사용자 정보를 헤더에 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", claims.getSubject())
                .header("X-User-Id", userId)
                .build();
            
            log.info("JWT Global Filter - Added headers X-User-Email: {}, X-User-Id: {}", claims.getSubject(), userId);
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            log.error("JWT Global Filter - Token validation failed for path: {}, error: {}", path, e.getMessage());
            return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path, String method) {
        return path.contains("/auth/") || 
               path.contains("/health") ||
               path.contains("/api/v1/users/signup") ||  // 회원가입 허용
               path.contains("/api/v1/auth/login") ||    // 로그인 허용
               (path.equals("/api/v1/posts") && "GET".equals(method)) ||  // 게시글 목록 조회 허용 (GET만)
               path.contains("/ws/");
    }

    private Claims validateToken(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = "{\"error\": \"" + err + "\"}";
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return -1; // 다른 필터들보다 먼저 실행
    }
}