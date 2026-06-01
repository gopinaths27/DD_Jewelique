package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.CartResponse;
import com.ddjewelique.backend.mapper.CartMapper;
import com.ddjewelique.backend.model.Cart;
import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.User;
import com.ddjewelique.backend.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }


    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public CartResponse getCart(Authentication auth) {
        Customer customer = (Customer) auth.getPrincipal();
        Cart cart = service.getCart(customer);
        return CartMapper.toResponse(cart);
    }
    @PostMapping("/add/{productId}")
    @PreAuthorize("hasRole('USER')")
    public CartResponse addToCart(Authentication auth,
                                  @PathVariable Long productId,
                                  @RequestParam int qty) {
        Customer customer = (Customer) auth.getPrincipal();
        Cart cart = service.addToCart(customer, productId, qty);
        return CartMapper.toResponse(cart);
    }

    @DeleteMapping("/remove/{productId}")
    @PreAuthorize("hasRole('USER')")
    public CartResponse removeFromCart(Authentication auth,
                                       @PathVariable Long productId) {
        Customer customer = (Customer) auth.getPrincipal();
        Cart cart = service.removeFromCart(customer, productId);
        return CartMapper.toResponse(cart);
    }

    @PutMapping("/update/{productId}")
    @PreAuthorize("hasRole('USER')")
        public CartResponse updateCartItem(Authentication auth,
                                       @PathVariable Long productId,
                                       @RequestParam int qty) {
        Customer customer = (Customer) auth.getPrincipal();
        Cart cart = service.updateCartItem(customer, productId, qty);
        return CartMapper.toResponse(cart);
    }
}