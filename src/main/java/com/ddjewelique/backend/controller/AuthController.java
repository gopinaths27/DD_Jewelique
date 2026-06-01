package com.ddjewelique.backend.controller;

import com.ddjewelique.backend.dto.AuthRequest;
import com.ddjewelique.backend.dto.AuthResponse;
import com.ddjewelique.backend.dto.CustomerDTO;
import com.ddjewelique.backend.dto.ResponseWrapper;
import com.ddjewelique.backend.model.Customer;
import com.ddjewelique.backend.service.CustomUserDetailsService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authManager;

    private final UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

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
}
