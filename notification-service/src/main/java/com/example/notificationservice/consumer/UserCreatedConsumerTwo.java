package com.example.notificationservice.consumer;

import com.example.notificationservice.service.RedisProcessedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCreatedConsumerTwo {

    private final RedisProcessedEventService processedEventService;

    @KafkaListener(
            topics = "user-created-topic",
            groupId = "notification-group"
    )
    public void consume(
            String message,
            Acknowledgment acknowledgment
    ) {

        String eventId = String.valueOf(message.hashCode());

        if (processedEventService.isProcessed(eventId)) {

            log.info(
                    "Duplicate Event Ignored : {}",
                    eventId
            );

            acknowledgment.acknowledge();
            return;
        }

        log.info(
                "<<<<<<<< CONSUMER-2 <<<<<<<< {}",
                message
        );

        if (message.contains("\"username\":\"43\"")) {
            throw new RuntimeException("Email Service Failed");
        }

        log.info(
                "Email Sent Successfully"
        );

        processedEventService.markProcessed(eventId);

        log.info(
                "Event Stored In Redis : {}",
                eventId
        );

        acknowledgment.acknowledge();

        log.info(
                "Offset Committed Successfully"
        );
    }
}