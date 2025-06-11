package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

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
        @RequestParam("endDate") long newEndDate,
        @RequestParam(value = "oldTitle", required = false) String oldTitle
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

            String txHash;
            
            // Check if file is provided for full update
            if (newFile != null && !newFile.isEmpty()) {
                // Full update with new file
                txHash = adminService.updateAnnouncement(
                    oldIpfsHash, newFile, newTitle, newStartDate, newEndDate
                );
            } else {
                // Determine what needs to be updated based on changes
                txHash = adminService.updateAnnouncementSelectively(
                    oldIpfsHash, oldTitle, newTitle, newStartDate, newEndDate
                );
            }
            
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

    @PutMapping(
        value = "/update-announcement-time",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateAnnouncementTime(
        @RequestParam("ipfsHash") String ipfsHash,
        @RequestParam("startDate") long newStartDate,
        @RequestParam("endDate") long newEndDate
    ) {
        try {
            // Validate input parameters
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "IPFS hash cannot be empty")
                );
            }
            
            if (newStartDate >= newEndDate) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "End date must be after start date")
                );
            }

            String txHash = adminService.updateAnnouncementTimeOnly(
                ipfsHash, newStartDate, newEndDate
            );
            return ResponseEntity.ok().body(
                java.util.Map.of(
                    "message", "Announcement time updated successfully",
                    "txHash", txHash,
                    "ipfsHash", ipfsHash,
                    "newStartDate", newStartDate,
                    "newEndDate", newEndDate
                )
            );
        } catch (Exception e) {
            System.err.println("updateAnnouncementTime error: " + e.getMessage());
            return ResponseEntity.status(500).body(
                java.util.Map.of("error", "Time update failed", "details", e.getMessage())
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

    @PutMapping(
        value = "/update-announcement-title",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> updateAnnouncementTitle(
        @RequestParam("ipfsHash") String ipfsHash,
        @RequestParam("oldTitle") String oldTitle,
        @RequestParam("newTitle") String newTitle
    ) {
        try {
            // Validate input parameters
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "IPFS hash cannot be empty")
                );
            }
            
            if (oldTitle == null || oldTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "Old title cannot be empty")
                );
            }
            
            if (newTitle == null || newTitle.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "New title cannot be empty")
                );
            }

            if (oldTitle.equals(newTitle)) {
                return ResponseEntity.badRequest().body(
                    java.util.Map.of("error", "New title must be different from old title")
                );
            }

            String txHash = adminService.updateAnnouncementTitleOnly(
                ipfsHash, oldTitle, newTitle
            );
            
            return ResponseEntity.ok().body(
                java.util.Map.of(
                    "message", "Announcement title updated successfully",
                    "txHash", txHash,
                    "ipfsHash", ipfsHash,
                    "oldTitle", oldTitle,
                    "newTitle", newTitle
                )
            );
        } catch (Exception e) {
            System.err.println("updateAnnouncementTitle error: " + e.getMessage());
            return ResponseEntity.status(500).body(
                java.util.Map.of("error", "Title update failed", "details", e.getMessage())
            );
        }
    }
}

