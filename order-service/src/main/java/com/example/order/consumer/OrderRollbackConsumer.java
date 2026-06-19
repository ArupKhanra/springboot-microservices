package com.example.order.consumer;

import com.example.order.service.OrderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderRollbackConsumer {

    @Autowired
    private OrderService orderService;

    @KafkaListener(
            topics = "payment-failed-topic",
            groupId = "order-group"
    )
    public void consume(
            ConsumerRecord<String, String> record) {

        System.out.println(
                "Rollback Event Received : "
                        + record.value()
        );

        JSONObject jsonObject =
                new JSONObject(record.value());

        Long orderId =
                jsonObject.getLong("orderId");

        orderService.cancelOrder(orderId);

        System.out.println(
                "ORDER CANCELLED : "
                        + orderId
        );
    }
}