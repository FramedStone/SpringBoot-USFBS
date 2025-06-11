package com.usfbs.springboot.controller;

import com.auth0.jwt.interfaces.Claim;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.dto.LoginRequest;
import com.usfbs.springboot.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(
    origins = "${cors.allowed-origins}",  
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS }
)
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String email = loginRequest.getEmail();
        String userAddress = loginRequest.getUserAddress();

        if (email == null || !email.matches("^[\\w.-]+@(student\\.)?mmu\\.edu\\.my$")) {
            return ResponseEntity.status(400).body("Invalid MMU email address.");
        }

        String role;
        try {
            role = authService.getUserRole(userAddress);
            
            // Update email mapping for event logging
            authService.updateUserEmailMapping(userAddress, email);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }

        List<String> permissions = authService.getPermissions(role);
        
        // Generate tokens
        String accessToken = authService.generateAccessToken(email, role, userAddress);
        String refreshToken = authService.generateRefreshToken(email, role, userAddress);

        // Set secure cookies
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(authService.getAccessExpiry())
                .sameSite("Lax")
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(authService.getRefreshExpiry())
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Login successful");
        responseBody.put("email", email);
        responseBody.put("role", role);
        responseBody.put("address", userAddress);
        responseBody.put("permissions", permissions);
        responseBody.put("accessToken", accessToken);
        responseBody.put("refreshToken", refreshToken);

        return ResponseEntity.ok(responseBody);
    }

    private String extractToken(HttpServletRequest req, String name) {
        if (req.getCookies()!=null) {
            for (Cookie c: req.getCookies()) {
                if (name.equals(c.getName())) return c.getValue();
            }
        }
        String h = req.getHeader("Authorization");
        return h!=null && h.startsWith("Bearer ") ? h.substring(7) : null;
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String,String> b) {
        String rt = b.get("refreshToken");
        if (authService.isRefreshTokenRevoked(rt)) {
            return ResponseEntity.status(401).body("Refresh token revoked");
        }
        try {
            Map<String, Claim> claims = authService.verifyToken(rt);  
            String email = claims.get("sub").asString();
            String role = claims.get("role").asString();
            String address = claims.get("address").asString();
            String newAccessToken = authService.generateAccessToken(email, role, address);
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(HttpServletRequest request) {
        try {
            // Extract JWT from cookie or Authorization header
            String token = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if ("accessToken".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            if (token == null) {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                }
            }
            if (token == null) {
                return ResponseEntity.status(401).body("No access token provided");
            }

            Map<String, Claim> claims = authService.verifyToken(token);
            String email = claims.get("sub").asString();
            String role = claims.get("role").asString();
            String address = claims.get("address").asString();

            Map<String, Object> resp = new HashMap<>();
            resp.put("email", email);
            resp.put("role", role);
            resp.put("address", address);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid or expired token");
        }
    }

    /**
     * Logout endpoint: clears auth cookies
     * TODO: consider rate-limiting if needed
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req, HttpServletResponse res) {
        try {
            String accessToken = extractToken(req, "accessToken");
            String refreshToken = extractToken(req, "refreshToken");
            
            // Extract user address from token before revoking
            if (accessToken != null) {
                try {
                    Map<String, Claim> claims = authService.verifyToken(accessToken);
                    String userAddress = claims.get("address").asString();
                    
                    // Clear user mapping on logout
                    authService.clearUserMapping(userAddress);
                } catch (Exception e) {
                    System.err.println("Failed to extract user address from token during logout: " + e.getMessage());
                }
            }

            authService.revokeAccessToken(accessToken);
            authService.revokeRefreshToken(refreshToken);

            // Clear cookies
            ResponseCookie clearAccessCookie = ResponseCookie.from("accessToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            ResponseCookie clearRefreshCookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            res.addHeader(HttpHeaders.SET_COOKIE, clearAccessCookie.toString());
            res.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie.toString());

            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Logout failed: " + e.getMessage());
        }
    }
}