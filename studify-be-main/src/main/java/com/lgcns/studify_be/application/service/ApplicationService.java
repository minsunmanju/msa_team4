package com.lgcns.studify_be.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lgcns.studify_be.application.domain.dto.ApplicationResponseDTO;
import com.lgcns.studify_be.application.domain.entity.ApplicationEntity;
import com.lgcns.studify_be.application.domain.entity.ApplicationStatus;
import com.lgcns.studify_be.application.repository.ApplicationRepository;
import com.lgcns.studify_be.notification.service.NotificationService;
import com.lgcns.studify_be.post.domain.entity.PostEntity;
import com.lgcns.studify_be.post.repository.PostRepository;
import com.lgcns.studify_be.user.User;
import com.lgcns.studify_be.user.UserRepository;


@Service
public class ApplicationService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ApplicationResponseDTO apply(Long postId, String email) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 모집글"));
        
        User applicant = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자"));
        
        if (post.getAuthor().getId().equals(applicant.getId())) {
            throw new RuntimeException("작성자 본인은 신청 불가");
        }

        boolean alreadyApplied = applicant.getApplications().stream()
                .anyMatch(app -> app.getPost().getPostId().equals(postId));
        if (alreadyApplied) {
            throw new RuntimeException("이미 지원한 모집글");
        }

        ApplicationEntity application = ApplicationEntity.builder()
                .post(post)
                .applicant(applicant)
                .status(ApplicationStatus.PENDING)
                .build();
        ApplicationEntity savedApplication = applicationRepository.save(application);

        // 작성자에게 WebSocket 알림
        // notificationService.sendNotification(
        //         post.getAuthor(), 
        //         applicant.getNickname() + "님이 신청했습니다."
        // );
        notificationService.sendApplicationNotification(
            post.getAuthor(),
            applicant, 
            savedApplication.getApplicationId(),
            postId,
            post.getTitle()
        );
        return ApplicationResponseDTO.fromEntity(savedApplication);
    }

    @Transactional
    public ApplicationResponseDTO approve(Long applicationId, String email) {
        ApplicationEntity application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 지원현황"));

        if (!application.getPost().getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("작성자만 승인할 수 있습니다.");
        }
        application.setStatus(ApplicationStatus.APPROVED);
        ApplicationEntity savedApplication = applicationRepository.save(application);

        // notificationService.sendNotification(
        //         savedApplication.getApplicant(),
        //         "신청이 승인되었습니다."
        // );
        // 승인 알림 전송 (신청자에게)
        notificationService.sendApplicationResultNotification(
            application.getApplicant(),
            "APPROVED",
            application.getPost().getTitle()
        );
        return ApplicationResponseDTO.fromEntity(savedApplication);
    }

    @Transactional
    public ApplicationResponseDTO reject(Long applicationId, String email) {
        ApplicationEntity app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 지원현황"));
        
        if (!app.getPost().getAuthor().getEmail().equals(email)) {
            throw new RuntimeException("작성자만 승인할 수 있습니다.");
        }
        
        app.setStatus(ApplicationStatus.REJECTED);
        ApplicationEntity savedApplication = applicationRepository.save(app);

        // notificationService.sendNotification(
        //         savedApplication.getApplicant(),
        //         "신청이 거부되었습니다."
        // );
        // 거절 알림 전송 (신청자에게)
        notificationService.sendApplicationResultNotification(
            app.getApplicant(),
            "REJECTED",
            app.getPost().getTitle()
        );
        return ApplicationResponseDTO.fromEntity(savedApplication);
    }

}
