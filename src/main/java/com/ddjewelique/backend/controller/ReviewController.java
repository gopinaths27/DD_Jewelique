package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.dto.ReviewDTO;
import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.Product;
import com.ddjewelique.backend.model.Review;
import com.ddjewelique.backend.repository.ProductRepository;
import com.ddjewelique.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepo;
    @Autowired
    private ProductRepository productRepo;
    // ✅ Add Review
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<ReviewDTO>> add(Authentication auth,
                                                          @RequestBody ReviewDTO reviewDto) {
        Customer customer = (Customer) auth.getPrincipal();

        // fetch product by ID
        Product product = productRepo.findById(reviewDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Review review = new Review();
        review.setCustomer(customer);
        review.setProduct(product);
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(null);

        Review saved = reviewRepo.save(review);
        ReviewDTO dto = convertToDTO(saved);

        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Review added", dto));
    }

    // ✅ Get Reviews for Product
    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseWrapper<List<ReviewDTO>>> getReviews(@PathVariable Long productId) {
        List<ReviewDTO> reviews = reviewRepo.findByProductId(productId)
                .stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Success", reviews));
    }

    // ✅ Update Review
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<ReviewDTO>> update(Authentication auth,
                                                             @PathVariable Long id,
                                                             @RequestBody Review updatedReview) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Customer customer = (Customer) auth.getPrincipal();
        if (!review.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You can only update your own review");
        }

        review.setRating(updatedReview.getRating());
        review.setComment(updatedReview.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        Review saved = reviewRepo.save(review);
        ReviewDTO dto = convertToDTO(saved);

        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Review updated", dto));
    }

    // ✅ Get My Reviews
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<List<ReviewDTO>>> getMyReviews(Authentication auth) {
        Customer customer = (Customer) auth.getPrincipal();
        List<ReviewDTO> reviews = reviewRepo.findByCustomerId(customer.getId())
                .stream()
                .map(this::convertToDTO)
                .toList();

        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Success", reviews));
    }

    // ✅ Delete Own Review
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ResponseWrapper<Void>> delete(Authentication auth, @PathVariable Long id) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        Customer customer = (Customer) auth.getPrincipal();
        if (!review.getCustomer().getId().equals(customer.getId())) {
            throw new RuntimeException("You can only delete your own review");
        }

        reviewRepo.delete(review);
        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Review deleted", null));
    }

    // ✅ Admin Delete Any Review
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> adminDelete(@PathVariable Long id) {
        Review review = reviewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        reviewRepo.delete(review);
        return ResponseEntity.ok(new ResponseWrapper<>("M200", "Review deleted by admin", null));
    }

    // ✅ DTO Converter
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setProductId(review.getProduct() != null ? review.getProduct().getId() : null);
        dto.setCustomerId(review.getCustomer() != null ? review.getCustomer().getId() : null);

        if (review.getCustomer() != null) {
            String firstName = review.getCustomer().getFirstName() != null ? review.getCustomer().getFirstName() : "";
            String lastName = review.getCustomer().getLastName() != null ? review.getCustomer().getLastName() : "";
            dto.setCustomerName((firstName + " " + lastName).trim());
        }

        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());

        return dto;
    }
}
