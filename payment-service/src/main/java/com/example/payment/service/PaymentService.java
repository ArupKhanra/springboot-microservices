package com.example.payment.service;

import com.example.payment.entiry.Payment;
import com.example.payment.event.PaymentFailedEvent;
import com.example.payment.producer.PaymentProducer;
import com.example.payment.repository.PaymentRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentProducer paymentProducer;

    public void processOrderEvent(
            String message) {

        JSONObject jsonObject =
                new JSONObject(message);

        Long orderId =
                jsonObject.getLong("orderId");

        Double amount =
                jsonObject.getDouble("amount");

        String productName =
                jsonObject.getString("productName");

        Payment payment =
                new Payment();

        payment.setOrderId(orderId);

        payment.setAmount(amount);

        Random random =
                new Random();

        boolean paymentSuccess =
                random.nextBoolean();

        if (paymentSuccess) {

            payment.setStatus("SUCCESS");

            paymentRepository.save(payment);

            System.out.println(
                    "PAYMENT SUCCESS"
            );

        } else {

            payment.setStatus("FAILED");

            paymentRepository.save(payment);

            PaymentFailedEvent event =
                    new PaymentFailedEvent();

            event.setOrderId(orderId);

            paymentProducer
                    .publishPaymentFailedEvent(
                            event
                    );

            System.out.println(
                    "PAYMENT FAILED"
            );
        }
    }

    public Payment processPayment(
            Payment payment) {

        payment.setStatus("SUCCESS");

        return paymentRepository.save(
                payment
        );
    }

    public Payment refundPayment(
            Long id) {

        Payment payment =
                paymentRepository.findById(id)
                        .orElseThrow();

        payment.setStatus("REFUNDED");

        return paymentRepository.save(
                payment
        );
    }

    public List<Payment> getAllPayments() {

        return paymentRepository.findAll();
    }
}