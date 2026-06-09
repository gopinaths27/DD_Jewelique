package com.ddjewelique.backend.repository;

import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByOtp(String otp);        // ✅ for reset-password

    Optional<PasswordResetToken> findByCustomer(Customer customer);
}