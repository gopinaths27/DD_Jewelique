package com.ddjewelique.backend.service;

import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private CustomerRepository customerRepo;

//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        Customer customer = customerRepo.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        return User.builder()
//                .username(customer.getEmail())
//                .password(customer.getPassword())
//                .roles(customer.getRole().replace("ROLE_", "")) // Spring expects plain role name
//                .build();
//    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return customerRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found: " + email));
    }

}
