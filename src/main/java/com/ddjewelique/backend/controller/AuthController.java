package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.*;
import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.model.PasswordResetToken;
import com.ddjewelique.backend.repository.CustomerRepository;
import com.ddjewelique.backend.repository.PasswordResetTokenRepository;
import com.ddjewelique.backend.service.EmailService;
import com.ddjewelique.backend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    public AuthController(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<AuthResponse>> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtUtil.generateToken((UserDetails) authentication.getPrincipal());
            String refreshToken = jwtUtil.generateRefreshToken((UserDetails) authentication.getPrincipal());

            AuthResponse dto = new AuthResponse(accessToken, refreshToken);

            ResponseWrapper<AuthResponse> response =
                    new ResponseWrapper<>("M200", "Success", dto);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            ResponseWrapper<AuthResponse> response =
                    new ResponseWrapper<>("M400", "Technical Error / " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (jwtUtil.validateToken(refreshToken)) {
            String username = jwtUtil.getUsernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String newAccessToken = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid or expired refresh token"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseWrapper<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            PasswordResetToken token = tokenRepository.findByOtp(request.getOtp())
                    .orElseThrow(() -> new RuntimeException("Invalid OTP"));

            if (token.isExpired()) {
                throw new RuntimeException("OTP expired");
            }

            Customer customer = token.getCustomer();
            customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
            customerRepository.save(customer);

            tokenRepository.delete(token);

            ResponseWrapper<String> response =
                    new ResponseWrapper<>("M200", "Success", "Password updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<String> response =
                    new ResponseWrapper<>("M400", "Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ResponseWrapper<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            Customer customer = customerRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            tokenRepository.findByCustomer(customer).ifPresent(tokenRepository::delete);

            String otp = String.valueOf(new Random().nextInt(900000) + 100000);
            PasswordResetToken resetToken = new PasswordResetToken(otp, customer);
            tokenRepository.save(resetToken);

            emailService.sendOtp(customer.getEmail(), otp);

            ResponseWrapper<String> response =
                    new ResponseWrapper<>("M200", "Success", "OTP sent to your email");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ResponseWrapper<String> response =
                    new ResponseWrapper<>("M400", "Error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
