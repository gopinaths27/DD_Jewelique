package com.ddjewelique.backend.controller;
import com.ddjewelique.backend.dto.OrderDTO;
import com.ddjewelique.backend.dto.ProductDTO;
import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.model.*;
import com.ddjewelique.backend.repository.CustomerRepository;
import com.ddjewelique.backend.repository.OrderRepository;
import com.ddjewelique.backend.repository.PaymentRepository;
import com.ddjewelique.backend.repository.ProductRepository;
import com.ddjewelique.backend.service.EmailService;
import com.ddjewelique.backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderRepository orderRepo;
    private final CustomerRepository customerRepo;
    private final ProductRepository productRepo;
    private final OrderService service;
    private final EmailService emailService;
    private final PaymentRepository paymentRepo;

    public OrderController(OrderRepository orderRepo,
                           CustomerRepository customerRepo,
                           ProductRepository productRepo,
                           OrderService service,
                           EmailService emailService,
                           PaymentRepository paymentRepo) {
        this.orderRepo = orderRepo;
        this.customerRepo = customerRepo;
        this.productRepo = productRepo;
        this.service = service;
        this.emailService=emailService;
        this.paymentRepo=paymentRepo;
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<List<OrderDTO>>> getOrders(Authentication auth) {
        Customer customer = (Customer) auth.getPrincipal();

        List<OrderDTO> dtos = service.getOrders(customer).stream()
                .map(this::convertToDTO)
                .toList();

        ResponseWrapper<List<OrderDTO>> response =
                new ResponseWrapper<>("M200", "Success", dtos);

        return ResponseEntity.ok(response);
    }
    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<OrderDTO>> add(Authentication auth,
                                                         @RequestBody Order order) {
        try {
            order.setOrderDate(java.time.LocalDateTime.now()    );

            // ✅ Get customer from logged-in user
            Customer customer = (Customer) auth.getPrincipal();
            order.setCustomer(customer);

            // ✅ Handle products via OrderItem
            for (OrderItem item : order.getItems()) {
                Product product = productRepo.findById(item.getProduct().getId())
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // ✅ Reduce stock
                if (product.getStockQuantity() == null || product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Not enough stock for product: " + product.getName());
                }
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepo.save(product);

                item.setProduct(product);
                item.setOrder(order);
                item.setPrice(product.getPrice() * item.getQuantity());
            }

            double total = order.getItems().stream()
                    .mapToDouble(OrderItem::getPrice)
                    .sum();
            order.setTotalAmount(total);

            Order savedOrder = orderRepo.save(order);

            // ✅ Send confirmation email to customer
            emailService.sendOrderPlacedEmail(
                    customer.getEmail(),
                    savedOrder.getId(),
                    savedOrder.getTotalAmount()
            );

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
    @PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<OrderDTO>> updateStatus(@PathVariable Long id,
                                                                  @RequestBody Map<String, String> body) {
        try {
            Order existing = orderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // ✅ Only update status
            existing.setStatus(body.get("status"));

            Order savedOrder = orderRepo.save(existing);
            // ✅ Send status update email
            emailService.sendOrderStatusUpdateEmail(
                    existing.getCustomer().getEmail(),
                    existing.getId(),
                    existing.getStatus()
            );
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


    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Void>> delete(@PathVariable Long id) {
        try {
            Order order = orderRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!"PLACED".equalsIgnoreCase(order.getStatus())) {
                throw new RuntimeException("Only orders with PENDING status can be deleted");
            }

            orderRepo.deleteById(id);

            ResponseWrapper<Void> response =
                    new ResponseWrapper<>("M200", "Success", null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<Void> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setStatus(order.getStatus());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCity(order.getCity());
        dto.setPincode(order.getPincode());
        dto.setCountry(order.getCountry());
        dto.setPhoneNumber(order.getPhoneNumber());
        dto.setTotalAmount(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
        // ✅ Add payment status
        Payment payment = paymentRepo.findByOrder(order).orElse(null);
        dto.setPaymentStatus(payment != null ? payment.getStatus() : "PENDING");
        if (order.getCustomer() != null) {
            dto.setCustomerName(
                    (order.getCustomer().getFirstName() != null ? order.getCustomer().getFirstName() : "")
                            + " " +
                            (order.getCustomer().getLastName() != null ? order.getCustomer().getLastName() : "")
            );
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<ProductDTO> productDTOs = order.getItems().stream().map(item -> {
                ProductDTO pdto = new ProductDTO();
                pdto.setId(item.getProduct().getId());
                pdto.setName(item.getProduct().getName());
                pdto.setPrice(item.getProduct().getPrice());
                pdto.setDescription(item.getProduct().getDescription());
                pdto.setCategory(item.getProduct().getCategory());
                pdto.setMaterial(item.getProduct().getMaterial());
                pdto.setWeight(item.getProduct().getWeight());
                pdto.setStockQuantity(item.getProduct().getStockQuantity());
                pdto.setImages(item.getProduct().getImages());
                pdto.setActive(item.getProduct().getActive());

                return pdto;
            }).toList();
            dto.setProducts(productDTOs);
        }
        // ✅ Map address fields
        dto.setShippingAddress(order.getShippingAddress());
        dto.setCity(order.getCity());
        dto.setPincode(order.getPincode());
        dto.setCountry(order.getCountry());
        dto.setPhoneNumber(order.getPhoneNumber());
        return dto;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<OrderDTO>> checkout(Authentication auth,
                                                              @RequestBody Order orderRequest) {
        try {
        Customer customer = (Customer) auth.getPrincipal();

        Order order = service.checkout(
                customer,
                orderRequest.getShippingAddress(),
                orderRequest.getCity(),
                orderRequest.getPincode(),
                orderRequest.getCountry(),
                orderRequest.getPhoneNumber()
        );

            emailService.sendOrderPlacedEmail(
                    customer.getEmail(),
                    order.getId(),
                    order.getTotalAmount()
            );

        OrderDTO dto = convertToDTO(order);
        ResponseWrapper<OrderDTO> response =
                new ResponseWrapper<>("M200", "Order placed successfully", dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseWrapper<OrderDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


}