package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.CustomerDTO;
import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public CustomerController(CustomerRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<CustomerDTO>>> getAll() {
        try {
            List<CustomerDTO> dtos = repo.findAll().stream().map(customer -> {
                CustomerDTO dto = new CustomerDTO();
                dto.setId(customer.getId());
                dto.setFirstName(customer.getFirstName());
                dto.setLastName(customer.getLastName());
                dto.setEmail(customer.getEmail());
                return dto;
            }).toList();

            ResponseWrapper<List<CustomerDTO>> response =
                    new ResponseWrapper<>("M200", "Success", dtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<List<CustomerDTO>> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<CustomerDTO>> register(@RequestBody Customer customer) {
        try {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            customer.setRole("ROLE_USER");
            Customer saved = repo.save(customer);

            CustomerDTO dto = new CustomerDTO();
            dto.setId(saved.getId());
            dto.setFirstName(saved.getFirstName());
            dto.setLastName(saved.getLastName());
            dto.setEmail(saved.getEmail());

            ResponseWrapper<CustomerDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseWrapper<CustomerDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseWrapper<CustomerDTO>> registerAdmin(@RequestBody Customer customer) {
        try {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
            customer.setRole("ROLE_ADMIN");
            Customer saved = repo.save(customer);

            CustomerDTO dto = new CustomerDTO();
            dto.setId(saved.getId());
            dto.setFirstName(saved.getFirstName());
            dto.setLastName(saved.getLastName());
            dto.setEmail(saved.getEmail());

            ResponseWrapper<CustomerDTO> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseWrapper<CustomerDTO> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}