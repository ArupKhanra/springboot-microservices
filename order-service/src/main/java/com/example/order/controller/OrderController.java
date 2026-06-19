package com.example.order.controller;

import com.example.order.entiry.Order;
import com.example.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Order createOrder(@RequestBody Order order) {

        return orderService.createOrder(order);
    }

    @PutMapping("/cancel/{id}")
    public Order cancelOrder(@PathVariable Long id) {

        return orderService.cancelOrder(id);
    }
    @GetMapping("/test")
    public String test() {
        return "Order Service Working";
    }
}
