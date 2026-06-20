package com.ddjewelique.backend.service;

import com.ddjewelique.backend.model.*;
import com.ddjewelique.backend.repository.CartRepository;
import com.ddjewelique.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }
    public Cart getCart(Customer customer) {
        return cartRepo.findByCustomer(customer).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setCustomer(customer);
            return cartRepo.save(cart);
        });
    }

    public Cart addToCart(Customer customer, Long productId, int qty) {
        Cart cart = getCart(customer);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(qty);
        item.setCart(cart);

        cart.getItems().add(item);
        return cartRepo.save(cart);
    }

    public Cart removeFromCart(Customer customer, Long productId) {
        Cart cart = getCart(customer);
        cart.getItems().removeIf(i -> i.getProduct().getId().equals(productId));
        return cartRepo.save(cart);
    };

    public Cart updateCartItem(Customer customer, Long productId, int qty) {
        // Get the cart for the customer
        Cart cart = cartRepo.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Find the product
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Find the cart item
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();

            if (qty <= 0) {
                // If qty is 0 or less → remove item
                cart.getItems().remove(existingItem);
            } else {
                // Update quantity
                existingItem.setQuantity(qty);
            }
        } else {
            // If item not in cart yet → add new
            if (qty > 0) {
                CartItem newItem = new CartItem();
                newItem.setProduct(product);
                newItem.setQuantity(qty);
                newItem.setCart(cart);
                cart.getItems().add(newItem);
            }
        }

        return cartRepo.save(cart);
    }
    public void clearCart(Customer customer) {
        Cart cart = getCart(customer);
        cart.getItems().clear();
        cartRepo.save(cart);
    }

}
