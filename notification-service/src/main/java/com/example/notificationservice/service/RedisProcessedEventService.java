package com.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisProcessedEventService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String PREFIX =
            "processed-event:";

    public boolean isProcessed(String eventId) {

        boolean exists =
                Boolean.TRUE.equals(
                        redisTemplate.hasKey(
                                PREFIX + eventId
                        )
                );

        log.info(
                "REDIS CHECK => {} => {}",
                eventId,
                exists
        );

        return exists;
    }

    public void markProcessed(String eventId) {

        redisTemplate.opsForValue().set(
                PREFIX + eventId,
                "PROCESSED",
                1,
                TimeUnit.DAYS
        );

        log.info(
                "REDIS STORE => {}",
                eventId
        );
    }
}