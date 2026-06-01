package com.ddjewelique.backend.service;

import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.Order;
import com.ddjewelique.backend.model.OrderItem;
import com.ddjewelique.backend.model.Product;
import com.ddjewelique.backend.repository.OrderRepository;
import com.ddjewelique.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public OrderService(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    // Place a new order
    public Order placeOrder(Customer customer, Map<Long, Integer> productQuantities) {
        Order order = new Order();
        order.setCustomer(customer);

        List<OrderItem> items = productQuantities.entrySet().stream().map(entry -> {
            Product product = productRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(entry.getValue());
            item.setPrice(product.getPrice() * entry.getValue()); // snapshot at order time
            item.setOrder(order);
            return item;
        }).toList();

        order.setItems(items);

        double total = items.stream()
                .mapToDouble(OrderItem::getPrice)
                .sum();
        order.setTotalAmount(total);

        order.setStatus("PENDING");

        return orderRepo.save(order);
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
