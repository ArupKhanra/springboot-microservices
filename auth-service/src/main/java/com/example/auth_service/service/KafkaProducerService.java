package com.example.auth_service.service;

import com.example.auth_service.dto.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserCreatedEvent(UserCreatedEvent event) {

        try {

            String json =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(
                    "user-created-topic",
                    json
            );

            log.info("Event Published Successfully : {}", json);

        } catch (Exception e) {

            log.error("Failed To Publish Event", e);
        }
    }
}