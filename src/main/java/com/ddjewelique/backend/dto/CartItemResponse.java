package com.ddjewelique.backend.dto;

import java.math.BigDecimal;

public class CartItemResponse {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private Boolean active;

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
