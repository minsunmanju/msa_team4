package com.lgcns.studify.post.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/post")
public class TestPostController {
    
    @GetMapping("/posts")
    public List<String> getPosts() {
        return Arrays.asList(
            "Post 1: Spring Boot 프로젝트 팀원 모집",
            "Post 2: React 개발자 구합니다",
            "Post 3: 알고리즘 스터디원 모집"
        );
    }
    
    @GetMapping("/health")
    public String health() {
        return "Post Service is running!";
    }
}