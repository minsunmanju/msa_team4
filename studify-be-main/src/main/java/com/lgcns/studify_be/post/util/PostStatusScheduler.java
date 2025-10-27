package com.lgcns.studify_be.post.util;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lgcns.studify_be.post.domain.entity.PostEntity;
import com.lgcns.studify_be.post.domain.entity.PostStatus;
import com.lgcns.studify_be.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PostStatusScheduler {

    private final PostRepository postRepository;

    // 1분마다 실행
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<PostEntity> expiredPosts = postRepository.findByStatusAndDeadlineBefore(PostStatus.OPEN, now);

        for (PostEntity post : expiredPosts) {
            post.setStatus(PostStatus.CLOSED);
        }
        // JPA flush로 자동 반영
    }
}
