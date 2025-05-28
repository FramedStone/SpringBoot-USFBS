package com.usfbs.springboot.controller;

import com.usfbs.springboot.service.AdminService;
import com.usfbs.springboot.service.AuthService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService; 

    @Autowired
    public AdminController(AdminService adminService, AuthService authService) { 
        this.adminService = adminService;
        this.authService = authService;
    }

    @PostMapping("/api/admin/add-user")
    public ResponseEntity<?> addUser(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, String> body) {
        String token = authHeader.replace("Bearer ", "");
        String role = authService.verifyToken(token).get("role").asString();
        if (!"Admin".equals(role)) {
            return ResponseEntity.status(403).body("Forbidden: Admins only");
        }
        try {
            adminService.addUser(body.get("userAddress"));
            return ResponseEntity.ok("User added");
        } catch (Exception e) {
            // TODO: Proper error logging
            return ResponseEntity.status(500).body("Failed to add user: " + e.getMessage());
        }
    }

    @PostMapping("/api/admin/ban-user")
    public ResponseEntity<?> banUser(@RequestBody Map<String, String> body) {
        try {
            adminService.banUser(body.get("userAddress"));
            return ResponseEntity.ok("User banned");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to ban user: " + e.getMessage());
        }
    }

    @PostMapping("/api/admin/unban-user")
    public ResponseEntity<?> unbanUser(@RequestBody Map<String, String> body) {
        try {
            adminService.unbanUser(body.get("userAddress"));
            return ResponseEntity.ok("User unbanned");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to unban user: " + e.getMessage());
        }
    }
}

