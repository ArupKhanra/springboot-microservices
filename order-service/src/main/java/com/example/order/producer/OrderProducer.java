package com.example.order.producer;

import com.example.order.event.OrderCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderProducer {

    @Autowired
    private KafkaTemplate<String, Object>
            kafkaTemplate;

    public void publishOrderCreatedEvent(
            OrderCreatedEvent event) {

        kafkaTemplate.send(
                "order-created-topic",
                event
        );

        System.out.println(
                "OrderCreatedEvent Published"
        );
    }
}