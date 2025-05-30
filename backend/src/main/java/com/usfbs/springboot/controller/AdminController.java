package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.PinataManifest;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<AnnouncementItem> getAnnouncementEvents() throws Exception {
        // Get all past AnnouncementAdded events
        List<Management.AnnouncementAddedEventResponse> events =
            managementContract.announcementAddedEventFlowable(
                new org.web3j.protocol.core.DefaultBlockParameterNumber(0),
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST
            ).toList().blockingGet();

        return events.stream().map(ev -> {
            String hash  = ev.ipfsHash;
            long   start = ev.startTime.longValue();
            long   end   = ev.endTime.longValue();

            // Fetch manifest from IPFS via Pinata gateway
            PinataManifest manifest = rest.getForObject(
                "https://gateway.pinata.cloud/ipfs/" + hash,
                PinataManifest.class
            );

            return new AnnouncementItem(
                hash,
                manifest != null ? manifest.getTitle() : "",
                manifest != null ? manifest.getFileCid() : "",
                start,
                end
            );
        }).collect(Collectors.toList());
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
            String txHash = adminService.uploadAnnouncement(file, title, startDate, endDate);
            return ResponseEntity.ok().body(
                java.util.Map.of("message", "Announcement uploaded", "txHash", txHash)
            );
        } catch (Exception e) {
            // Log error with context
            System.err.println("uploadAnnouncement error: " + e.getMessage());
            return ResponseEntity.status(500).body(
                java.util.Map.of("error", "Upload failed", "details", e.getMessage())
            );
        }
    }
}

