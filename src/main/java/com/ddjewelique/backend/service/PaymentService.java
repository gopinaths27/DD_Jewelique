package com.ddjewelique.backend.service;

import com.ddjewelique.backend.model.Order;
import com.ddjewelique.backend.model.Payment;
import com.ddjewelique.backend.repository.OrderRepository;
import com.ddjewelique.backend.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private OrderRepository orderRepo;


    public Payment processPayment(Long orderId, String gatewayPaymentId, double amount) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentId(gatewayPaymentId);
        payment.setAmount(amount);
        payment.setStatus("SUCCESS");
        payment.setCreatedAt(LocalDateTime.now());

        // ✅ Do not change order status here
        return paymentRepo.save(payment);
    }

    public Payment failPayment(Long orderId, String gatewayPaymentId, double amount) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentId(gatewayPaymentId);
        payment.setAmount(amount);
        payment.setStatus("FAILED");
        payment.setCreatedAt(LocalDateTime.now());

        // ✅ Order remains PLACED
        return paymentRepo.save(payment);
    }

    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus("REFUNDED");
        paymentRepo.save(payment);

        Order order = payment.getOrder();
        order.setStatus("CANCELLED"); // ✅ only here we update order
        orderRepo.save(order);

        return payment;
    }
}
