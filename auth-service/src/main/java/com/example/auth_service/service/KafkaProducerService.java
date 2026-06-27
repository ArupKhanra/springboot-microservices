package com.example.auth_service.service;

import com.example.auth_service.dto.UserCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private static final String TOPIC = "user-created-topic";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final OpenTelemetry openTelemetry;
    private final TextMapSetter<ProducerRecord<String, String>> kafkaSetter;

    /**
     * @param event           the event payload to publish
     * @param propagationContext the OpenTelemetry Context to inject into Kafka headers.
     *                            Pass the context RESTORED from OutboxEvent.traceparent,
     *                            NOT Context.current() — the calling (scheduler) thread
     *                            has no reliable ambient trace context of its own.
     */
    public void sendUserCreatedEvent(UserCreatedEvent event, Context propagationContext) {

        try {
            String json = objectMapper.writeValueAsString(event);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(TOPIC, event.getEventId(), json);

            openTelemetry
                    .getPropagators()
                    .getTextMapPropagator()
                    .inject(propagationContext, record, kafkaSetter);

            record.headers().forEach(header ->
                    log.info(
                            "Kafka Producer Header => {} = {}",
                            header.key(),
                            new String(header.value(), StandardCharsets.UTF_8)
                    )
            );

            kafkaTemplate.send(record)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info(
                                    "Kafka Publish Success => Topic={}, Partition={}, Offset={}, EventId={}",
                                    result.getRecordMetadata().topic(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset(),
                                    event.getEventId()
                            );
                        } else {
                            log.error(
                                    "Kafka Publish Failed => EventId={}",
                                    event.getEventId(),
                                    ex
                            );
                        }
                    });

        } catch (Exception e) {
            log.error(
                    "Failed To Serialize Or Publish Event => EventId={}",
                    event.getEventId(),
                    e
            );
            throw new RuntimeException("Failed to publish Kafka event", e);
        }
    }
}