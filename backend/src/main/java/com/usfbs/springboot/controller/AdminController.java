package com.usfbs.springboot.controller;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.PinataManifest;
import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
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
        var contractAddress = managementContract.getContractAddress();
        var startBlock = org.web3j.protocol.core.DefaultBlockParameterName.EARLIEST;
        var endBlock = org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

        List<Management.AnnouncementAddedEventResponse> addedEvents =
            managementContract.announcementAddedEventFlowable(
                new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
            ).toList().blockingGet();

        List<Management.AnnouncementIpfsHashModifiedEventResponse> ipfsModifiedEvents =
            managementContract.announcementIpfsHashModifiedEventFlowable(
                new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
            ).toList().blockingGet();

        List<Management.AnnouncementTimeModifiedEventResponse> timeModifiedEvents =
            managementContract.announcementTimeModifiedEventFlowable(
                new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
            ).toList().blockingGet();

        List<Management.AnnouncementDeletedEventResponse> deletedEvents =
            managementContract.announcementDeletedEventFlowable(
                new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress)
            ).toList().blockingGet();

        // 2. Reconstruct the current state of announcements
        Map<String, AnnouncementState> announcementMap = new java.util.HashMap<>();
        for (var event : addedEvents) {
            announcementMap.put(event.ipfsHash, new AnnouncementState(
                event.ipfsHash,
                event.startTime.longValue(),
                event.endTime.longValue()
            ));
        }
        for (var event : ipfsModifiedEvents) {
            AnnouncementState state = announcementMap.remove(event.ipfsHash_);
            if (state != null) {
                state.ipfsHash = event.ipfsHash;
                announcementMap.put(event.ipfsHash, state);
            }
        }
        for (var event : timeModifiedEvents) {
            AnnouncementState state = announcementMap.get(event.ipfsHash);
            if (state != null) {
                state.startDate = event.startTime.longValue();
                state.endDate = event.endTime.longValue();
            }
        }
        for (var event : deletedEvents) {
            announcementMap.remove(event.ipfsHash);
        }

        // 3. Filter out expired announcements (do not delete, just hide)
        long now = System.currentTimeMillis() / 1000L;
        List<AnnouncementItem> result = announcementMap.values().stream()
            .filter(state -> state.endDate >= now)
            .map(state -> {
                PinataManifest manifest = null;
                try {
                    manifest = rest.getForObject(
                        "https://gateway.pinata.cloud/ipfs/" + state.ipfsHash,
                        PinataManifest.class
                    );
                } catch (Exception e) {
                    System.err.println("Pinata fetch error for " + state.ipfsHash + ": " + e.getMessage());
                }
                return new AnnouncementItem(
                    state.ipfsHash,
                    manifest != null ? manifest.getTitle() : "",
                    manifest != null ? manifest.getFileCid() : "",
                    state.startDate,
                    state.endDate
                );
            })
            .collect(Collectors.toList());

        return result;
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

    // Helper class for announcement state
    private static class AnnouncementState {
        String ipfsHash;
        long startDate;
        long endDate;

        AnnouncementState(String ipfsHash, long startDate, long endDate) {
            this.ipfsHash = ipfsHash;
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}

