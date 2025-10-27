package com.lgcns.studify.post.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.lgcns.studify.post.domain.dto.PostRequestDTO;
import com.lgcns.studify.post.domain.dto.PostResponseDTO;
import com.lgcns.studify.post.service.PostService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 관리 API")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
    public ResponseEntity<?> register(@RequestBody PostRequestDTO request,
                                      @RequestHeader("X-User-Id") String userIdHeader) {
        try {
            Long authorId = Long.parseLong(userIdHeader);
            PostResponseDTO response = postService.createPost(request, authorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 작성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping
    @Operation(summary = "전체 게시글 조회", description = "모든 게시글을 조회합니다.")
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        try {
            List<PostResponseDTO> posts = postService.getAllPosts();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{postId}")
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        try {
            PostResponseDTO post = postService.getPostById(postId);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("게시글을 찾을 수 없습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PutMapping("/{postId}")
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    public ResponseEntity<?> updatePost(@PathVariable Long postId,
                                        @RequestBody PostRequestDTO request,
                                        @RequestHeader("X-User-Id") String userIdHeader) {
        try {
            Long authorId = Long.parseLong(userIdHeader);
            PostResponseDTO response = postService.updatePost(postId, request, authorId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("게시글 수정 권한이 없습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    public ResponseEntity<?> deletePost(@PathVariable Long postId,
                                        @RequestHeader("X-User-Id") String userIdHeader) {
        try {
            Long authorId = Long.parseLong(userIdHeader);
            postService.deletePost(postId, authorId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("게시글 삭제 권한이 없습니다: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("게시글 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/author/{authorId}")
    @Operation(summary = "작성자별 게시글 조회", description = "특정 작성자의 게시글을 조회합니다.")
    public ResponseEntity<List<PostResponseDTO>> getPostsByAuthor(@PathVariable Long authorId) {
        try {
            List<PostResponseDTO> posts = postService.getPostsByAuthor(authorId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search/position")
    @Operation(summary = "포지션별 게시글 검색", description = "특정 포지션의 게시글을 검색합니다.")
    public ResponseEntity<List<PostResponseDTO>> searchPostsByPosition(
            @RequestParam List<String> positions) {
        try {
            List<PostResponseDTO> posts = postService.searchPostsByPosition(positions);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "키워드 검색", description = "제목 또는 내용으로 게시글을 검색합니다.")
    public ResponseEntity<List<PostResponseDTO>> searchPosts(@RequestParam String keyword) {
        try {
            List<PostResponseDTO> posts = postService.searchPostsByKeyword(keyword);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    @Operation(summary = "서비스 헬스체크", description = "Post Service의 상태를 확인합니다.")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Post Service");
        response.put("message", "Post Service is running successfully!");
        return ResponseEntity.ok(response);
    }
}