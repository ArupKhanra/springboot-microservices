package com.example.payment.controller;

import com.example.payment.entiry.Payment;
import com.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Payment processPayment(
            @RequestBody Payment payment) {

        return paymentService.processPayment(
                payment
        );
    }

    @PutMapping("/refund/{id}")
    public Payment refundPayment(
            @PathVariable Long id) {

        return paymentService.refundPayment(
                id
        );
    }

    @GetMapping
    public List<Payment> getAllPayments() {

        return paymentService.getAllPayments();
    }
}