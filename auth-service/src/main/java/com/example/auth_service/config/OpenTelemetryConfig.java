package com.example.auth_service.config;

import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public TextMapSetter<ProducerRecord<String, String>> kafkaSetter() {
        return (carrier, key, value) -> {
            if (carrier == null || key == null || value == null) {
                return;
            }
            carrier.headers().add(key, value.getBytes(StandardCharsets.UTF_8));
        };
    }

    /**
     * Used by the OutboxPublisherScheduler to EXTRACT the trace context
     * back out of the single "traceparent" string column stored in MySQL.
     */
    @Bean
    public TextMapGetter<Map<String, String>> mapGetter() {
        return new TextMapGetter<>() {
            @Override
            public Iterable<String> keys(Map<String, String> carrier) {
                return carrier.keySet();
            }

            @Override
            public String get(Map<String, String> carrier, String key) {
                return carrier == null ? null : carrier.get(key);
            }
        };
    }
}