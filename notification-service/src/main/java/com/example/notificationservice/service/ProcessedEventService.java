package com.example.notificationservice.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProcessedEventService {

    private final Set<String> processedEvents =
            ConcurrentHashMap.newKeySet();

    public boolean isProcessed(String eventId) {
        return processedEvents.contains(eventId);
    }

    public void markProcessed(String eventId) {
        processedEvents.add(eventId);
    }
}