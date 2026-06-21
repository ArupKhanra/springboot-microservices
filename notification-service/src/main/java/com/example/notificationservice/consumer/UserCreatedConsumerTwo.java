package com.example.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserCreatedConsumerTwo {

    @KafkaListener(
            topics = "user-created-topic",
            groupId = "notification-group"
    )
    public void consume(String message) {

        log.info(
                "<<<<<<<< CONSUMER-2 <<<<<<<< {}",
                message
        );

        if (message.contains("\"username\":\"43\"")) {
            throw new RuntimeException("Email Service Failed");
        }
    }
}