package com.example.payment.consumer;

import com.example.payment.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    @Autowired
    private PaymentService paymentService;

    @KafkaListener(
            topics = "order-created-topic",
            groupId = "payment-group"
    )
    public void consume(
            ConsumerRecord<String, String> record) {

        System.out.println(
                "Received Event : "
                        + record.value()
        );

        paymentService.processOrderEvent(
                record.value()
        );
    }
}