package com.ddjewelique.backend.mapper;

import com.ddjewelique.backend.dto.CartResponse;
import com.ddjewelique.backend.dto.CartItemResponse;
import com.ddjewelique.backend.model.Cart;
import com.ddjewelique.backend.model.CartItem;

import java.math.BigDecimal;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartResponse toResponse(Cart cart) {
        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setCustomerEmail(cart.getCustomer().getEmail());

        response.setItems(cart.getItems().stream()
                .map(CartMapper::mapItem)
                .collect(Collectors.toList()));

        response.setTotalAmount(cart.calculateTotal()); // if you have a method for totals
        return response;
    }

    private static CartItemResponse mapItem(CartItem item) {
        CartItemResponse dto = new CartItemResponse();
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(BigDecimal.valueOf(item.getProduct().getPrice())); // ✅ fixed
        dto.setActive(item.getProduct().getActive());
        return dto;
    }
}
