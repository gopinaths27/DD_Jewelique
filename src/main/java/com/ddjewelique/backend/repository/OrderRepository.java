package com.ddjewelique.backend.repository;

import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.Order;
import com.ddjewelique.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomer(Customer customer);
}

