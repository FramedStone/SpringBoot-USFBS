package com.usfbs.springboot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.usfbs.springboot.util.DateTimeUtil;
import com.auth0.jwt.interfaces.Claim;

@Service
public class EventLogService {
    
    private final ConcurrentLinkedQueue<EventLogEntry> eventLogs = new ConcurrentLinkedQueue<>();
    private static final int MAX_LOGS = 1000;
    
    @Autowired(required = false)
    private AuthService authService;
    
    public static class EventLogEntry {
        private String ipfsHash;
        private String action;
        private String fromAddress;
        private String email;
        private String role;
        private String timestamp;
        private String originalOutput;
        private LocalDateTime dateAdded;
        private String eventType;
        
        public EventLogEntry(String ipfsHash, String action, String fromAddress, String timestamp, String originalOutput, String eventType, AuthService authService) {
            this.ipfsHash = ipfsHash != null ? ipfsHash : "";
            this.action = action;
            this.fromAddress = fromAddress;
            this.timestamp = timestamp;
            this.originalOutput = originalOutput;
            this.eventType = eventType;
            this.dateAdded = LocalDateTime.now();
            
            // Enhanced resolution logic - only real data, no placeholders
            this.email = resolveEmailFromContext(fromAddress, authService);
            this.role = resolveRoleFromContext(fromAddress, authService);
        }
        
        private String resolveEmailFromContext(String address, AuthService authService) {
            // Try to get email from current JWT token first
            String emailFromJWT = getEmailFromCurrentJWT(authService);
            if (emailFromJWT != null && !emailFromJWT.trim().isEmpty()) {
                return emailFromJWT;
            }
            
            // Try to get cached email from Web3Auth login
            if (authService != null) {
                String cachedEmail = authService.getCachedEmailByAddress(address);
                if (cachedEmail != null && !cachedEmail.trim().isEmpty()) {
                    return cachedEmail;
                }
            }
            
            // Return blank if no real email found  
            return "-";
        }
        
        private String resolveRoleFromContext(String address, AuthService authService) {
            // Try to get role from current Spring Security context first
            String roleFromSecurity = getRoleFromSecurityContext();
            if (roleFromSecurity != null && !roleFromSecurity.trim().isEmpty()) {
                return roleFromSecurity;
            }
            
            // Try to get role from current JWT token
            String roleFromJWT = getRoleFromCurrentJWT(authService);
            if (roleFromJWT != null && !roleFromJWT.trim().isEmpty()) {
                return roleFromJWT;
            }
            
            // Try to get role from AuthService (blockchain-based)
            if (authService != null) {
                try {
                    String blockchainRole = authService.getUserRole(address);
                    if (blockchainRole != null && !blockchainRole.trim().isEmpty() && !"User".equals(blockchainRole)) {
                        return blockchainRole;
                    }
                } catch (Exception e) {
                    System.err.println("Failed to resolve role from blockchain for " + address + ": " + e.getMessage());
                }
            }
            
            // Return unknown if no definitive role found - no default User assignment
            return "Unknown";
        }
        
        private String getEmailFromCurrentJWT(AuthService authService) {
            try {
                String token = getCurrentJWTToken();
                if (token != null && authService != null) {
                    Map<String, Claim> claims = authService.verifyToken(token);
                    Claim emailClaim = claims.get("sub");
                    if (emailClaim != null) {
                        String email = emailClaim.asString();
                        if (email != null && !email.trim().isEmpty() && email.contains("@")) {
                            return email;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to extract email from JWT: " + e.getMessage());
            }
            return null;
        }
        
        private String getRoleFromSecurityContext() {
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getAuthorities() != null) {
                    String role = auth.getAuthorities().stream()
                        .findFirst()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .orElse(null);
                    if (role != null && !role.trim().isEmpty()) {
                        return role;
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to extract role from Security Context: " + e.getMessage());
            }
            return null;
        }
        
        private String getRoleFromCurrentJWT(AuthService authService) {
            try {
                String token = getCurrentJWTToken();
                if (token != null && authService != null) {
                    Map<String, Claim> claims = authService.verifyToken(token);
                    Claim roleClaim = claims.get("role");
                    if (roleClaim != null) {
                        String role = roleClaim.asString();
                        if (role != null && !role.trim().isEmpty()) {
                            return role;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to extract role from JWT: " + e.getMessage());
            }
            return null;
        }
        
        private String getCurrentJWTToken() {
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs != null) {
                    HttpServletRequest request = attrs.getRequest();
                    
                    // Try to get token from cookie first
                    Cookie[] cookies = request.getCookies();
                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            if ("accessToken".equals(cookie.getName())) {
                                return cookie.getValue();
                            }
                        }
                    }
                    
                    // Try to get token from Authorization header
                    String authHeader = request.getHeader("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        return authHeader.substring(7);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to extract JWT token from request: " + e.getMessage());
            }
            return null;
        }
        
        // Getters
        public String getIpfsHash() { return ipfsHash; }
        public String getAction() { return action; }
        public String getFromAddress() { return fromAddress; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
        public String getTimestamp() { return timestamp; }
        public String getOriginalOutput() { return originalOutput; }
        public LocalDateTime getDateAdded() { return dateAdded; }
        public String getEventType() { return eventType; }
    }
    
    public void addEventLog(String ipfsHash, String action, String fromAddress, 
                       BigInteger timestamp, String originalOutput, String eventType) {
        try {
            String formattedTimestamp = DateTimeUtil.formatTimestamp(timestamp);
            EventLogEntry entry = new EventLogEntry(ipfsHash, action, fromAddress, formattedTimestamp, originalOutput, eventType, authService);
            
            eventLogs.offer(entry);
            
            // Keep only the latest MAX_LOGS entries
            while (eventLogs.size() > MAX_LOGS) {
                eventLogs.poll();
            }
            
            // Enhanced logging with real data tracking
            String dataSource = determineDataSource(entry.getEmail(), entry.getRole(), fromAddress);
            System.out.println(">>> EventLogService: Added " + action + " from " + fromAddress + 
                              " (Email: " + entry.getEmail() + ", Role: " + entry.getRole() + " [" + dataSource + "])");
        } catch (Exception e) {
            System.err.println("Failed to add event log: " + e.getMessage());
        }
    }

    private String determineDataSource(String email, String role, String address) {
        boolean hasRealEmail = email != null && !email.equals("Unknown") && email.contains("@");
        boolean hasRealRole = role != null && !role.equals("Unknown");
        
        if (hasRealEmail && hasRealRole) {
            return "Authenticated User";
        } else if (hasRealRole) {
            return "Blockchain Role Only";
        } else if (hasRealEmail) {
            return "JWT Email Only";
        } else {
            return "No User Context";
        }
    }
    
    public void addEventLog(String ipfsHash, String action, String fromAddress, String timestamp, String originalOutput, String eventType) {
        EventLogEntry entry = new EventLogEntry(ipfsHash, action, fromAddress, timestamp, originalOutput, eventType, authService);
        
        eventLogs.offer(entry);
        
        // Keep only the latest MAX_LOGS entries
        while (eventLogs.size() > MAX_LOGS) {
            eventLogs.poll();
        }
        
        String dataSource = determineDataSource(entry.getEmail(), entry.getRole(), fromAddress);
        System.out.println(">>> EventLogService: Added " + action + " from " + fromAddress + " (Email: " + entry.getEmail() + ", Role: " + entry.getRole() + " [" + dataSource + "])");
    }
    
    public void addEventLogWithUserContext(String ipfsHash, String action, String fromAddress, 
                                         String timestamp, String originalOutput, String eventType,
                                         String userEmail, String userRole) {
        // Only use provided context if it's real data
        String finalEmail = (userEmail != null && !userEmail.trim().isEmpty() && userEmail.contains("@")) ? userEmail : "Unknown";
        String finalRole = (userRole != null && !userRole.trim().isEmpty()) ? userRole : "Unknown";
        
        EventLogEntry entry = new EventLogEntry(ipfsHash, action, fromAddress, timestamp, originalOutput, eventType, authService) {
            @Override
            public String getEmail() { return finalEmail; }
            @Override  
            public String getRole() { return finalRole; }
        };
        
        eventLogs.offer(entry);
        
        // Keep only the latest MAX_LOGS entries
        while (eventLogs.size() > MAX_LOGS) {
            eventLogs.poll();
        }
        
        String dataSource = determineDataSource(entry.getEmail(), entry.getRole(), fromAddress);
        System.out.println(">>> EventLogService: Added " + action + " from " + fromAddress + " (Email: " + entry.getEmail() + ", Role: " + entry.getRole() + " [" + dataSource + "])");
    }
    
    public List<EventLogEntry> getAllEventLogs() {
        return new ArrayList<>(eventLogs);
    }
    
    public List<EventLogEntry> getEventLogsByAction(String action) {
        return eventLogs.stream()
            .filter(log -> log.getAction().equalsIgnoreCase(action))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    public List<EventLogEntry> getEventLogsByType(String eventType) {
        return eventLogs.stream()
            .filter(log -> log.getEventType().equalsIgnoreCase(eventType))
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}