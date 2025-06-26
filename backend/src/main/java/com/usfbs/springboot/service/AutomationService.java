package com.usfbs.springboot.service;

import com.usfbs.springboot.dto.AnnouncementItem;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AutomationService {

    private final RestTemplate restTemplate;
    private final String backendUrl;

    @Value("${automation.admin-email}")
    private String automationAdminEmail;

    @Value("${automation.admin-address}")
    private String automationAdminAddress;

    // Store JWT and expiry in memory
    private String automationJwt = null;
    private long jwtExpiryEpoch = 0;

    @Autowired
    private AdminService adminService;

    @Autowired
    public AutomationService(
        RestTemplate restTemplate,
        @Value("${app.backend-url:http://localhost:8080}") String backendUrl
    ) {
        this.restTemplate = restTemplate;
        this.backendUrl = backendUrl;
    }

    // Helper: Login and get JWT
    private synchronized void refreshJwtIfNeeded() {
        long now = Instant.now().getEpochSecond();
        if (automationJwt == null || now >= jwtExpiryEpoch - 60) {
            try {
                String url = backendUrl + "/api/auth/login";
                Map<String, String> loginPayload = new HashMap<>();
                loginPayload.put("email", automationAdminEmail); // must be a valid MMU admin email
                loginPayload.put("userAddress", automationAdminAddress); // must be the admin's blockchain address

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(loginPayload, headers);

                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> body = response.getBody();
                    automationJwt = (String) body.get("accessToken");
                    Object expObj = body.get("expiresIn");
                    if (expObj != null) {
                        jwtExpiryEpoch = now + Integer.parseInt(expObj.toString());
                    } else {
                        jwtExpiryEpoch = now + 1500;
                    }
                } else {
                    throw new RuntimeException("Failed to login for automation JWT: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("AutomationService JWT login error: " + e.getMessage());
                throw new RuntimeException("AutomationService JWT login error", e);
            }
        }
    }

    // Runs every day at 00:05 AM Malaysia time (GMT+8) for expired announcements
    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Kuala_Lumpur")
    public void deleteExpiredAnnouncements() {
        try {
            List<AnnouncementItem> announcements = adminService.getAllAnnouncements();
            long now = Instant.now().getEpochSecond();
            for (AnnouncementItem item : announcements) {
                if (item.getEndDate() <= now) {
                    adminService.deleteAnnouncement(item.getIpfsHash());
                }
            }
        } catch (Exception e) {
            System.err.println("deleteExpiredAnnouncements error: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Kuala_Lumpur")
    public void autoCompleteBookings() {
        try {
            refreshJwtIfNeeded();
            String url = backendUrl + "/api/admin/bookings/all";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + automationJwt);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                System.err.println("Failed to fetch bookings: " + response.getStatusCode());
                return;
            }

            List<Map<String, Object>> bookings = (List<Map<String, Object>>) response.getBody().get("data");
            long now = Instant.now().getEpochSecond();

            for (Map<String, Object> booking : bookings) {
                Object statusObj = booking.get("status");
                int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : Integer.parseInt(statusObj.toString());
                long endTime = Long.parseLong(booking.get("endTime").toString());

                if (status == 1 && endTime <= now) {
                    String ipfsHash = booking.get("ipfsHash").toString();
                    try {
                        String completeUrl = backendUrl + "/api/admin/bookings/" + ipfsHash + "/complete";
                        HttpHeaders completeHeaders = new HttpHeaders();
                        completeHeaders.set("Authorization", "Bearer " + automationJwt);
                        HttpEntity<Void> completeEntity = new HttpEntity<>(completeHeaders);
                        ResponseEntity<Map> completeResp = restTemplate.exchange(
                            completeUrl,
                            HttpMethod.POST,
                            completeEntity,
                            Map.class
                        );
                        if (completeResp.getStatusCode() == HttpStatus.OK) {
                            System.out.println("Auto-completed booking: " + ipfsHash);
                        } else {
                            System.err.println("Failed to complete booking: " + ipfsHash);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error completing booking " + ipfsHash + ": " + ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("autoCompleteBookings error: " + e.getMessage());
        }
    }
    // TODO: Add notification or logging enhancements 
}