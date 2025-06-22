package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.SportFacilityDetailResponse;
import com.usfbs.springboot.dto.SportFacilityResponse;
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

    /**
     * Get all sport facilities for users
     */
    @GetMapping("/sport-facilities")
    public ResponseEntity<?> getSportFacilities() {
        try {
            List<SportFacilityResponse> facilities = userService.getSportFacilities();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", facilities,
                "count", facilities.size(),
                "message", String.format("Retrieved %d sport facilities", facilities.size())
            ));
            
        } catch (Exception e) {
            logger.error("Error getting sport facilities for user: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "Failed to retrieve sport facilities"
            ));
        }
    }

    /**
     * Get sport facility details with courts for users
     */
    @GetMapping("/sport-facilities/{facilityName}/details")
    public ResponseEntity<?> getSportFacilityDetails(@PathVariable String facilityName) {
        try {
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Facility name is required",
                    "message", "Please provide a valid facility name"
                ));
            }
            
            SportFacilityDetailResponse facilityDetails = userService.getSportFacilityWithCourts(facilityName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", facilityDetails,
                "courtsCount", facilityDetails.getCourts().size(),
                "message", String.format("Retrieved facility '%s' with %d courts", 
                    facilityDetails.getName(), facilityDetails.getCourts().size())
            ));
            
        } catch (Exception e) {
            logger.error("Error getting sport facility details '{}' for user: {}", facilityName, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "facilityName", facilityName,
                "message", "Failed to retrieve sport facility details"
            ));
        }
    }
}
