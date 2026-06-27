package com.example.notificationservice.consumer;

import com.example.notificationservice.service.RedisProcessedEventService;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserCreatedConsumerTwo {

    private final RedisProcessedEventService processedEventService;
    private final OpenTelemetry openTelemetry;
    private final TextMapGetter<ConsumerRecord<String, String>> kafkaGetter;
    private final Tracer tracer;

    @Observed(name = "consume-user-created-event")
    @KafkaListener(
            topics = "user-created-topic-2",
            groupId = "notification-group"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment
    ) {

        String message = record.value();

        if (message == null) {
            log.warn("Received null Kafka message");
            acknowledgment.acknowledge();
            return;
        }

        Context extractedContext = openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.root(), record, kafkaGetter);

        Span span = tracer.spanBuilder("notification.user-created.consume-2")
                .setParent(extractedContext)
                .setSpanKind(SpanKind.CONSUMER)
                .setAttribute("messaging.system", "kafka")
                .setAttribute("messaging.destination.name", record.topic())
                .setAttribute("messaging.kafka.partition", record.partition())
                .setAttribute("messaging.kafka.offset", record.offset())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {

            String traceId = span.getSpanContext().getTraceId();
            String spanId = span.getSpanContext().getSpanId();

            MDC.put("traceId", traceId);
            MDC.put("spanId", spanId);

            log.info(
                    "Received Event => traceId={}, spanId={}, payload={}",
                    traceId,
                    spanId,
                    message
            );

            String eventId = String.valueOf(message.hashCode());

            if (processedEventService.isProcessed(eventId)) {

                log.info(
                        "Duplicate Event Ignored => EventId={}, TraceId={}",
                        eventId,
                        traceId
                );

                span.addEvent("idempotency.duplicate.skipped");
                acknowledgment.acknowledge();
                return;
            }

            log.info("Processing Event (Consumer-2) => EventId={}", eventId);

            if (message.contains("\"username\":\"43\"")) {
                throw new RuntimeException("Email Service Failed");
            }

            log.info("Email Sent Successfully => EventId={}", eventId);

            processedEventService.markProcessed(eventId);

            log.info("Event Stored In Redis => {}", eventId);

            acknowledgment.acknowledge();

            log.info("Kafka Offset Committed => EventId={}", eventId);

        } catch (Exception e) {

            span.recordException(e);
            span.setStatus(StatusCode.ERROR, e.getMessage());

            log.error(
                    "Consumer-2 Processing Failed => EventId={}",
                    String.valueOf(message.hashCode()),
                    e
            );

            throw e;

        } finally {
            span.end();
            MDC.remove("traceId");
            MDC.remove("spanId");
        }
    }
}