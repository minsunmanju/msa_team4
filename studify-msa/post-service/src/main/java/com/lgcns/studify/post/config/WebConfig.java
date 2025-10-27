package com.lgcns.studify.post.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://msateam4.vercel.app") // 프론트 배포 주소
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

