package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.SportFacilityRequest;
import com.usfbs.springboot.dto.SportFacilityResponse;
import com.usfbs.springboot.dto.SportFacilityDetailResponse;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;
import com.usfbs.springboot.service.AdminService;
import com.usfbs.springboot.util.PinataUtil;
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
import java.util.HashMap;
import org.web3j.tuples.generated.Tuple7;

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

    @Autowired
    private PinataUtil pinataUtil;

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
        consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE },
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateSportFacility(
        @RequestParam(value = "oldName", required = false) String oldName,
        @RequestParam(value = "newName", required = false) String newName,
        @RequestParam(value = "newLocation", required = false) String newLocation,
        @RequestParam(value = "newStatus", required = false) Integer newStatusInt,
        @RequestParam(value = "newImageFile", required = false) MultipartFile newImageFile,
        @RequestParam(value = "newImageIPFS", required = false) String newImageIPFS,
        @RequestBody(required = false) Map<String, Object> requestBody
    ) {
        try {
            // Support both JSON and multipart
            String _oldName = oldName;
            String _newName = newName;
            String _newLocation = newLocation;
            Integer _newStatusInt = newStatusInt;
            MultipartFile _newImageFile = newImageFile;
            String _newImageIPFS = newImageIPFS;

            if (_oldName == null && requestBody != null) {
                _oldName = (String) requestBody.get("oldName");
                _newName = (String) requestBody.getOrDefault("newName", "");
                _newLocation = (String) requestBody.getOrDefault("newLocation", "");
                _newStatusInt = requestBody.containsKey("newStatus") && requestBody.get("newStatus") != null
                    ? (Integer) requestBody.get("newStatus")
                    : null;
                _newImageIPFS = (String) requestBody.get("newImageIPFS");
            }

            if (_oldName == null || _oldName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Old facility name is required"));
            }

            String result = adminService.updateSportFacility(
                _oldName,
                _newName,
                _newLocation,
                _newImageIPFS,
                _newStatusInt != null ? java.math.BigInteger.valueOf(_newStatusInt) : null
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

            if (newCourtName != null && newCourtName.equals(oldCourtName)) {
                newCourtName = "";
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

            String ipfsHash = pinataUtil.uploadJsonToIPFS(bookingDetails, fileName);

            // Create booking on-chain (initial IPFS hash)
            String txHash = adminService.createBooking(
                ipfsHash,
                facilityName,
                courtName,
                BigInteger.valueOf(startTime),
                BigInteger.valueOf(endTime),
                status
            );

            // After bookingCreated event, the AdminService will update the booking receipt on IPFS,
            // unpin the old IPFS hash (ipfsHash), and upload the new one.
            // Retrieve the new IPFS hash from AdminService 
            String newIpfsHash = adminService.getLatestBookingIpfsHash(ipfsHash);

            // update it on-chain
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                adminService.updateBookingIPFSHash(ipfsHash, newIpfsHash);
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

    @GetMapping("/bookings/{ipfsHash}")
    public ResponseEntity<?> getBookingByIpfsHash(@PathVariable String ipfsHash) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "ipfsHash is required"));
            }
            // Call the contract function which returns a tuple, not a struct
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                adminService.getBookingTupleByIpfsHash(ipfsHash);

            if (bookingTuple == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Booking not found"
                ));
            }

            Map<String, Object> booking = Map.of(
                "owner", bookingTuple.component1(),
                "ipfsHash", bookingTuple.component2(),
                "facilityName", bookingTuple.component3(),
                "courtName", bookingTuple.component4(),
                "startTime", bookingTuple.component5(),
                "endTime", bookingTuple.component6(),
                "status", bookingTuple.component7().toString()
            );

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

    @GetMapping("/bookings/all")
    public ResponseEntity<?> getAllBookings_() {
        try {
            List<Map<String, Object>> bookings = adminService.getAllBookings_();
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

    /**
     * GET all bookings for the current user (calls Booking.getAllBookings)
     */
    @GetMapping("/bookings")
    public ResponseEntity<?> getAllBookings() {
        try {
            // This should call the Booking contract's getAllBookings() (not getAllBookings_())
            List<Object> rawBookings = adminService.getAllBookings();
            List<Map<String, Object>> bookings = new ArrayList<>();

            for (Object obj : rawBookings) {
                Map<String, Object> bookingMap = new HashMap<>();
                try {
                    Class<?> bookingClass = obj.getClass();
                    java.lang.reflect.Field ownerField = bookingClass.getDeclaredField("owner");
                    java.lang.reflect.Field ipfsHashField = bookingClass.getDeclaredField("ipfsHash");
                    java.lang.reflect.Field fnameField = bookingClass.getDeclaredField("fname");
                    java.lang.reflect.Field cnameField = bookingClass.getDeclaredField("cname");
                    java.lang.reflect.Field timeField = bookingClass.getDeclaredField("time");
                    java.lang.reflect.Field statusField = bookingClass.getDeclaredField("status");

                    ownerField.setAccessible(true);
                    ipfsHashField.setAccessible(true);
                    fnameField.setAccessible(true);
                    cnameField.setAccessible(true);
                    timeField.setAccessible(true);
                    statusField.setAccessible(true);

                    Object timeObj = timeField.get(obj);
                    Class<?> timeClass = timeObj.getClass();
                    java.lang.reflect.Field startTimeField = timeClass.getDeclaredField("startTime");
                    java.lang.reflect.Field endTimeField = timeClass.getDeclaredField("endTime");
                    startTimeField.setAccessible(true);
                    endTimeField.setAccessible(true);

                    bookingMap.put("owner", ownerField.get(obj));
                    bookingMap.put("ipfsHash", ipfsHashField.get(obj));
                    bookingMap.put("facilityName", fnameField.get(obj));
                    bookingMap.put("courtName", cnameField.get(obj));
                    bookingMap.put("startTime", startTimeField.get(timeObj));
                    bookingMap.put("endTime", endTimeField.get(timeObj));
                    bookingMap.put("status", statusField.get(obj).toString());
                } catch (Exception e) {
                    logger.warn("Could not extract booking fields: {}", e.getMessage());
                }
                bookings.add(bookingMap);
            }
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", bookings
            ));
        } catch (Exception e) {
            logger.error("Error getting user bookings: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    // GET all booked timeslots for a court 
    @GetMapping(
        value = "/{sportFacility}/{court}/booked-timeslots",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> getBookedTimeSlotsAdmin(
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
            List<Map<String, Object>> slots = adminService.getBookedTimeSlots(facilityName, courtName);
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

    @PostMapping("/bookings/{ipfsHash}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable String ipfsHash) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "ipfsHash is required"
                ));
            }
            // Use tuple-based fetch
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                adminService.getBookingTupleByIpfsHash(ipfsHash);

            if (bookingTuple == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Booking not found"
                ));
            }

            // Build booking map with userAddress included and status "completed"
            Map<String, Object> booking = Map.of(
                "facilityName", bookingTuple.component3(),
                "courtName", bookingTuple.component4(),
                "startTime", bookingTuple.component5(),
                "endTime", bookingTuple.component6(),
                "status", "completed",
                "userAddress", bookingTuple.component1()
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            Long startTime = bookingTuple.component5() != null ? bookingTuple.component5().longValue() : null;
            java.time.LocalDate bookingDate = startTime != null
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload new JSON to IPFS
            String newIpfsHash = pinataUtil.uploadJsonToIPFS(booking, fileName);

            // Complete booking on-chain (get txHash and event)
            String txHash = adminService.completeBooking(ipfsHash, newIpfsHash);

            // Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(ipfsHash);

            // Update the new IPFS hash on-chain (optional, if not already done in completeBooking)
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                adminService.updateBookingIPFSHash(ipfsHash, newIpfsHash);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "ipfsHash", newIpfsHash != null ? newIpfsHash : ipfsHash,
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

            // Use tuple-based fetch
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                adminService.getBookingTupleByIpfsHash(ipfsHash);

            if (bookingTuple == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Booking not found"
                ));
            }

            // Build booking map with userAddress and status "rejected"
            Map<String, Object> booking = Map.of(
                "facilityName", bookingTuple.component3(),
                "courtName", bookingTuple.component4(),
                "startTime", bookingTuple.component5(),
                "endTime", bookingTuple.component6(),
                "status", "rejected",
                "userAddress", bookingTuple.component1(),
                "rejectionReason", reason
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            Long startTime = bookingTuple.component5() != null ? bookingTuple.component5().longValue() : null;
            java.time.LocalDate bookingDate = startTime != null
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload new JSON to IPFS
            String newIpfsHash = pinataUtil.uploadJsonToIPFS(booking, fileName);

            // Reject booking on-chain (get txHash and event)
            String txHash = adminService.rejectBooking(ipfsHash, reason);

            // Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(ipfsHash);

            // Update the new IPFS hash on-chain
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                adminService.updateBookingIPFSHash(ipfsHash, newIpfsHash);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "ipfsHash", newIpfsHash != null ? newIpfsHash : ipfsHash,
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
            // Use tuple-based fetch
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                adminService.getBookingTupleByIpfsHash(ipfsHash);

            if (bookingTuple == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", "Booking not found"
                ));
            }

            // Build booking map
            Map<String, Object> booking = Map.of(
                "facilityName", bookingTuple.component3(),
                "courtName", bookingTuple.component4(),
                "startTime", bookingTuple.component5(),
                "endTime", bookingTuple.component6(),
                "status", "cancelled",
                "userAddress", bookingTuple.component1()
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            Long startTime = bookingTuple.component5() != null ? bookingTuple.component5().longValue() : null;
            java.time.LocalDate bookingDate = startTime != null
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload new JSON to IPFS
            String newIpfsHash = pinataUtil.uploadJsonToIPFS(booking, fileName);

            // Cancel booking on-chain (get txHash and event)
            String txHash = adminService.cancelBooking(ipfsHash);

            // Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(ipfsHash);

            // Update the new IPFS hash on-chain
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                adminService.updateBookingIPFSHash(ipfsHash, newIpfsHash);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "txHash", txHash,
                "ipfsHash", newIpfsHash != null ? newIpfsHash : ipfsHash,
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

    @PostMapping(
        value = "/sport-facilities/upload-image",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> uploadSportFacilityImage(@RequestParam("image") MultipartFile imageFile) {
        try {
            if (imageFile == null || imageFile.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Image file is required"));
            }
            String imageIPFS = adminService.uploadFacilityImageToIPFS(imageFile);
            return ResponseEntity.ok(Map.of("success", true, "imageIPFS", imageIPFS));
        } catch (Exception e) {
            logger.error("Error uploading facility image: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}