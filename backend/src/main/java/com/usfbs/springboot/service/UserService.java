package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;
import com.usfbs.springboot.contracts.Booking;
import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.SportFacilityResponse;
import com.usfbs.springboot.dto.SportFacilityDetailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import java.math.BigInteger;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private Management managementContract;
    
    @Autowired
    private SportFacility sportFacilityContract;
    
    @Autowired
    private Booking bookingContract;
    
    @Value("${pinata.gateway.url:https://gateway.pinata.cloud}")
    private String pinataGatewayUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gets all announcements from blockchain for users
     */
    public List<AnnouncementItem> getAnnouncements() throws Exception {
        try {
            logger.info("Fetching announcements for user from blockchain");
            
            List<Object> rawAnnouncements = managementContract.getAnnouncements().send();
            List<AnnouncementItem> announcements = new ArrayList<>();

            for (Object obj : rawAnnouncements) {
                try {
                    AnnouncementItem item = null;
                    
                    if (obj instanceof Management.Announcement) {
                        Management.Announcement announcement = (Management.Announcement) obj;
                        item = _processAnnouncementDirect(announcement);
                    } else {
                        item = _extractAnnouncementFromObject(obj);
                    }
                    
                    if (item != null) {
                        _resolveAnnouncementMedia(item);
                        announcements.add(item);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process announcement object: {}", e.getMessage());
                    continue;
                }
            }

            logger.info("Successfully retrieved {} announcements for user", announcements.size());
            return announcements;

        } catch (Exception e) {
            if (e.getMessage().contains("No Announcement found in blockchain")) {
                logger.info("No announcements found in blockchain for user");
                return new ArrayList<>();
            }
            
            logger.error("Error getting announcements for user: {}", e.getMessage());
            throw new Exception("Failed to get announcements: " + e.getMessage());
        }
    }

    /**
     * Process announcement object directly when proper casting is possible
     */
    private AnnouncementItem _processAnnouncementDirect(Management.Announcement announcement) {
        try {
            if (announcement.ipfsHash == null || announcement.ipfsHash.trim().isEmpty()) {
                return null;
            }

            AnnouncementItem item = new AnnouncementItem();
            item.setIpfsHash(announcement.ipfsHash);
            item.setStartDate(announcement.startTime.longValue());
            item.setEndDate(announcement.endTime.longValue());
            
            return item;
        } catch (Exception e) {
            logger.warn("Failed to process announcement directly: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract announcement data from object using reflection for classloader compatibility
     */
    private AnnouncementItem _extractAnnouncementFromObject(Object obj) {
        try {
            Class<?> announcementClass = obj.getClass();
            
            java.lang.reflect.Field ipfsHashField = announcementClass.getDeclaredField("ipfsHash");
            java.lang.reflect.Field startTimeField = announcementClass.getDeclaredField("startTime");
            java.lang.reflect.Field endTimeField = announcementClass.getDeclaredField("endTime");
            
            ipfsHashField.setAccessible(true);
            startTimeField.setAccessible(true);
            endTimeField.setAccessible(true);
            
            String ipfsHash = (String) ipfsHashField.get(obj);
            java.math.BigInteger startTime = (java.math.BigInteger) startTimeField.get(obj);
            java.math.BigInteger endTime = (java.math.BigInteger) endTimeField.get(obj);
            
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return null;
            }

            AnnouncementItem item = new AnnouncementItem();
            item.setIpfsHash(ipfsHash);
            item.setStartDate(startTime.longValue());
            item.setEndDate(endTime.longValue());
            
            return item;
            
        } catch (Exception e) {
            logger.warn("Failed to extract announcement data using reflection: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Resolve announcement media information by fetching IPFS manifest
     */
    private void _resolveAnnouncementMedia(AnnouncementItem item) {
        try {
            if (item.getIpfsHash() == null || item.getIpfsHash().trim().isEmpty()) {
                return;
            }
            
            String manifestUrl = pinataGatewayUrl + "/ipfs/" + item.getIpfsHash();
            logger.debug("Fetching manifest from: {}", manifestUrl);
            
            String manifestJson = restTemplate.getForObject(manifestUrl, String.class);
            
            if (manifestJson != null) {
                JsonNode manifestNode = objectMapper.readTree(manifestJson);
                
                if (manifestNode.has("title")) {
                    String title = manifestNode.get("title").asText();
                    item.setTitle(title);
                }
                
                if (manifestNode.has("fileCid")) {
                    String fileCid = manifestNode.get("fileCid").asText();
                    item.setFileCid(fileCid);
                    
                    logger.debug("Resolved media file CID: {}", fileCid);
                }
            } else {
                logger.warn("Empty manifest response for IPFS hash: {}", item.getIpfsHash());
            }
            
        } catch (Exception e) {
            logger.warn("Failed to resolve IPFS manifest for hash {}: {}", item.getIpfsHash(), e.getMessage());
        }
    }

    /**
     * Gets all sport facilities from blockchain for users
     */
    public List<SportFacilityResponse> getSportFacilities() throws Exception {
        try {
            logger.info("Fetching sport facilities for user from blockchain");
            
            org.web3j.tuples.generated.Tuple4<List<String>, List<String>, List<String>, List<java.math.BigInteger>> result =
                sportFacilityContract.getAllSportFacility().send();

            List<String> names = result.component1();
            List<String> locations = result.component2();
            List<String> imageIPFSList = result.component3();
            List<java.math.BigInteger> statuses = result.component4();

            List<SportFacilityResponse> facilities = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                SportFacilityResponse facility = new SportFacilityResponse();
                facility.setName(names.get(i));
                facility.setLocation(locations.get(i));
                facility.setImageIPFS(imageIPFSList.get(i));
                facility.setStatus(_convertStatusToString(statuses.get(i)));
                facilities.add(facility);
            }

            logger.info("Successfully retrieved {} sport facilities for user", facilities.size());
            return facilities;

        } catch (Exception e) {
            if (e.getMessage().contains("No Sport Facility found in blockchain")) {
                logger.info("No sport facilities found in blockchain for user");
                return new ArrayList<>();
            }
            
            logger.error("Error getting sport facilities for user: {}", e.getMessage());
            throw new Exception("Failed to get sport facilities: " + e.getMessage());
        }
    }

    /**
     * Convert status enum from blockchain to string
     */
    private String _convertStatusToString(java.math.BigInteger status) {
        switch (status.intValue()) {
            case 0: return "OPEN";
            case 1: return "CLOSED";
            case 2: return "MAINTENANCE";
            case 3: return "BOOKED";
            default: return "UNKNOWN";
        }
    }

    /**
     * Gets all courts from a specific sport facility for users
     */
    public SportFacilityDetailResponse getSportFacilityWithCourts(String facilityName) throws Exception {
        try {
            logger.info("Fetching sport facility '{}' with courts for user from blockchain", facilityName);
            
            if (facilityName == null || facilityName.trim().isEmpty()) {
                throw new IllegalArgumentException("Facility name cannot be empty");
            }
            
            org.web3j.tuples.generated.Tuple5<String, String, String, java.math.BigInteger, List<SportFacility.court>> result =
                sportFacilityContract.getSportFacility(facilityName).send();

            SportFacilityDetailResponse response = new SportFacilityDetailResponse();
            response.setName(result.component1());
            response.setLocation(result.component2());
            response.setImageIPFS(result.component3());
            response.setStatus(_convertStatusToString(result.component4()));
            response.setCourts(result.component5());

            logger.info("Successfully retrieved sport facility '{}' with {} courts for user", facilityName, result.component5().size());
            return response;

        } catch (Exception e) {
            if (e.getMessage().contains("Sport Facility not found")) {
                logger.warn("Sport facility '{}' not found", facilityName);
                throw new Exception("Sport facility '" + facilityName + "' not found");
            }
            
            logger.error("Error getting sport facility '{}' with courts for user: {}", facilityName, e.getMessage());
            throw new Exception("Failed to get sport facility details: " + e.getMessage());
        }
    }

    /**
     * Convert raw court object to SportFacility.court
     */
    private SportFacility.court _convertToCourtObject(Object obj) {
        try {
            // Try direct casting first
            if (obj instanceof SportFacility.court) {
                return (SportFacility.court) obj;
            }
            
            // Use reflection as fallback
            Class<?> courtClass = obj.getClass();
            
            java.lang.reflect.Field nameField = courtClass.getDeclaredField("name");
            java.lang.reflect.Field earliestTimeField = courtClass.getDeclaredField("earliestTime");
            java.lang.reflect.Field latestTimeField = courtClass.getDeclaredField("latestTime");
            java.lang.reflect.Field statusField = courtClass.getDeclaredField("status");
            
            nameField.setAccessible(true);
            earliestTimeField.setAccessible(true);
            latestTimeField.setAccessible(true);
            statusField.setAccessible(true);
            
            String name = (String) nameField.get(obj);
            java.math.BigInteger earliestTime = (java.math.BigInteger) earliestTimeField.get(obj);
            java.math.BigInteger latestTime = (java.math.BigInteger) latestTimeField.get(obj);
            java.math.BigInteger status = (java.math.BigInteger) statusField.get(obj);
            
            if (name == null || name.trim().isEmpty()) {
                return null;
            }

            return new SportFacility.court(name, earliestTime, latestTime, status);
            
        } catch (Exception e) {
            logger.warn("Failed to convert court object: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Create a new booking
     */
    public String createBooking(String ipfsHash, String facilityName, String courtName, BigInteger startTime, BigInteger endTime) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) throw new IllegalArgumentException("ipfsHash is required");
            if (facilityName == null || facilityName.trim().isEmpty()) throw new IllegalArgumentException("facilityName is required");
            if (courtName == null || courtName.trim().isEmpty()) throw new IllegalArgumentException("courtName is required");
            if (startTime == null || endTime == null || endTime.compareTo(startTime) <= 0) throw new IllegalArgumentException("Invalid time range");

            Booking.timeSlot timeSlot = new Booking.timeSlot(startTime, endTime);

            TransactionReceipt receipt = bookingContract.createBooking(ipfsHash, facilityName, courtName, timeSlot).send();

            if (receipt.isStatusOK()) {
                logger.info("User booking created successfully: {}", ipfsHash);
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking creation failed on-chain");
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage());
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }
    }

    /**
     * Gets a single booking by ipfsHash for the current user
     */
    public Map<String, Object> getBookingByIpfsHash(String userAddress, String ipfsHash) {
        try {
            Object bookingObj = bookingContract.getBooking(ipfsHash).send();
            Map<String, Object> bookingMap = new HashMap<>();
            Class<?> bookingClass = bookingObj.getClass();

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

            Object timeObj = timeField.get(bookingObj);
            Class<?> timeClass = timeObj.getClass();
            java.lang.reflect.Field startTimeField = timeClass.getDeclaredField("startTime");
            java.lang.reflect.Field endTimeField = timeClass.getDeclaredField("endTime");
            startTimeField.setAccessible(true);
            endTimeField.setAccessible(true);

            // Only allow access if the booking belongs to the user
            String owner = ownerField.get(bookingObj).toString();
            if (!owner.equalsIgnoreCase(userAddress)) {
                throw new RuntimeException("Access denied: not booking owner");
            }

            bookingMap.put("owner", owner);
            bookingMap.put("ipfsHash", ipfsHashField.get(bookingObj));
            bookingMap.put("facilityName", fnameField.get(bookingObj));
            bookingMap.put("courtName", cnameField.get(bookingObj));
            bookingMap.put("startTime", startTimeField.get(timeObj));
            bookingMap.put("endTime", endTimeField.get(timeObj));
            bookingMap.put("status", statusField.get(bookingObj).toString());

            return bookingMap;
        } catch (Exception e) {
            logger.error("Error getting booking by ipfsHash for user: {}", e.getMessage());
            throw new RuntimeException("Failed to get booking: " + e.getMessage());
        }
    }

    /**
     * Gets all bookings for the current user
     */
    public List<Map<String, Object>> getAllBookings(String userAddress) {
        try {
            // Call Booking contract's getAllBookings (returns only user's bookings)
            List<Object> rawBookings = bookingContract.getAllBookings().send();
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
            return bookings;
        } catch (Exception e) {
            logger.error("Error getting all user bookings: {}", e.getMessage());
            throw new RuntimeException("Failed to get all user bookings: " + e.getMessage());
        }
    }

    /**
     * Cancels a booking by ipfsHash for the current user
     */
    public String cancelBooking(String userAddress, String ipfsHash) {
        try {
            if (userAddress == null || userAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("User address is required");
            }
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                throw new IllegalArgumentException("ipfsHash is required");
            }
            // For this contract, you may need to pass the same ipfsHash twice (see Booking.sol)
            org.web3j.protocol.core.methods.response.TransactionReceipt receipt =
                bookingContract.cancelBooking(ipfsHash, ipfsHash).send();
            if (receipt.isStatusOK()) {
                logger.info("User {} cancelled booking: {}", userAddress, ipfsHash);
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking cancellation failed on-chain");
        } catch (Exception e) {
            logger.error("Error cancelling booking for user {}: {}", userAddress, e.getMessage());
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
        }
    }
}