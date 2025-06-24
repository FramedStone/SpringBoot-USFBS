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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @PostMapping(
        value = "/sport-facilities",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> addSportFacility(
        @RequestParam("facilityName") String facilityName,
        @RequestParam("facilityLocation") String facilityLocation,
        @RequestParam("facilityStatus") Integer facilityStatusInt,
        @RequestParam("facilityCourts") String facilityCourtsJson,
        @RequestParam(value = "image", required = false) MultipartFile imageFile // <-- optional
    ) {
        try {
            // Validate input parameters
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Facility name is required"));
            }
            if (facilityLocation == null || facilityLocation.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Facility location is required"));
            }
            if (facilityStatusInt == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Facility status is required"));
            }
            if (facilityCourtsJson == null || facilityCourtsJson.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Facility courts are required"));
            }
            // Image is now optional

            // Upload image to IPFS if present
            String imageIPFS = "";
            if (imageFile != null && !imageFile.isEmpty()) {
                imageIPFS = adminService.uploadFacilityImageToIPFS(imageFile);
            }

            // Parse courts JSON string to List<Map<String, Object>>
            List<Map<String, Object>> courtsList = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(facilityCourtsJson, List.class);

            // Convert courtsList to List<SportFacility.court>
            List<com.usfbs.springboot.contracts.SportFacility.court> facilityCourts =
                courtsList.stream().map(courtMap -> {
                    String name = (String) courtMap.get("name");
                    Integer earliestTime = (Integer) courtMap.get("earliestTime");
                    Integer latestTime = (Integer) courtMap.get("latestTime");
                    Integer status = (Integer) courtMap.get("status");
                    return new com.usfbs.springboot.contracts.SportFacility.court(
                        name,
                        BigInteger.valueOf(earliestTime),
                        BigInteger.valueOf(latestTime),
                        BigInteger.valueOf(status)
                    );
                }).toList();

            String result = adminService.addSportFacility(
                facilityName,
                facilityLocation,
                imageIPFS,
                BigInteger.valueOf(facilityStatusInt),
                facilityCourts
            );
            return ResponseEntity.ok(Map.of("success", true, "message", result, "imageIPFS", imageIPFS));
        } catch (Exception e) {
            logger.error("Error adding sport facility: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @GetMapping(
        value = "/sport-facilities",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAllSportFacilities(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String name
    ) {
        try {
            List<SportFacilityResponse> facilities = adminService.getAllSportFacilities();

            // Filtering
            if (status != null && !status.isEmpty()) {
                facilities = facilities.stream()
                    .filter(f -> f.getStatus().equalsIgnoreCase(status))
                    .toList();
            }
            if (name != null && !name.isEmpty()) {
                facilities = facilities.stream()
                    .filter(f -> f.getName().toLowerCase().contains(name.toLowerCase()))
                    .toList();
            }

            // Pagination
            int fromIndex = Math.max(0, page * size);
            int toIndex = Math.min(fromIndex + size, facilities.size());
            List<SportFacilityResponse> paged = fromIndex < toIndex ? facilities.subList(fromIndex, toIndex) : List.of();

            Map<String, Object> response = Map.of(
                "success", true,
                "data", paged,
                "page", page,
                "size", size,
                "total", facilities.size()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all sport facilities: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping(
        value = "/sport-facilities",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateSportFacility(@RequestBody Map<String, Object> request) {
        try {
            String oldName = (String) request.get("oldName");
            String newName = (String) request.getOrDefault("newName", "");
            String newLocation = (String) request.getOrDefault("newLocation", "");
            String newImageIPFS = (String) request.getOrDefault("newImageIPFS", "");
            Integer newStatusInt = request.get("newStatus") != null ? (Integer) request.get("newStatus") : 0;

            if (oldName == null || oldName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Old facility name is required"));
            }

            String result = adminService.updateSportFacility(
                oldName,
                newName,
                newLocation,
                newImageIPFS,
                java.math.BigInteger.valueOf(newStatusInt)
            );
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            logger.error("Error updating sport facility: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping(
        value = "/sport-facilities/{facilityName}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> deleteSportFacility(@PathVariable("facilityName") String facilityName) {
        try {
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Facility name is required"));
            }
            String result = adminService.deleteSportFacility(facilityName);
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            logger.error("Error deleting sport facility: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping(
        value = "/{sportFacility}/courts",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> addCourt(
        @PathVariable("sportFacility") String facilityName,
        @RequestBody Map<String, Object> request
    ) {
        try {
            List<Map<String, Object>> courtsList = (List<Map<String, Object>>) request.get("courts");
            if (courtsList == null || courtsList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Courts list is required"));
            }

            List<com.usfbs.springboot.contracts.SportFacility.court> courts =
                courtsList.stream().map(courtMap -> {
                    String name = (String) courtMap.get("name");
                    Integer earliestTime = (Integer) courtMap.get("earliestTime");
                    Integer latestTime = (Integer) courtMap.get("latestTime");
                    Integer status = (Integer) courtMap.get("status");
                    return new com.usfbs.springboot.contracts.SportFacility.court(
                        name,
                        BigInteger.valueOf(earliestTime),
                        BigInteger.valueOf(latestTime),
                        BigInteger.valueOf(status)
                    );
                }).toList();

            String result = adminService.addCourt(facilityName, courts);
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            logger.error("Error adding court(s): {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    
    // GET single court by facility and court name
    @GetMapping(
        value = "/{sportFacility}/{court}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getCourt(
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
            var court = adminService.getCourt(facilityName, courtName);
            return ResponseEntity.ok(Map.of("success", true, "data", List.of(court)));
        } catch (Exception e) {
            logger.error("Error getting court: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // GET all courts for a facility
    @GetMapping(
        value = "/{sportFacility}/courts",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAllCourts(
        @PathVariable("sportFacility") String facilityName
    ) {
        try {
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Facility name is required"));
            }
            var courts = adminService.getAllCourts(facilityName);
            return ResponseEntity.ok(Map.of("success", true, "data", courts));
        } catch (Exception e) {
            logger.error("Error getting courts: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // GET available time range for a court in a facility
    @GetMapping(
        value = "/{sportFacility}/{court}/time-slots",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getAvailableTimeRange(
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
            // Call the contract via service
            var tuple = adminService.getAvailableTimeRange(facilityName, courtName);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "earliestTime", tuple.get(0),
                "latestTime", tuple.get(1)
            ));
        } catch (Exception e) {
            logger.error("Error getting available time range: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PutMapping(
        value = "/{sportFacility}/courts",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateCourt(
        @PathVariable("sportFacility") String facilityName,
        @RequestBody Map<String, Object> request
    ) {
        try {
            String oldCourtName = (String) request.get("oldCourtName");
            String newCourtName = (String) request.getOrDefault("newCourtName", "");
            Integer earliestTime = request.get("earliestTime") != null ? (Integer) request.get("earliestTime") : 0;
            Integer latestTime = request.get("latestTime") != null ? (Integer) request.get("latestTime") : 0;
            Integer statusInt = request.get("status") != null ? (Integer) request.get("status") : 0;

            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Facility name is required"));
            }
            if (oldCourtName == null || oldCourtName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Old court name is required"));
            }

            String result = adminService.updateCourt(
                facilityName,
                oldCourtName,
                newCourtName,
                java.math.BigInteger.valueOf(earliestTime),
                java.math.BigInteger.valueOf(latestTime),
                java.math.BigInteger.valueOf(statusInt)
            );
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            logger.error("Error updating court: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @DeleteMapping(
        value = "/{sportFacility}/courts",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> deleteCourt(
        @PathVariable("sportFacility") String facilityName,
        @RequestParam("courtName") String courtName
    ) {
        try {
            if (facilityName == null || facilityName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Facility name is required"));
            }
            if (courtName == null || courtName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Court name is required"));
            }

            String result = adminService.deleteCourt(facilityName, courtName);
            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            logger.error("Error deleting court: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
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

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> request) {
        try {
            String ipfsHash = (String) request.get("ipfsHash");
            String facilityName = (String) request.get("facilityName");
            String courtName = (String) request.get("courtName");
            Long startTime = request.get("startTime") instanceof Integer
                ? ((Integer) request.get("startTime")).longValue()
                : (Long) request.get("startTime");
            Long endTime = request.get("endTime") instanceof Integer
                ? ((Integer) request.get("endTime")).longValue()
                : (Long) request.get("endTime");

            if (ipfsHash == null || facilityName == null || courtName == null || startTime == null || endTime == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Missing required fields"));
            }

            String txHash = adminService.createBooking(
                ipfsHash,
                facilityName,
                courtName,
                BigInteger.valueOf(startTime),
                BigInteger.valueOf(endTime)
            );
            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
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

    @GetMapping("/bookings/{ipfsHash}")
    public ResponseEntity<?> getBookingByIpfsHash(@PathVariable String ipfsHash) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "ipfsHash is required"));
            }
            Map<String, Object> booking = adminService.getBookingByIpfsHash(ipfsHash);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", booking
            ));
        } catch (Exception e) {
            logger.error("Error getting booking: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<Map<String, Object>> bookings = adminService.getAllBookings();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookings
            ));
        } catch (Exception e) {
            logger.error("Error getting all bookings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/bookings/{ipfsHash}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable String ipfsHash) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ipfsHash is required"
                ));
            }
            String txHash = adminService.completeBooking(ipfsHash);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "message", "Booking completed successfully"
            ));
        } catch (Exception e) {
            logger.error("Error completing booking: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("bookings/{ipfsHash}/reject")
    public ResponseEntity<?> rejectBooking(
        @PathVariable String ipfsHash,
        @RequestBody Map<String, String> request
    ) {
        try {
            String reason = request.get("reason");
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ipfsHash is required"
                ));
            }
            if (reason == null || reason.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Rejection reason is required"
                ));
            }
            String txHash = adminService.rejectBooking(ipfsHash, reason);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "message", "Booking rejected successfully"
            ));
        } catch (Exception e) {
            logger.error("Error rejecting booking: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/bookings/{ipfsHash}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable String ipfsHash) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ipfsHash is required"
                ));
            }
            String txHash = adminService.cancelBooking(ipfsHash);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "message", "Booking cancelled successfully"
            ));
        } catch (Exception e) {
            logger.error("Error cancelling booking: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}