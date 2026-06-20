package com.example.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserCreatedConsumer {

    @KafkaListener(
            topics = "user-created-topic",
            groupId = "notification-group"
    )
    public void consume(String message) {

        System.out.println(
                "Received Event : " + message
        );

        log.info(
                "Received Event : {}",
                message
        );
    }
}