package com.example.notificationservice.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

@Configuration
public class OpenTelemetryConfig {

    @Bean
    public TextMapGetter<ConsumerRecord<String, String>> kafkaGetter() {

        return new TextMapGetter<>() {

            @Override
            public Iterable<String> keys(ConsumerRecord<String, String> carrier) {
                if (carrier == null) {
                    return java.util.Collections.emptyList();
                }
                return StreamSupport
                        .stream(carrier.headers().spliterator(), false)
                        .map(Header::key)
                        .toList();
            }

            @Override
            public String get(ConsumerRecord<String, String> carrier, String key) {
                if (carrier == null || key == null) {
                    return null;
                }
                Header header = carrier.headers().lastHeader(key);
                if (header == null) {
                    return null;
                }
                return new String(header.value(), StandardCharsets.UTF_8);
            }
        };
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("notification-service");
    }
}