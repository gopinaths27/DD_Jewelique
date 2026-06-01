package com.ddjewelique.backend.repository;

import com.ddjewelique.backend.model.Cart;
import com.ddjewelique.backend.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
}
