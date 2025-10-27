package com.lgcns.studify_be.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lgcns.studify_be.post.domain.entity.Position;
import com.lgcns.studify_be.post.domain.entity.PostEntity;
import com.lgcns.studify_be.post.domain.entity.PostStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    
    List<PostEntity> findByTitleContainingIgnoreCase(String keyword);
    List<PostEntity> findByContentContainingIgnoreCase(String keyword);
    List<PostEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String titleKeyword, String contentKeyword);

    @Query("SELECT p FROM PostEntity p LEFT JOIN FETCH p.comments WHERE p.postId = :postId")
    Optional<PostEntity> findByIdWithComments(@Param("postId") Long postId);

    @Query("SELECT p FROM PostEntity p WHERE :position MEMBER OF p.position")
    List<PostEntity> findByPosition(@Param("position") Position position);

    List<PostEntity> findByStatusAndDeadlineBefore(PostStatus status, LocalDateTime now);
}
