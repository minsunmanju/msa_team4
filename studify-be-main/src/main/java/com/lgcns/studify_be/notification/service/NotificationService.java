package com.lgcns.studify_be.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lgcns.studify_be.notification.domain.entity.NotificationEntity;
import com.lgcns.studify_be.notification.repository.NotificationRepository;
import com.lgcns.studify_be.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    public void sendNotification(User user, String message) {
        // DB에 저장
        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .message(message)
                .build();
        notificationRepository.save(notification);

        // WebSocket 전송
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + user.getId(),
                message
        );
    }

    // 스터디 신청 알림 (작성자에게)
    public void sendApplicationNotification(User author, User applicant, Long applicationId, Long postId, String postTitle) {
        try {
            // JSON 형태로 구조화된 데이터 전송
            var notificationData = new java.util.HashMap<String, Object>();
            notificationData.put("type", "APPLICATION_RECEIVED");
            notificationData.put("message", applicant.getNickname() + "님이 '" + postTitle + "'에 신청했습니다.");
            notificationData.put("applicationId", applicationId);
            notificationData.put("postId", postId);
            notificationData.put("applicantName", applicant.getNickname());
            notificationData.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notificationData);

            // DB 저장
            NotificationEntity notification = NotificationEntity.builder()
                    .user(author)
                    .message(jsonMessage)
                    .build();
            notificationRepository.save(notification);

            // WebSocket 전송
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + author.getId(),
                    jsonMessage
            );
        } catch (Exception e) {
            // JSON 변환 실패 시 기본 메시지로 폴백
            sendNotification(author, applicant.getNickname() + "님이 신청했습니다.");
        }
    }

    // 승인/거절 알림 (신청자에게)
    public void sendApplicationResultNotification(User applicant, String result, String postTitle) {
        try {
            var notificationData = new java.util.HashMap<String, Object>();
            notificationData.put("type", "APPLICATION_RESULT");
            notificationData.put("message", "'" + postTitle + "' 신청이 " + (result.equals("APPROVED") ? "승인" : "거절") + "되었습니다.");
            notificationData.put("result", result);
            notificationData.put("postTitle", postTitle);
            notificationData.put("timestamp", System.currentTimeMillis());

            String jsonMessage = objectMapper.writeValueAsString(notificationData);

            // DB 저장
            NotificationEntity notification = NotificationEntity.builder()
                    .user(applicant)
                    .message(jsonMessage)
                    .build();
            notificationRepository.save(notification);

            // WebSocket 전송
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + applicant.getId(),
                    jsonMessage
            );
        } catch (Exception e) {
            // 폴백
            sendNotification(applicant, "신청이 " + (result.equals("APPROVED") ? "승인" : "거절") + "되었습니다.");
        }
    }



}
