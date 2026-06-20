package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.PaymemtDTO;
import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.model.Payment;
import com.ddjewelique.backend.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/payments")
    public class PaymentController {

        @Autowired
        private PaymentService paymentService;


    @PostMapping("/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<PaymemtDTO>> pay(@PathVariable Long orderId,
                                                           @RequestParam String gatewayPaymentId,
                                                           @RequestParam double amount) {
        Payment payment = paymentService.processPayment(orderId, gatewayPaymentId, amount);

        // ✅ Convert to DTO
        PaymemtDTO dto = new PaymemtDTO(
                payment.getId(),
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getOrder().getId()
        );

        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Payment successful", dto));
    }

    @PostMapping("/refund/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<PaymemtDTO>> refund(@PathVariable Long paymentId) {
        Payment payment = paymentService.refundPayment(paymentId);

        // ✅ Convert to DTO
        PaymemtDTO dto = new PaymemtDTO(
                payment.getId(),
                payment.getPaymentId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getOrder().getId()
        );

        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Refund successful", dto));
    }
    }

