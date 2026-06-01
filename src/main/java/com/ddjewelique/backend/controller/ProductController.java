package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.ProductDTO;
import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.model.Product;
import com.ddjewelique.backend.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ProductDTO>>> getProducts(Authentication auth) {
        try {
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            List<Product> products = isAdmin ? repo.findAll() : repo.findByActiveTrue();

            List<ProductDTO> dtos = products.stream().map(product -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setPrice(product.getPrice());
                return dto;
            }).toList();

            ResponseWrapper<List<ProductDTO>> response =
                    new ResponseWrapper<>("M200", "Success", dtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<List<ProductDTO>> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // READ ONE
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductDTO>> getById(@PathVariable Long id) {
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());

            ResponseWrapper<ProductDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<ProductDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductDTO>> add(@RequestBody Product product) {
        try {
            Product savedProduct = repo.save(product);

            ProductDTO dto = new ProductDTO();
            dto.setId(savedProduct.getId());
            dto.setName(savedProduct.getName());
            dto.setPrice(savedProduct.getPrice());

            ResponseWrapper<ProductDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseWrapper<ProductDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<ProductDTO>> update(@PathVariable Long id,
                                                              @RequestBody Product productDetails) {
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setDescription(productDetails.getDescription());
            product.setCategory(productDetails.getCategory());
            product.setMaterial(productDetails.getMaterial());
            product.setWeight(productDetails.getWeight());
            product.setStockQuantity(productDetails.getStockQuantity());
            product.setImageUrl(productDetails.getImageUrl());
            product.setActive(productDetails.isActive());

            Product updated = repo.save(product);

            ProductDTO dto = new ProductDTO();
            dto.setId(updated.getId());
            dto.setName(updated.getName());
            dto.setPrice(updated.getPrice());

            ResponseWrapper<ProductDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<ProductDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // DELETE (soft delete)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<Void>> deleteProduct(@PathVariable Long id) {
        try {
            Product product = repo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            product.setActive(false);
            repo.save(product);

            ResponseWrapper<Void> response =
                    new ResponseWrapper<>("M200", "Success", null);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<Void> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}