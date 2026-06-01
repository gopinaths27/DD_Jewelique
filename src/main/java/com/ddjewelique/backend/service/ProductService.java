package com.ddjewelique.backend.service;

import com.ddjewelique.backend.exception.ProductNotFoundException;
import com.ddjewelique.backend.model.Product;
import com.ddjewelique.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Product getProductById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + id + " not found"));
    }

    public Product addProduct(Product product) {
        validateProduct(product);
        return repo.save(product);
    }

    public Product updateProduct(Long id, Product product) {
        Product existing = getProductById(id);

        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setCategory(product.getCategory());
        existing.setMaterial(product.getMaterial());
        existing.setWeight(product.getWeight());
        existing.setStockQuantity(product.getStockQuantity());
        existing.setImageUrl(product.getImageUrl());
        existing.setActive(product.isActive());

        validateProduct(existing);
        return repo.save(existing);
    }

    public void deleteProduct(Long id) {
        if (!repo.existsById(id)) {
            throw new ProductNotFoundException("Product with id " + id + " not found");
        }
        repo.deleteById(id);
    }

    private void validateProduct(Product product) {
        if (product.getPrice() == null || product.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
    }
}
