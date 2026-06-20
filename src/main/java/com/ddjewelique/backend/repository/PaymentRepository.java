package com.ddjewelique.backend.repository;

import com.ddjewelique.backend.model.Order;
import com.ddjewelique.backend.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentId(String paymentId);
    Optional<Payment> findByOrder(Order orderId);
}