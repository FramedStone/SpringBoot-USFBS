package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;

    /**
     * Get all announcements for users
     */
    @GetMapping("/announcements")
    public ResponseEntity<?> getAnnouncements() {
        try {
            List<AnnouncementItem> announcements = userService.getAnnouncements();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", announcements,
                "count", announcements.size(),
                "message", String.format("Retrieved %d announcements", announcements.size())
            ));
            
        } catch (Exception e) {
            logger.error("Error getting announcements for user: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "Failed to retrieve announcements"
            ));
        }
    }
}
