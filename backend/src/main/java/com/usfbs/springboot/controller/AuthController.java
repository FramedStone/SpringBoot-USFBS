package com.usfbs.springboot.controller;

import com.auth0.jwt.interfaces.Claim;
import com.usfbs.springboot.dto.LoginRequest;
import com.usfbs.springboot.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // TODO: verify Web3Auth JWT or email and extract user address
        String userAddress = loginRequest.getUserAddress(); // Replace with actual extraction logic
        String role = authService.getUserRole(userAddress, managementContract);
        if ("Unregistered".equals(role)) {
            return ResponseEntity.status(403).body("User not registered in the system.");
        }
        String accessToken = authService.generateAccessToken(loginRequest.getEmail(), role);
        String refreshToken = authService.generateRefreshToken(loginRequest.getEmail(), role);
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("role", role);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        try {
            Map<String, Claim> claims = authService.verifyToken(refreshToken);
            String email = claims.get("sub").asString();
            String role = claims.get("role").asString();
            String newAccessToken = authService.generateAccessToken(email, role);
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
    }
}