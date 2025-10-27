package com.lgcns.studify.post.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lgcns.studify.post.domain.dto.PostRequestDTO;
import com.lgcns.studify.post.domain.dto.PostResponseDTO;
import com.lgcns.studify.post.domain.entity.Post;
import com.lgcns.studify.post.domain.entity.Position;
import com.lgcns.studify.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO request, Long authorId) {
        Post post = request.toEntity(authorId);
        Post savedPost = postRepository.save(post);
        return PostResponseDTO.fromEntity(savedPost);
    }

    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public PostResponseDTO getPostById(Long postId) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return PostResponseDTO.fromEntity(post);
    }

    public List<PostResponseDTO> getPostsByAuthor(Long authorId) {
        return postRepository.findByAuthorId(authorId).stream()
                .map(PostResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponseDTO updatePost(Long postId, PostRequestDTO request, Long authorId) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        // 작성자 확인
        if (!post.getAuthorId().equals(authorId)) {
            throw new RuntimeException("Not authorized to update this post");
        }
        
        post.update(request);
        Post updatedPost = postRepository.save(post);
        return PostResponseDTO.fromEntity(updatedPost);
    }

    @Transactional
    public void deletePost(Long postId, Long authorId) {
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        // 작성자 확인
        if (!post.getAuthorId().equals(authorId)) {
            throw new RuntimeException("Not authorized to delete this post");
        }
        
        postRepository.delete(post);
    }

    public List<PostResponseDTO> searchPostsByPosition(List<String> positions) {
        List<Position> positionEnums = positions.stream()
                .map(Position::from)
                .collect(Collectors.toList());
        
        return postRepository.findByPositions(positionEnums).stream()
                .map(PostResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<PostResponseDTO> searchPostsByKeyword(String keyword) {
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword).stream()
                .map(PostResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}