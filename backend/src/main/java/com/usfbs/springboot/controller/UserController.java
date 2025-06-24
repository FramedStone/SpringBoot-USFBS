package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.SportFacilityDetailResponse;
import com.usfbs.springboot.dto.SportFacilityResponse;
import com.usfbs.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;

    @Autowired
    private com.usfbs.springboot.util.PinataUtil pinataUtil;

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

    /**
     * Create a booking for a court
     */
    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request) {
        try {
            // Extract booking details from request
            String facilityName = (String) request.get("facilityName");
            String courtName = (String) request.get("courtName");
            Long startTime = request.get("startTime") instanceof Integer
                ? ((Integer) request.get("startTime")).longValue()
                : (Long) request.get("startTime");
            Long endTime = request.get("endTime") instanceof Integer
                ? ((Integer) request.get("endTime")).longValue()
                : (Long) request.get("endTime");
            String status = (String) request.getOrDefault("status", "pending");
            String userAddress = (String) request.get("userAddress"); 

            if (facilityName == null || courtName == null || startTime == null || endTime == null || userAddress == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing required fields"));
            }

            // Bundle booking details for IPFS
            Map<String, Object> bookingDetails = Map.of(
                "facilityName", facilityName,
                "courtName", courtName,
                "startTime", startTime,
                "endTime", endTime,
                "status", status,
                "userAddress", userAddress 
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            java.time.LocalDate bookingDate = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload booking receipt to IPFS via Pinata
            String ipfsHash = pinataUtil.uploadJsonToIPFS(bookingDetails, fileName);

            // Call service to create booking on-chain
            String txHash = userService.createBooking(
                ipfsHash,
                facilityName,
                courtName,
                BigInteger.valueOf(startTime),
                BigInteger.valueOf(endTime),
                status
            );

            // After bookingCreated event, update the booking receipt on IPFS,
            // unpin the old IPFS hash (ipfsHash), and upload the new one.
            // Retrieve the new IPFS hash from UserService
            String newIpfsHash = userService.getLatestBookingIpfsHash(ipfsHash);

            // update it on-chain
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                userService.updateBookingIPFSHash(ipfsHash, newIpfsHash);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "ipfsHash", newIpfsHash != null ? newIpfsHash : ipfsHash,
                "message", "Booking created successfully"
            ));
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Get a single booking for the current user by ipfsHash
     */
    @GetMapping("/bookings/{ipfsHash}")
    public ResponseEntity<?> getBookingByIpfsHash(
        @RequestHeader("user-address") String userAddress,
        @PathVariable String ipfsHash
    ) {
        try {
            if (userAddress == null || userAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User address is required"
                ));
            }
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ipfsHash is required"
                ));
            }
            Map<String, Object> booking = userService.getBookingByIpfsHash(userAddress, ipfsHash);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", booking
            ));
        } catch (Exception e) {
            logger.error("Error getting booking for user: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "Failed to retrieve booking"
            ));
        }
    }

    /**
     * Get all bookings for the current user
     */
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(@RequestHeader("user-address") String userAddress) {
        try {
            if (userAddress == null || userAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User address is required"
                ));
            }
            List<Map<String, Object>> bookings = userService.getAllBookings(userAddress);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookings,
                "count", bookings.size(),
                "message", String.format("Retrieved %d bookings for user", bookings.size())
            ));
        } catch (Exception e) {
            logger.error("Error getting user bookings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "message", "Failed to retrieve user bookings"
            ));
        }
    }

    // GET all booked timeslots for a court
    @GetMapping(
        value = "/{sportFacility}/{court}/booked-timeslots",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getBookedTimeSlotsUser(
        @PathVariable("sportFacility") String facilityName,
        @PathVariable("court") String courtName
    ) {
        try {
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Facility name is required"));
            }
            if (courtName == null || courtName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Court name is required"));
            }
            List<Map<String, Object>> slots = userService.getBookedTimeSlots(facilityName, courtName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", slots,
                "count", slots.size()
            ));
        } catch (Exception e) {
            logger.error("Error getting booked timeslots: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * Cancel a booking by ipfsHash
     */
    @PostMapping("/bookings/{ipfsHash}/cancel")
    public ResponseEntity<?> cancelBooking(
        @RequestHeader("user-address") String userAddress,
        @PathVariable String ipfsHash
    ) {
        try {
            if (userAddress == null || userAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User address is required"
                ));
            }
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ipfsHash is required"
                ));
            }
            String txHash = userService.cancelBooking(userAddress, ipfsHash);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "message", "Booking cancelled successfully"
            ));
        } catch (Exception e) {
            logger.error("Error cancelling booking for user: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
