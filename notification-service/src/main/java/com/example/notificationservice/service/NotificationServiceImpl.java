package com.example.notificationservice.service;

import com.example.notificationservice.dto.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void processUserRegistration(UserCreatedEvent event) {

        log.info("Processing User Registration Event : {}", event);

        // Future:
        // Email
        // SMS
        // Push Notification
    }
}