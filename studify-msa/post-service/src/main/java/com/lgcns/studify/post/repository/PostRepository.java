package com.lgcns.studify.post.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lgcns.studify.post.domain.entity.Post;
import com.lgcns.studify.post.domain.entity.Position;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 작성자 ID로 포스트 찾기
    List<Post> findByAuthorId(Long authorId);
    
    // 포스트 ID로 포스트 찾기
    Optional<Post> findByPostId(Long postId);
    
    // 카테고리로 포스트 찾기
    @Query("SELECT p FROM Post p WHERE p.category = :category")
    List<Post> findByCategory(@Param("category") String category);
    
    // 포지션으로 포스트 찾기
    @Query("SELECT DISTINCT p FROM Post p JOIN p.position pos WHERE pos IN :positions")
    List<Post> findByPositions(@Param("positions") List<Position> positions);
    
    // 제목으로 포스트 검색
    List<Post> findByTitleContainingIgnoreCase(String title);
    
    // 내용으로 포스트 검색
    List<Post> findByContentContainingIgnoreCase(String content);
    
    // 제목 또는 내용으로 포스트 검색
    @Query("SELECT p FROM Post p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(@Param("keyword") String keyword);
}