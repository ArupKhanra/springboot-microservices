package com.example.notificationservice.service;

import com.example.notificationservice.dto.UserCreatedEvent;

public interface NotificationService {

    void processUserRegistration(UserCreatedEvent event);
}