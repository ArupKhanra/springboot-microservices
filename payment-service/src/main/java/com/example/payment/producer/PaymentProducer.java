package com.example.payment.producer;

import com.example.payment.event.PaymentFailedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentProducer {

    @Autowired
    private KafkaTemplate<String, Object>
            kafkaTemplate;

    public void publishPaymentFailedEvent(
            PaymentFailedEvent event) {

        kafkaTemplate.send(
                "payment-failed-topic",
                event
        );

        System.out.println(
                "PaymentFailedEvent Published"
        );
    }
}