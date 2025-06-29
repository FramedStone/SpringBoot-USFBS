package com.usfbs.springboot.service;

import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.service.EventLogService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.time.Instant;
import java.util.List;
import java.util.Formatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
    private EventLogService eventLogService; 

    @Autowired
    public AutomationService(
        RestTemplate restTemplate,
        @Value("${app.backend-url:http://localhost:8080}") String backendUrl,
        EventLogService eventLogService 
    ) {
        this.restTemplate = restTemplate;
        this.backendUrl = backendUrl;
        this.eventLogService = eventLogService; 
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

            // Define formatter for Malaysia time
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy ha").withZone(ZoneId.of("Asia/Kuala_Lumpur"));

            // Only process and display bookings with status == 0 (APPROVED)
            List<Map<String, Object>> approvedBookings = new ArrayList<>();
            for (Map<String, Object> booking : bookings) {
                Object statusObj = booking.get("status");
                int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : Integer.parseInt(statusObj.toString());
                if (status == 0) {
                    Map<String, Object> bookingCopy = new HashMap<>(booking);
                    bookingCopy.put("status", "APPROVED");

                    // Convert startTime and endTime to "dd/MM/yyyy ha" format in Malaysia time
                    if (bookingCopy.containsKey("startTime")) {
                        try {
                            long startEpoch = Long.parseLong(bookingCopy.get("startTime").toString());
                            ZonedDateTime startZdt = Instant.ofEpochSecond(startEpoch).atZone(ZoneId.of("Asia/Kuala_Lumpur"));
                            bookingCopy.put("startTime", startZdt.format(formatter).toLowerCase());
                        } catch (Exception ex) {
                            // TODO: handle invalid startTime format
                        }
                    }
                    if (bookingCopy.containsKey("endTime")) {
                        try {
                            long endEpoch = Long.parseLong(bookingCopy.get("endTime").toString());
                            ZonedDateTime endZdt = Instant.ofEpochSecond(endEpoch).atZone(ZoneId.of("Asia/Kuala_Lumpur"));
                            bookingCopy.put("endTime", endZdt.format(formatter).toLowerCase());
                        } catch (Exception ex) {
                            // TODO: handle invalid endTime format
                        }
                    }

                    approvedBookings.add(bookingCopy);
                }
            }

            System.out.println(">>> AutomationService: Fetched bookings: " + approvedBookings);

            long now = Instant.now().getEpochSecond();

            for (Map<String, Object> booking : approvedBookings) {
                Object statusObj = booking.get("status");
                int status = statusObj instanceof Number ? ((Number) statusObj).intValue() : Integer.parseInt(statusObj.toString());
                long endTimeEpoch = Long.parseLong(booking.get("endTime").toString());

                if (status == 0 && endTimeEpoch <= now) {
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

                            // Log to event log for SystemLogs page
                            String facilityName = booking.get("facilityName") != null ? booking.get("facilityName").toString() : "-";
                            String courtName = booking.get("courtName") != null ? booking.get("courtName").toString() : "-";
                            long startEpoch = booking.get("startTime") != null ? Long.parseLong(booking.get("startTime").toString()) : 0;
                            long endEpoch = booking.get("endTime") != null ? Long.parseLong(booking.get("endTime").toString()) : 0;
                            ZonedDateTime startZdt = Instant.ofEpochSecond(startEpoch).atZone(ZoneId.of("Asia/Kuala_Lumpur"));
                            ZonedDateTime endZdt = Instant.ofEpochSecond(endEpoch).atZone(ZoneId.of("Asia/Kuala_Lumpur"));
                            String startStr = startZdt.format(formatter).toLowerCase();
                            String endStr = endZdt.format(formatter).toLowerCase();

                            StringBuilder note = new StringBuilder();
                            note.append(ipfsHash).append("\n")
                                .append(facilityName).append("\n")
                                .append(courtName).append("\n")
                                .append(startStr).append(" to ").append(endStr);

                            // Use EventLogService to add the log
                            eventLogService.addEventLog(
                                ipfsHash,
                                "Booking Completed",
                                "System",
                                String.valueOf(Instant.now().toEpochMilli()),
                                note.toString(),
                                "BOOKING"
                            );
                        } else {
                            System.err.println("Failed to complete booking: " + ipfsHash);
                        }
                    } catch (Exception ex) {
                        System.err.println("Error completing booking " + ipfsHash + ": " + ex.getMessage());
                    }
                } else if (status == 0 && endTimeEpoch > now) {
                    long secondsLeft = endTimeEpoch - now;
                    long days = secondsLeft / (24 * 3600);
                    long hours = (secondsLeft % (24 * 3600)) / 3600;
                    long minutes = (secondsLeft % 3600) / 60;
                    StringBuilder timeLeft = new StringBuilder();
                    if (days > 0) {
                        timeLeft.append(days).append(days == 1 ? " day" : " days");
                    }
                    if (hours > 0) {
                        if (timeLeft.length() > 0) timeLeft.append(" ");
                        timeLeft.append(hours).append(hours == 1 ? " hour" : " hours");
                    }
                    if (minutes > 0) {
                        if (timeLeft.length() > 0) timeLeft.append(" ");
                        timeLeft.append(minutes).append(minutes == 1 ? " minute" : " minutes");
                    }
                    if (timeLeft.length() == 0) {
                        timeLeft.append("less than 1 minute");
                    }
                    System.out.println(">>> AutomationService: Booking " + booking.get("ipfsHash") + " cannot be auto-completed yet. Time left: " + timeLeft);
                }
            }
        } catch (Exception e) {
            System.err.println("autoCompleteBookings error: " + e.getMessage());
        }
    }
}