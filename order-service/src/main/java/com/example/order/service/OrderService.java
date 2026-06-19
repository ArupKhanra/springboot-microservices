package com.example.order.service;

import com.example.order.entiry.Order;
import com.example.order.event.OrderCreatedEvent;
import com.example.order.producer.OrderProducer;
import com.example.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderProducer orderProducer;

    public Order createOrder(
            Order order) {

        order.setStatus("CREATED");

        Order savedOrder =
                orderRepository.save(order);

        OrderCreatedEvent event =
                new OrderCreatedEvent();

        event.setOrderId(
                savedOrder.getId());

        event.setProductName(
                savedOrder.getProductName());

        event.setAmount(
                savedOrder.getAmount());

        orderProducer.publishOrderCreatedEvent(
                event
        );

        return savedOrder;
    }

    public Order cancelOrder(Long id) {

        Order order =
                orderRepository.findById(id)
                        .orElseThrow();

        order.setStatus("CANCELLED");

        return orderRepository.save(order);
    }
}