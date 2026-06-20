package com.ddjewelique.backend.service;

import com.ddjewelique.backend.model.*;
import com.ddjewelique.backend.repository.OrderRepository;
import com.ddjewelique.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final CartService cartService;
     public OrderService(OrderRepository orderRepo, ProductRepository productRepo, CartService cartServ) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
        this.cartService=cartServ;
    }

    // Place a new order
    public Order placeOrder(Customer customer, Map<Long, Integer> productQuantities,
                            String shippingAddress, String city, String pincode, String country,String phoneNumber) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PLACED");
        order.setShippingAddress(shippingAddress);
        order.setCity(city);
        order.setPincode(pincode);
        order.setCountry(country);
        order.setPhoneNumber(phoneNumber);

        List<OrderItem> items = productQuantities.entrySet().stream().map(entry -> {
            Product product = productRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            int qty = entry.getValue();

            if (product.getStockQuantity() == null || product.getStockQuantity() < qty) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - qty);
            productRepo.save(product);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setPrice(product.getPrice() * qty);
            item.setOrder(order);
            return item;
        }).toList();

        order.setItems(items);

        double total = items.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();
        order.setTotalAmount(total);

        return orderRepo.save(order);
    }

    public Order checkout(Customer customer, String shippingAddress, String city,
                          String pincode, String country,String phoneNumber) {
        Cart cart = cartService.getCart(customer);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        Map<Long, Integer> productQuantities = cart.getItems().stream()
                .collect(Collectors.toMap(
                        item -> item.getProduct().getId(),
                        CartItem::getQuantity
                ));

        Order order = placeOrder(customer, productQuantities, shippingAddress, city, pincode, country,phoneNumber);

        cartService.clearCart(customer);

        return order;
    }

    public List<Order> getOrders(Customer customer) {
        return orderRepo.findByCustomer(customer);
    }

    public Order updateStatus(Long orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        return orderRepo.save(order);
    }
}
