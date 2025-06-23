package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.SportFacilityRequest;
import com.usfbs.springboot.dto.SportFacilityResponse;
import com.usfbs.springboot.dto.SportFacilityDetailResponse;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;
import com.usfbs.springboot.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    private final Management managementContract;
    private final RestTemplate rest;
    private final AdminService adminService;

    @Autowired
    public AdminController(Management managementContract, RestTemplate restTemplate, AdminService adminService) {
        this.managementContract = managementContract;
        this.rest = restTemplate;
        this.adminService = adminService;
    }

    @GetMapping("/get-announcements")
    public List<AnnouncementItem> getAnnouncementsFromContract() throws Exception {
        try {
            List<AnnouncementItem> announcements = adminService.getAllAnnouncements();
            
            if (announcements.isEmpty()) {
                System.out.println("No announcements found or all announcements have invalid IPFS data");
            }
            
            return announcements;
            
        } catch (Exception e) {
            System.err.println("getAnnouncementsFromContract error: " + e.getMessage());
            
            if (e.getMessage().contains("No Announcement found in blockchain")) {
                return new ArrayList<>();
            }
            
            // Log the error but return empty list to prevent 500 errors
            System.err.println("Returning empty list due to error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @PostMapping(
        value = "/upload-announcement",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadAnnouncement(
        @RequestParam("file") MultipartFile file,
        @RequestParam("title") String title,
        @RequestParam("startDate") long startDate,
        @RequestParam("endDate") long endDate
    ) {
        try {
            // Validate input parameters
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "File cannot be empty")
                );
            }
            
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Title cannot be empty")
                );
            }
            
            if (startDate >= endDate) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "End date must be after start date")
                );
            }

            String txHash = adminService.uploadAnnouncement(file, title, startDate, endDate);
            return ResponseEntity.ok().body(
                java.util.Map.of(
                    "message", "Announcement uploaded successfully", 
                    "txHash", txHash,
                    "title", title,
                    "startDate", startDate,
                    "endDate", endDate
                )
            );
        } catch (Exception e) {
            System.err.println("uploadAnnouncement error: " + e.getMessage());
            return ResponseEntity.status(500).body(
                java.util.Map.of("error", "Upload failed", "details", e.getMessage())
            );
        }
    }

    @PutMapping(
        value = "/update-announcement",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateAnnouncement(
        @RequestParam("oldIpfsHash") String oldIpfsHash,
        @RequestParam(value = "file", required = false) MultipartFile newFile,
        @RequestParam("title") String newTitle,
        @RequestParam("startDate") long newStartDate,
        @RequestParam("endDate") long newEndDate
    ) {
        try {
            // Validate input parameters
            if (oldIpfsHash == null || oldIpfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Old IPFS hash cannot be empty")
                );
            }
            
            if (newTitle == null || newTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Title cannot be empty")
                );
            }
            
            if (newStartDate >= newEndDate) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "End date must be after start date")
                );
            }

            // Always use unified updateAnnouncement method
            String txHash = adminService.updateAnnouncement(
                oldIpfsHash, newFile, newTitle, newStartDate, newEndDate
            );
            
            return ResponseEntity.ok().body(
                java.util.Map.of(
                    "message", "Announcement updated successfully",
                    "txHash", txHash,
                    "oldIpfsHash", oldIpfsHash,
                    "newTitle", newTitle,
                    "newStartDate", newStartDate,
                    "newEndDate", newEndDate,
                    "fileUpdated", newFile != null && !newFile.isEmpty()
                )
            );
        } catch (Exception e) {
            System.err.println("updateAnnouncement error: " + e.getMessage());
            return ResponseEntity.status(500).body(
                java.util.Map.of("error", "Update failed", "details", e.getMessage())
            );
        }
    }

    @DeleteMapping(
        value = "/delete-announcement",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> deleteAnnouncement(@RequestParam("ipfsHash") String ipfsHash) {
        try {
            // Validate input parameters
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "IPFS hash cannot be empty")
                );
            }

            String txHash = adminService.deleteAnnouncement(ipfsHash);
            return ResponseEntity.ok().body(
                java.util.Map.of(
                    "message", "Announcement deleted successfully",
                    "txHash", txHash,
                    "deletedIpfsHash", ipfsHash
                )
            );
        } catch (Exception e) {
            System.err.println("deleteAnnouncement error: " + e.getMessage());
            return ResponseEntity.status(500).body(
                java.util.Map.of("error", "Deletion failed", "details", e.getMessage())
            );
        }
    }

    // Sport Facility Management Endpoints
    @PostMapping("/sport-facilities")
    public ResponseEntity<?> addSportFacility(@RequestBody SportFacilityRequest request) {
        try {
            List<SportFacility.court> courts = request.getCourts().stream()
                .map(courtReq -> new SportFacility.court(
                    courtReq.getName(),
                    courtReq.getEarliestTime(),
                    courtReq.getLatestTime(),
                    courtReq.getStatus()
                ))
                .collect(Collectors.toList());
            
            String result = adminService.addSportFacility(
                request.getFacilityName(),
                request.getFacilityLocation(),
                request.getFacilityStatus(),
                courts
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities")
    public ResponseEntity<?> getAllSportFacilities() {
        try {
            List<SportFacilityResponse> facilities = adminService.getAllSportFacilities();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", facilities
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities/{facilityName}")
    public ResponseEntity<?> getSportFacility(@PathVariable String facilityName) {
        try {
            SportFacilityDetailResponse facility = adminService.getSportFacility(facilityName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", facility
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/sport-facilities/{facilityName}/name")
    public ResponseEntity<?> updateSportFacilityName(
            @PathVariable String facilityName,
            @RequestBody Map<String, String> request) {
        try {
            String newName = request.get("newName");
            String result = adminService.updateSportFacilityName(facilityName, newName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/sport-facilities/{facilityName}/location")
    public ResponseEntity<?> updateSportFacilityLocation(
            @PathVariable String facilityName,
            @RequestBody Map<String, String> request) {
        try {
            String newLocation = request.get("location");
            String result = adminService.updateSportFacilityLocation(facilityName, newLocation);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/sport-facilities/{facilityName}/status")
    public ResponseEntity<?> updateSportFacilityStatus(
            @PathVariable String facilityName,
            @RequestBody Map<String, Integer> request) {
        try {
            BigInteger status = BigInteger.valueOf(request.get("status"));
            String result = adminService.updateSportFacilityStatus(facilityName, status);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/sport-facilities/{facilityName}")
    public ResponseEntity<?> deleteSportFacility(@PathVariable String facilityName) {
        try {
            String result = adminService.deleteSportFacility(facilityName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities/{facilityName}/courts")
    public ResponseEntity<?> getAllCourts(@PathVariable String facilityName) {
        try {
            List<SportFacility.court> courts = adminService.getAllCourts(facilityName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courts
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities/{facilityName}/courts/{courtName}")
    public ResponseEntity<?> getCourt(@PathVariable String facilityName, @PathVariable String courtName) {
        try {
            SportFacility.court court = adminService.getCourt(facilityName, courtName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", court
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities/{facilityName}/courts/{courtName}/time-range")
    public ResponseEntity<?> getCourtTimeRange(
        @PathVariable String facilityName,
        @PathVariable String courtName
    ) {
        try {
            Map<String, Object> timeRange = adminService.getCourtAvailableTimeRange(facilityName, courtName);
            
            // Ensure status is included in response
            if (!timeRange.containsKey("status")) {
                timeRange.put("status", "UNKNOWN");
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", timeRange,
                "message", "Court time range retrieved successfully"
            ));
        } catch (Exception e) {
            logger.error("Error getting court time range: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/sport-facilities/{facilityName}/courts")
    public ResponseEntity<?> addCourts(
            @PathVariable String facilityName,
            @RequestBody Map<String, List<Map<String, Object>>> requestBody) {
        try {
            List<Map<String, Object>> courtsData = requestBody.get("courts");
            
            // Validate input
            if (courtsData == null || courtsData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "No courts provided"
                ));
            }
            
            // Validate court names are unique within this request
            Set<String> courtNames = new HashSet<>();
            for (Map<String, Object> courtData : courtsData) {
                String courtName = (String) courtData.get("name");
                if (courtNames.contains(courtName)) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Duplicate court name in request: " + courtName
                    ));
                }
                courtNames.add(courtName);
            }
            
            List<SportFacility.court> courts = courtsData.stream()
                .map(courtData -> new SportFacility.court(
                    (String) courtData.get("name"),
                    BigInteger.valueOf(((Number) courtData.get("earliestTime")).longValue()),
                    BigInteger.valueOf(((Number) courtData.get("latestTime")).longValue()),
                    BigInteger.valueOf(((Number) courtData.get("status")).longValue())
                ))
                .collect(Collectors.toList());
            
            String result = adminService.addCourtsToFacility(facilityName, courts);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "courtsAdded", courts.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/sport-facilities/{facilityName}/courts/{courtName}")
    public ResponseEntity<?> deleteCourt(
            @PathVariable String facilityName,
            @PathVariable String courtName) {
        try {
            String result = adminService.deleteCourt(facilityName, courtName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/sport-facilities/{facilityName}/courts/{courtName}/time")
    public ResponseEntity<?> updateCourtTime(
            @PathVariable String facilityName,
            @PathVariable String courtName,
            @RequestBody Map<String, Long> requestBody) {
        try {
            Long earliestTime = requestBody.get("earliestTime");
            Long latestTime = requestBody.get("latestTime");
            
            String result = adminService.updateCourtTime(facilityName, courtName, earliestTime, latestTime);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/sport-facilities/{facilityName}/courts/{courtName}/status")
    public ResponseEntity<?> updateCourtStatus(
        @PathVariable String facilityName,
        @PathVariable String courtName,
        @RequestBody Map<String, String> request
    ) {
        try {
            String status = request.get("status");
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Status is required"
                ));
            }
            
            String result = adminService.updateCourtStatus(facilityName, courtName, status);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
        } catch (Exception e) {
            logger.error("Error updating court status: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities/{facilityName}/courts/{courtName}/time-range-with-bookings")
    public ResponseEntity<?> getCourtTimeRangeWithBookings(
        @PathVariable String facilityName,
        @PathVariable String courtName
    ) {
        try {
            Map<String, Object> courtInfo = adminService.getCourtAvailableTimeRangeWithBookings(facilityName, courtName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", courtInfo,
                "message", "Court information with bookings retrieved successfully"
            ));
        } catch (Exception e) {
            logger.error("Error getting court information with bookings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/sport-facilities/{facilityName}/courts/{courtName}/booked-slots")
    public ResponseEntity<?> getCourtBookedSlots(
        @PathVariable String facilityName,
        @PathVariable String courtName
    ) {
        try {
            List<Map<String, Object>> bookedSlots = adminService.getBookedTimeSlots(facilityName, courtName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookedSlots,
                "message", String.format("Retrieved %d booked time slots", bookedSlots.size())
            ));
        } catch (Exception e) {
            logger.error("Error getting booked time slots: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // User Management Endpoints
    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        try {
            List<Map<String, Object>> users = adminService.getAllUsers();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", users
            ));
        } catch (Exception e) {
            logger.error("Error getting users: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/users/ban")
    public ResponseEntity<?> banUser(@RequestBody Map<String, String> request) {
        try {
            String userAddress = request.get("userAddress");
            String reason = request.get("reason");
            
            if (userAddress == null || userAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User address is required"
                ));
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Ban reason is required"
                ));
            }
            
            String result = adminService.banUser(userAddress, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "bannedUser", userAddress,
                "reason", reason
            ));
        } catch (Exception e) {
            logger.error("Error banning user: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/users/unban")
    public ResponseEntity<?> unbanUser(@RequestBody Map<String, String> request) {
        try {
            String userAddress = request.get("userAddress");
            String reason = request.get("reason");
            
            if (userAddress == null || userAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User address is required"
                ));
            }
            
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Unban reason is required"
                ));
            }
            
            String result = adminService.unbanUser(userAddress, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "unbannedUser", userAddress,
                "reason", reason
            ));
        } catch (Exception e) {
            logger.error("Error unbanning user: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/users/{userAddress}/status")
    public ResponseEntity<?> getUserStatus(@PathVariable String userAddress) {
        try {
            Map<String, Object> userStatus = adminService.getUserStatus(userAddress);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", userStatus
            ));
        } catch (Exception e) {
            logger.error("Error getting user status: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping(
        value = "/bookings/create",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createBooking(
        @RequestParam("facilityName") String facilityName,
        @RequestParam("courtName") String courtName,
        @RequestParam("userAddress") String userAddress,
        @RequestParam("startTime") long startTime,
        @RequestParam("endTime") long endTime,
        @RequestParam("eventDescription") String eventDescription,
        @RequestParam(value = "receiptFile", required = false) MultipartFile receiptFile
    ) {
        try {
            // Validate required parameters
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Facility name is required"
                ));
            }
            
            if (courtName == null || courtName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Court name is required"
                ));
            }
            
            if (userAddress == null || userAddress.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "User address is required"
                ));
            }
            
            if (eventDescription == null || eventDescription.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Event description is required"
                ));
            }
            
            if (startTime >= endTime) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "End time must be after start time"
                ));
            }

            // Validate file if provided
            if (receiptFile != null && !receiptFile.isEmpty()) {
                // Check file size (max 10MB)
                if (receiptFile.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Receipt file size cannot exceed 10MB"
                    ));
                }
                
                // Check file type
                String contentType = receiptFile.getContentType();
                if (contentType == null || 
                    (!contentType.startsWith("image/") && 
                     !contentType.equals("application/pdf"))) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Receipt file must be an image or PDF"
                    ));
                }
            }

            String txHash = adminService.createBooking(
                facilityName, courtName, userAddress, 
                startTime, endTime, eventDescription, receiptFile
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Booking created successfully",
                "txHash", txHash,
                "facilityName", facilityName,
                "courtName", courtName,
                "userAddress", userAddress,
                "startTime", startTime,
                "endTime", endTime,
                "eventDescription", eventDescription,
                "hasReceiptFile", receiptFile != null && !receiptFile.isEmpty()
            ));

        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/bookings/{manifestCid}/details")
    public ResponseEntity<?> getBookingDetails(@PathVariable String manifestCid) {
        try {
            if (manifestCid == null || manifestCid.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Manifest CID is required"
                ));
            }

            Map<String, Object> bookingDetails = adminService.getBookingWithDetails(manifestCid);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookingDetails,
                "message", "Booking details retrieved successfully"
            ));

        } catch (Exception e) {
            logger.error("Error getting booking details: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings(
        @RequestParam(value = "facilityName", required = false) String facilityName,
        @RequestParam(value = "courtName", required = false) String courtName,
        @RequestParam(value = "status", required = false) String status,
        @RequestParam(value = "userAddress", required = false) String userAddress
    ) {
        try {
            List<Map<String, Object>> bookings;
            
            // Use filtering if any filter parameters are provided
            if (facilityName != null || courtName != null || status != null || userAddress != null) {
                bookings = adminService.getBookingsWithFilter(facilityName, courtName, status, userAddress);
            } else {
                bookings = adminService.getAllBookings();
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookings,
                "count", bookings.size(),
                "message", String.format("Retrieved %d bookings", bookings.size())
            ));
            
        } catch (Exception e) {
            logger.error("Error getting bookings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/bookings/{bookingId}")
    public ResponseEntity<?> getBooking(@PathVariable Long bookingId) {
        try {
            if (bookingId == null || bookingId < 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Valid booking ID is required"
                ));
            }
            
            Map<String, Object> booking = adminService.getBookingById(bookingId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", booking,
                "message", "Booking details retrieved successfully"
            ));
            
        } catch (Exception e) {
            logger.error("Error getting booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/bookings/{bookingId}/reject")
    public ResponseEntity<?> rejectBooking(
        @PathVariable Long bookingId,
        @RequestBody Map<String, String> request
    ) {
        try {
            String reason = request.get("reason");
            
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Rejection reason is required"
                ));
            }
            
            String result = adminService.rejectBooking(bookingId, reason);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "bookingId", bookingId,
                "reason", reason
            ));
            
        } catch (Exception e) {
            logger.error("Error rejecting booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/bookings/{bookingId}/note")
    public ResponseEntity<?> attachBookingNote(
        @PathVariable Long bookingId,
        @RequestBody Map<String, String> request
    ) {
        try {
            String note = request.get("note");
            
            if (note == null || note.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Note content is required"
                ));
            }
            
            String result = adminService.attachBookingNote(bookingId, note);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result,
                "bookingId", bookingId,
                "note", note
            ));
            
        } catch (Exception e) {
            logger.error("Error attaching note to booking {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/bookings/update-status")
    public ResponseEntity<?> updateAllBookingStatus() {
        try {
            String result = adminService.updateAllBookingStatus();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", result
            ));
            
        } catch (Exception e) {
            logger.error("Error updating booking statuses: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}

