package com.ddjewelique.backend.dto;

public class PaymemtDTO {
    private Long id;
    private String paymentId;
    private String status;
    private double amount;
    private Long orderId;

    public PaymemtDTO(Long id, String paymentId, String status, double amount, Long orderId) {
        this.id = id;
        this.paymentId = paymentId;
        this.status = status;
        this.amount = amount;
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
