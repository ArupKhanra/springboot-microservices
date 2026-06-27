package com.example.auth_service.scheduler;

import com.example.auth_service.dto.UserCreatedEvent;
import com.example.auth_service.entity.OutboxEvent;
import com.example.auth_service.entity.OutboxStatus;
import com.example.auth_service.repository.OutboxEventRepository;
import com.example.auth_service.service.KafkaProducerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;
    private final OpenTelemetry openTelemetry;
    private final TextMapGetter<Map<String, String>> mapGetter;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    @Observed(name = "publish-outbox-events")
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByStatus(OutboxStatus.PENDING.name());

        if (events.isEmpty()) {
            return;
        }

        log.info("Pending Outbox Events : {}", events.size());

        for (OutboxEvent event : events) {
            publishSingleEvent(event);
        }
    }

    private void publishSingleEvent(OutboxEvent event) {

        // ===== 1. RESTORE the trace context stored in MySQL =====
        Context restoredContext = Context.root();

        if (event.getTraceparent() != null && !event.getTraceparent().isBlank()) {
            Map<String, String> carrier = Map.of("traceparent", event.getTraceparent());
            restoredContext = openTelemetry
                    .getPropagators()
                    .getTextMapPropagator()
                    .extract(Context.root(), carrier, mapGetter);
        } else {
            log.warn("OutboxEvent {} has no traceparent stored, starting fresh trace", event.getEventId());
        }

        // ===== 2. Create a SPAN under the restored context, make it current
        //          for the duration of this iteration (logs/metrics correlate too) =====
        Tracer tracer = openTelemetry.getTracer("auth-service-outbox-scheduler");

        Span span = tracer.spanBuilder("outbox.publish")
                .setParent(restoredContext)
                .setSpanKind(SpanKind.PRODUCER)
                .setAttribute("outbox.event.id", event.getEventId())
                .setAttribute("outbox.event.type", event.getEventType())
                .startSpan();

        try (Scope scope = span.makeCurrent()) {

            log.info("Publishing Event : {}", event.getEventId());

            UserCreatedEvent userCreatedEvent =
                    objectMapper.readValue(event.getPayload(), UserCreatedEvent.class);

            // ===== 3. Pass the CURRENT context (the span we just created,
            //          which is a child of the restored HTTP trace) explicitly
            //          to the producer — do NOT rely on Context.current()
            //          surviving implicitly across the method boundary. =====
            kafkaProducerService.sendUserCreatedEvent(userCreatedEvent, Context.current());

            event.setStatus(OutboxStatus.SENT.name());
            event.setPublishedAt(LocalDateTime.now());
            outboxEventRepository.save(event);

            span.addEvent("kafka.publish.success");

            log.info("Outbox Event Published Successfully : {}", event.getEventId());

        } catch (Exception e) {

            span.recordException(e);

            event.setRetryCount(event.getRetryCount() + 1);

            if (event.getRetryCount() >= 5) {
                event.setStatus(OutboxStatus.FAILED.name());
                log.error(
                        "Event Marked As FAILED => EventId={}, RetryCount={}",
                        event.getEventId(), event.getRetryCount()
                );
            } else {
                log.warn(
                        "Retry Count Updated => EventId={}, RetryCount={}",
                        event.getEventId(), event.getRetryCount()
                );
            }

            outboxEventRepository.save(event);

            log.error("Failed To Publish Event : {}", event.getEventId(), e);

        } finally {
            span.end();
        }
    }
}