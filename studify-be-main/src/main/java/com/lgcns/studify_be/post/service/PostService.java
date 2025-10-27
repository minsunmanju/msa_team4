package com.lgcns.studify_be.post.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lgcns.studify_be.post.domain.dto.PostRequestDTO;
import com.lgcns.studify_be.post.domain.dto.PostResponseDTO;
import com.lgcns.studify_be.post.domain.entity.Position;
import com.lgcns.studify_be.post.domain.entity.PostEntity;
import com.lgcns.studify_be.post.domain.entity.PostStatus;
import com.lgcns.studify_be.post.repository.PostRepository;
import com.lgcns.studify_be.user.User;
import com.lgcns.studify_be.user.UserRepository;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    public PostResponseDTO register(PostRequestDTO request, String email) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
        PostEntity post = postRepository.save(request.toEntity(user));
        return PostResponseDTO.fromEntity(post);
    }

    public List<PostResponseDTO> readPostList() {
        List<PostEntity> postList = postRepository.findAll();
        return postList.stream()
                        .map(entity -> PostResponseDTO.fromEntity(entity))
                        .toList();     
    }

    public PostResponseDTO readPostDetail(Long postId) {
        PostEntity post = postRepository.findByIdWithComments(postId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 모집글"));
        return PostResponseDTO.fromEntity(post);
    }

    public List<PostResponseDTO> findPostByTitle(String keyword) {
        List<PostEntity> postList = postRepository.findByTitleContainingIgnoreCase(keyword);
        return postList.stream()
                .map(entity -> PostResponseDTO.fromEntity(entity))
                .toList();
    }

    public List<PostResponseDTO> findPostByContent(String keyword) {
        List<PostEntity> postList = postRepository.findByContentContainingIgnoreCase(keyword);
        return postList.stream()
                .map(entity -> PostResponseDTO.fromEntity(entity))
                .toList();
    }

    public List<PostResponseDTO> findPostByTitleContent(String keyword) {
        List<PostEntity> postList = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
        return postList.stream()
                .map(entity -> PostResponseDTO.fromEntity(entity))
                .toList();
    }

    @Transactional
    public PostResponseDTO updatePost(Long postId, PostRequestDTO request, String email) {
        PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 모집글"));
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("본인 글만 수정 가능합니다.");
        }
        post.update(request);
        return PostResponseDTO.fromEntity(post);
    }

    public void deletePost(Long postId, String email) {
        PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 모집글"));
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("본인 글만 삭제 가능합니다.");
        }
        postRepository.delete(post);
    }

    public PostResponseDTO closePost(Long postId, String email) {
        PostEntity post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 모집글"));
        if (!post.getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("본인 글만 마감 가능합니다.");
        }
        post.setStatus(PostStatus.CLOSED);
        postRepository.save(post);
        return PostResponseDTO.fromEntity(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> searchPostsByPosition(String position) {
        if (position == null || position.equalsIgnoreCase("all")) {
            return postRepository.findAll()
                    .stream()
                    .map(PostResponseDTO::fromEntity)
                    .toList();
        }

        List<String> positionsStr = List.of(position.split(",")); 
        List<Position> positions = new ArrayList<>();
        for (String p : positionsStr) {
            Position pos = Position.from(p.trim());
            if (pos != null) positions.add(pos);
        }

        List<PostEntity> result = new ArrayList<>();
        for (Position pos : positions) {
            result.addAll(postRepository.findByPosition(pos));
        }

        return result.stream()
                     .distinct()
                     .map(PostResponseDTO::fromEntity)
                     .toList();
    }
}
