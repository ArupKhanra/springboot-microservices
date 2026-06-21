package com.example.notificationservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

    @Bean
    public DefaultErrorHandler errorHandler() {

        System.out.println("CUSTOM ERROR HANDLER LOADED");

        return new DefaultErrorHandler(
                new FixedBackOff(5000L, 2L)
        );
    }
}