package com.lgcns.studify_be.notification.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lgcns.studify_be.notification.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notification")
public class NotificationCtrl {
    
    @Autowired
    private NotificationService notificationService;
}
