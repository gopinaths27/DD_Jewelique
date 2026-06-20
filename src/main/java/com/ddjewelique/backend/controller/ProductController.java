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
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // Save file locally (replace with cloud storage if needed)
            String uploadDir = "uploads/products/";
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(uploadDir + fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());
            // Generate URL (local server example)
            String imageUrl = "http://localhost:8080/" + uploadDir + fileName;

            return ResponseEntity.ok(new ResponseWrapper<>("M200", "Success", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseWrapper<>("M400", "Error", e.getMessage()));
        }
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<ResponseWrapper<List<ProductDTO>>> getProducts(Authentication auth) {
        try {
            boolean isAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            List<Product> products = repo.findByActiveTrue(); // ✅ filter active
            List<ProductDTO> dtos = products.stream().map(product -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(product.getId());
                dto.setName(product.getName());
                dto.setPrice(product.getPrice());
                dto.setCategory(product.getCategory());
                dto.setStockQuantity(product.getStockQuantity());
                dto.setDescription(product.getDescription());
                dto.setImages(product.getImages());
                dto.setWeight(product.getWeight());
                dto.setMaterial(product.getMaterial());
                dto.setActive(product.getActive());

                // ✅ map images
                if (product.getImages() != null) {
                    dto.setImages(product.getImages());
                }

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
            Product product = repo.findByIdAndActiveTrue(id)
                    .orElseThrow(() -> new RuntimeException("Product not found or inactive"));

            ProductDTO dto = new ProductDTO();
            dto.setId(product.getId());
            dto.setName(product.getName());
            dto.setPrice(product.getPrice());
            dto.setCategory(product.getCategory());
            dto.setStockQuantity(product.getStockQuantity());
            dto.setDescription(product.getDescription());
            dto.setImages(product.getImages());
            dto.setWeight(product.getWeight());
            dto.setMaterial(product.getMaterial());
            dto.setActive(product.getActive());

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
            dto.setImages(savedProduct.getImages());
            dto.setCategory(savedProduct.getCategory());
            dto.setStockQuantity(savedProduct.getStockQuantity());
            dto.setDescription(savedProduct.getDescription());
            dto.setMaterial(savedProduct.getMaterial());
            dto.setStockQuantity(savedProduct.getStockQuantity());
            dto.setWeight(savedProduct.getWeight());
            dto.setActive(savedProduct.getActive());

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

            if (productDetails.getName() != null) {
                product.setName(productDetails.getName());
            }
            if (productDetails.getPrice() != null) {
                product.setPrice(productDetails.getPrice());
            }
            if (productDetails.getDescription() != null) {
                product.setDescription(productDetails.getDescription());
            }
            if (productDetails.getCategory() != null) {
                product.setCategory(productDetails.getCategory());
            }
            if (productDetails.getMaterial() != null) {
                product.setMaterial(productDetails.getMaterial());
            }
            if (productDetails.getWeight() != null) {
                product.setWeight(productDetails.getWeight());
            }
            if (productDetails.getStockQuantity() != null) {
                product.setStockQuantity(productDetails.getStockQuantity());
            }
            if (productDetails.getActive() != null) {
                product.setActive(productDetails.getActive());
            }

            if (productDetails.getImages() != null && !productDetails.getImages().isEmpty()) {
                product.getImages().clear();                // remove old
                product.getImages().addAll(productDetails.getImages()); // add new
            }
            Product updated = repo.save(product);

            ProductDTO dto = new ProductDTO();
            dto.setId(updated.getId());
            dto.setName(updated.getName());
            dto.setPrice(updated.getPrice());
            dto.setImages(updated.getImages());
            dto.setCategory(updated.getCategory());
            dto.setStockQuantity(updated.getStockQuantity());
            dto.setDescription(updated.getDescription());
            dto.setMaterial(updated.getMaterial());
            dto.setWeight(updated.getWeight());
            dto.setActive(updated.getActive());
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