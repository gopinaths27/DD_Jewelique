package com.ddjewelique.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Password Reset OTP");
        message.setText("Use this OTP to reset your password: " + otp + "\nIt will expire in 5 minutes.");
        mailSender.send(message);
    }
    // ✅ New: Order placed email
    public void sendOrderPlacedEmail(String toEmail, Long orderId, double totalAmount) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Placed - ID " + orderId);
        message.setText("Dear Customer,\n\nYour order has been placed successfully.\n"
                + "Order ID: " + orderId + "\nTotal Amount: " + totalAmount
                + "\n\nWe will notify you when the status changes.\n\nThank you for shopping with us!");
        mailSender.send(message);
    }

    // ✅ New: Order status update email
    public void sendOrderStatusUpdateEmail(String toEmail, Long orderId, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Status Updated - ID " + orderId);
        message.setText("Dear Customer,\n\nYour order status has been updated to: " + status
                + "\n\nThank you for shopping with us!");
        mailSender.send(message);
    }
}
