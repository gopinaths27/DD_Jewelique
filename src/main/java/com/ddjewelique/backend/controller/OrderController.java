package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.OrderDTO;
import com.ddjewelique.backend.dto.ProductDTO;
import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.Order;
import com.ddjewelique.backend.model.OrderItem;
import com.ddjewelique.backend.model.Product;
import com.ddjewelique.backend.repository.CustomerRepository;
import com.ddjewelique.backend.repository.OrderRepository;
import com.ddjewelique.backend.repository.ProductRepository;
import com.ddjewelique.backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final OrderService service;

    public OrderController(OrderRepository orderRepo,
                           CustomerRepository customerRepo,
                           ProductRepository productRepo,
                           OrderService service) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.service = service;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public List<Order> getOrders(Authentication auth) {
        Customer customer = (Customer) auth.getPrincipal();
        return service.getOrders(customer);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ResponseWrapper<OrderDTO>> add(@RequestBody Order order) {
        try {
            order.setOrderDate(java.time.LocalDateTime.now());

            Customer customer = customerRepo.findById(order.getCustomer().getId())
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            order.setCustomer(customer);

            // ✅ Handle products via OrderItem
            for (OrderItem item : order.getItems()) {
                Product product = productRepo.findById(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));
                item.setProduct(product);
                item.setOrder(order);
                item.setPrice(product.getPrice() * item.getQuantity());
            }

            double total = order.getItems().stream()
                    .mapToDouble(OrderItem::getPrice)
                    .sum();
            order.setTotalAmount(total);

            Order savedOrder = orderRepo.save(order);
            OrderDTO dto = convertToDTO(savedOrder);

            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<OrderDTO>>> getAll() {
        try {
            List<OrderDTO> dtos = orderRepo.findAll().stream()
                    .map(this::convertToDTO)
                    .toList();

            ResponseWrapper<List<OrderDTO>> response =
                    new ResponseWrapper<>("M200", "Success", dtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<List<OrderDTO>> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<ResponseWrapper<OrderDTO>> getById(@PathVariable Long id) {
        try {
            Order order = orderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            OrderDTO dto = convertToDTO(order);

            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<OrderDTO>> update(@PathVariable Long id,
                                                            @RequestBody Order updatedOrder) {
        try {
            Order existing = orderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            existing.setStatus(updatedOrder.getStatus());

            if (updatedOrder.getItems() != null) {
                for (OrderItem item : updatedOrder.getItems()) {
                    Product product = productRepo.findById(item.getProduct().getId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));
                    item.setProduct(product);
                    item.setOrder(existing);
                    item.setPrice(product.getPrice() * item.getQuantity());
                }
                existing.setItems(updatedOrder.getItems());

                double total = existing.getItems().stream()
                        .mapToDouble(OrderItem::getPrice)
                        .sum();
                existing.setTotalAmount(total);
            }

            Order savedOrder = orderRepo.save(existing);
            OrderDTO dto = convertToDTO(savedOrder);

            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable Long id) {
        try {
            if (!orderRepo.existsById(id)) {
                throw new RuntimeException("Order not found");
            }
            orderRepo.deleteById(id);

            ResponseWrapper<Void> response =
                    new ResponseWrapper<>("M200", "Success", null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<Void> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);

        if (order.getCustomer() != null) {
            dto.setCustomerName(order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<ProductDTO> productDTOs = order.getItems().stream().map(item -> {
                ProductDTO pdto = new ProductDTO();
                pdto.setId(item.getProduct().getId());
                pdto.setName(item.getProduct().getName());
                pdto.setPrice(item.getProduct().getPrice());
                return pdto;
            }).toList();
            dto.setProducts(productDTOs);
        }

        return dto;
    }
}