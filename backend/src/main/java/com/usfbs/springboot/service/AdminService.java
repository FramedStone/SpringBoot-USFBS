package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;
import com.usfbs.springboot.contracts.Booking;
import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.PinataManifest;
import com.usfbs.springboot.dto.SportFacilityResponse;
import com.usfbs.springboot.dto.SportFacilityDetailResponse;
import com.usfbs.springboot.util.PinataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple4;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.RawTransactionManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;

@Service
public class AdminService {
    
    private final Map<String, Long> recentRequests = new ConcurrentHashMap<>();
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    private final PinataUtil pinataUtil;
    private final Management managementContract;
    
    @Autowired
    private SportFacility sportFacilityContract;

    @Autowired
    private Booking bookingContract;

    @Autowired
    public AdminService(PinataUtil pinataUtil, Management managementContract) {
        this.pinataUtil = pinataUtil;
        this.managementContract = managementContract;
    }

    @Autowired
    private RawTransactionManager rawTransactionManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private EventLogService eventLogService;

    // Store mapping from oldIpfsHash to newIpfsHash for the latest booking event
    private final Map<String, String> latestIpfsHashMap = new ConcurrentHashMap<>();

    public void addUser(String userAddress) throws Exception {
        managementContract.addUser(userAddress).send();
    }

    /**
     * Gets all users from the blockchain
     */
    public List<Map<String, Object>> getAllUsers() throws Exception {
        try {
            // Get User structs instead of just addresses
            RemoteFunctionCall<List> usersCall = managementContract.getUsers();
            List<Object> rawResult = usersCall.send();
            
            List<Map<String, Object>> users = new ArrayList<>();
            
            for (Object obj : rawResult) {
                Map<String, Object> userInfo = new HashMap<>();
                
                try {
                    // Extract User struct data using reflection
                    String userAddress = _extractUserAddress(obj);
                    String bannedReason = _extractBannedReason(obj);
                    
                    userInfo.put("userAddress", userAddress);
                    
                    // Get status from contract
                    boolean isRegistered = managementContract.getUser(userAddress).send();
                    boolean isBanned = managementContract.getBannedUser(userAddress).send();
                    
                    userInfo.put("isRegistered", isRegistered);
                    userInfo.put("isBanned", isBanned);
                    userInfo.put("status", determineUserStatus(isRegistered, isBanned));
                    
                    // Add ban reason from blockchain
                    if (isBanned && bannedReason != null && !bannedReason.equals("-")) {
                        userInfo.put("banReason", bannedReason);
                    } else {
                        userInfo.put("banReason", null);
                    }
                    
                    // Add email from AuthService cache if available
                    String email = authService.getUserEmailByAddress(userAddress);
                    userInfo.put("email", email);
                    
                } catch (Exception e) {
                    // If we can't get status, mark as unknown
                    userInfo.put("isRegistered", false);
                    userInfo.put("isBanned", false);
                    userInfo.put("status", "UNKNOWN");
                    userInfo.put("banReason", null);
                    userInfo.put("email", "Unknown");
                    logger.warn("Could not determine status for user: {}", e.getMessage());
                }
                
                users.add(userInfo);
            }
            
            logger.info("Retrieved {} users from blockchain with ban reasons", users.size());
            return users;
            
        } catch (Exception e) {
            if (e.getMessage().contains("No registered users found in blockchain")) {
                logger.info("No users found in blockchain - returning empty list");
                return new ArrayList<>();
            }
            
            logger.error("Error getting all users: {}", e.getMessage());
            throw new Exception("Failed to get users: " + e.getMessage());
        }
    }

    /**
     * Gets user status (registered and banned status)
     */
    public Map<String, Object> getUserStatus(String userAddress) throws Exception {
        try {
            boolean isRegistered = managementContract.getUser(userAddress).send();
            boolean isBanned = managementContract.getBannedUser(userAddress).send();
            
            Map<String, Object> status = new HashMap<>();
            status.put("userAddress", userAddress);
            status.put("isRegistered", isRegistered);
            status.put("isBanned", isBanned);
            status.put("status", determineUserStatus(isRegistered, isBanned));
            
            logger.info("Retrieved status for user {}: registered={}, banned={}", 
                       userAddress, isRegistered, isBanned);
            
            return status;
            
        } catch (Exception e) {
            logger.error("Error getting user status for {}: {}", userAddress, e.getMessage());
            throw new Exception("Failed to get user status: " + e.getMessage());
        }
    }


    /**
     * Bans a user with a reason
     */
    public String banUser(String userAddress, String reason) throws Exception {
        try {
            // Validate inputs
            if (userAddress == null || userAddress.trim().isEmpty()) {
                throw new Exception("User address is required");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new Exception("Ban reason is required");
            }
            
            TransactionReceipt receipt = managementContract.banUser(userAddress, reason).send();
            
            if (receipt.isStatusOK()) {
                logger.info("User {} banned successfully with reason: {}", userAddress, reason);
                return String.format("User '%s' has been banned successfully", userAddress);
            }
            
            return "Failed to ban user";
            
        } catch (Exception e) {
            logger.error("Error banning user {}: {}", userAddress, e.getMessage());
            throw new Exception("Failed to ban user: " + e.getMessage());
        }
    }

    /**
     * Unbans a user with a reason
     */
    public String unbanUser(String userAddress, String reason) throws Exception {
        try {
            // Validate inputs
            if (userAddress == null || userAddress.trim().isEmpty()) {
                throw new Exception("User address is required");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new Exception("Unban reason is required");
            }
            
            TransactionReceipt receipt = managementContract.unbanUser(userAddress, reason).send();
            
            if (receipt.isStatusOK()) {
                logger.info("User {} unbanned successfully with reason: {}", userAddress, reason);
                return String.format("User '%s' has been unbanned successfully", userAddress);
            }
            
            return "Failed to unban user";
            
        } catch (Exception e) {
            logger.error("Error unbanning user {}: {}", userAddress, e.getMessage());
            throw new Exception("Failed to unban user: " + e.getMessage());
        }
    }

    /**
     * Determines user status based on registration and ban status
     */
    private String determineUserStatus(boolean isRegistered, boolean isBanned) {
        if (isBanned) {
            return "BANNED";
        } else if (isRegistered) {
            return "ACTIVE";
        } else {
            return "NOT_REGISTERED";
        }
    }

    /**
     * Uploads an announcement with a file and metadata
     */
    public String uploadAnnouncement(MultipartFile file, String title, long startDate, long endDate) throws Exception {
        // Pin raw file to get CID
        String fileCid = pinataUtil.uploadFileToIPFS(
            file.getBytes(),
            file.getOriginalFilename()
        );

        // Build metadata JSON manifest
        Map<String, Object> manifest = Map.of(
            "title", title,
            "startDate", startDate,
            "endDate", endDate,
            "fileCid", fileCid
        );

        // Pin the JSON manifest to get its CID with announcement title
        String manifestFileName = sanitizeFileName(title) + "-manifest.json";
        String metaCid = pinataUtil.uploadJsonToIPFS(manifest, manifestFileName);

        // Store the manifest CID on-chain - FIXED: Added title parameter
        TransactionReceipt receipt = managementContract.addAnnouncement(
            metaCid,           // ipfsHash
            title,             // title
            BigInteger.valueOf(startDate),  // startTime
            BigInteger.valueOf(endDate)     // endTime
        ).send();

        return receipt.getTransactionHash();
    }

    /**
     * Updates an existing announcement with a new file and metadata
     */
    public String updateAnnouncement(String oldIpfsHash, MultipartFile newFile, String newTitle, 
                                   long newStartDate, long newEndDate) throws Exception {
        try {
            // Get old manifest to extract old file CID for cleanup
            String oldManifestJson = fetchManifestFromIPFS(oldIpfsHash);
            PinataManifest oldManifest = parseManifest(oldManifestJson);
            String oldFileCid = oldManifest.getFileCid();

            String newFileCid;
            if (newFile != null && !newFile.isEmpty()) {
                // Pin new file to get new CID
                newFileCid = pinataUtil.uploadFileToIPFS(
                    newFile.getBytes(),
                    newFile.getOriginalFilename()
                );
            } else {
                // Keep existing file if no new file provided
                newFileCid = oldFileCid;
            }

            // Build new metadata JSON manifest
            Map<String, Object> newManifest = Map.of(
                "title", newTitle,
                "startDate", newStartDate,
                "endDate", newEndDate,
                "fileCid", newFileCid
            );

            // Pin the new JSON manifest to get its CID with new title
            String manifestFileName = sanitizeFileName(newTitle) + "-manifest.json";
            String newMetaCid = pinataUtil.uploadJsonToIPFS(newManifest, manifestFileName);

            // Use the unified updateAnnouncement method
            TransactionReceipt receipt = managementContract.updateAnnouncement(
                oldIpfsHash,      // ipfsHash_ (old)
                newMetaCid,       // ipfsHash (new)
                newTitle,         // title
                BigInteger.valueOf(newStartDate),  // startTime
                BigInteger.valueOf(newEndDate)     // endTime
            ).send();

            // Cleanup old IPFS files after successful blockchain updates
            try {
                pinataUtil.unpinFromIPFS(oldIpfsHash); // Unpin old manifest
                if (newFile != null && !newFile.isEmpty() && !oldFileCid.equals(newFileCid)) {
                    pinataUtil.unpinFromIPFS(oldFileCid);  // Only unpin old file if we uploaded a new one
                }
                System.out.println("Successfully cleaned up old IPFS files: " + oldIpfsHash + 
                                 (newFile != null && !newFile.isEmpty() ? ", " + oldFileCid : ""));
            } catch (Exception e) {
                System.err.println("Warning: Failed to unpin old IPFS files: " + e.getMessage());
                // Continue execution as blockchain update was successful
            }

            return receipt.getTransactionHash();

        } catch (Exception e) {
            System.err.println("Failed to update announcement: " + e.getMessage());
            throw new Exception("Announcement update failed: " + e.getMessage());
        }
    }

    /**
     * Deletes an announcement and cleans up associated IPFS files
     */
    public String deleteAnnouncement(String ipfsHash) throws Exception {
        try {
            // Get manifest to extract file CID for cleanup
            String manifestJson = fetchManifestFromIPFS(ipfsHash);
            PinataManifest manifest = parseManifest(manifestJson);
            String fileCid = manifest.getFileCid();

            // Delete announcement from blockchain using Management contract function
            TransactionReceipt receipt = managementContract.deleteAnnouncement(ipfsHash).send();

            // Cleanup IPFS files after successful blockchain deletion
            try {
                pinataUtil.unpinFromIPFS(ipfsHash); // Unpin manifest
                pinataUtil.unpinFromIPFS(fileCid);  // Unpin file
                System.out.println("Successfully cleaned up IPFS files: " + ipfsHash + ", " + fileCid);
            } catch (Exception e) {
                System.err.println("Warning: Failed to unpin IPFS files: " + e.getMessage());
                // Continue execution as blockchain deletion was successful
            }

            System.out.println("Successfully deleted announcement with IPFS hash: " + ipfsHash);
            return receipt.getTransactionHash();

        } catch (Exception e) {
            System.err.println("Failed to delete announcement: " + e.getMessage());
            throw new Exception("Announcement deletion failed: " + e.getMessage());
        }
    }

    /**
     * Retrieves all announcements from the blockchain
     */
    public List<AnnouncementItem> getAllAnnouncements() throws Exception {
        try {
            // Use the correct Web3j pattern for view functions
            RemoteFunctionCall<List> announcementsCall = managementContract.getAnnouncements();
            
            // Execute the call directly
            List<Object> rawResult = announcementsCall.send();
            
            List<AnnouncementItem> announcementItems = new ArrayList<>();
            
            // Process the raw result to extract Management.Announcement objects
            for (Object obj : rawResult) {
                try {
                    AnnouncementItem item = null;
                    
                    // Try direct casting first
                    if (obj instanceof Management.Announcement) {
                        Management.Announcement announcement = (Management.Announcement) obj;
                        item = processAnnouncementDirect(announcement);
                    } else {
                        // Use reflection as fallback for ClassLoader conflicts
                        item = extractAnnouncementFromObject(obj);
                    }
                    
                    if (item != null) {
                        announcementItems.add(item);
                    }
                    
                } catch (Exception e) {
                    System.err.println("Failed to process announcement object: " + e.getMessage());
                    // Continue processing other announcements
                }
            }

            System.out.println("Successfully retrieved " + announcementItems.size() + " announcements from contract");
            return announcementItems;

        } catch (Exception e) {
            System.err.println("getAllAnnouncements error: " + e.getMessage());
            
            // Handle specific blockchain error for empty announcements
            if (e.getMessage().contains("No Announcement found in blockchain")) {
                System.out.println("No announcements found in blockchain - returning empty list");
                return new ArrayList<>();
            }
            
            throw new Exception("Failed to get announcements from contract: " + e.getMessage());
        }
    }

    private AnnouncementItem processAnnouncementDirect(Management.Announcement announcement) throws Exception {
        String ipfsHash = announcement.ipfsHash;

        // Skip empty or deleted announcements
        if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
            return null;
        }

        try {
            // Fetch the manifest JSON from IPFS using the CID
            String manifestJson = fetchManifestFromIPFS(ipfsHash);
            PinataManifest manifest = parseManifest(manifestJson);

            // Create AnnouncementItem with both IPFS and blockchain data
            AnnouncementItem item = new AnnouncementItem();
            item.setIpfsHash(ipfsHash);
            item.setTitle(manifest.getTitle());
            item.setStartDate(manifest.getStartDate());
            item.setEndDate(manifest.getEndDate());
            item.setFileCid(manifest.getFileCid());
            item.setBlockchainStartDate(announcement.startTime.longValue());
            item.setBlockchainEndDate(announcement.endTime.longValue());

            return item;
        } catch (Exception e) {
            System.err.println("Failed to process announcement with IPFS hash " + ipfsHash + ": " + e.getMessage());
            return null;
        }
    }

    private AnnouncementItem extractAnnouncementFromObject(Object obj) throws Exception {
        try {
            // Use reflection to extract data when direct casting fails
            Class<?> announcementClass = obj.getClass();
            
            // Extract fields using reflection based on Management.Announcement structure
            java.lang.reflect.Field ipfsHashField = announcementClass.getDeclaredField("ipfsHash");
            java.lang.reflect.Field startTimeField = announcementClass.getDeclaredField("startTime");
            java.lang.reflect.Field endTimeField = announcementClass.getDeclaredField("endTime");
            
            ipfsHashField.setAccessible(true);
            startTimeField.setAccessible(true);
            endTimeField.setAccessible(true);
            
            String ipfsHash = (String) ipfsHashField.get(obj);
            BigInteger startTime = (BigInteger) startTimeField.get(obj);
            BigInteger endTime = (BigInteger) endTimeField.get(obj);
            
            // Skip empty announcements
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                return null;
            }
            
            try {
                // Fetch the manifest JSON from IPFS
                String manifestJson = fetchManifestFromIPFS(ipfsHash);
                PinataManifest manifest = parseManifest(manifestJson);
                
                // Create AnnouncementItem
                AnnouncementItem item = new AnnouncementItem();
                item.setIpfsHash(ipfsHash);
                item.setTitle(manifest.getTitle());
                item.setStartDate(manifest.getStartDate());
                item.setEndDate(manifest.getEndDate());
                item.setFileCid(manifest.getFileCid());
                item.setBlockchainStartDate(startTime.longValue());
                item.setBlockchainEndDate(endTime.longValue());
                
                return item;
            } catch (Exception e) {
                System.err.println("Failed to fetch IPFS data for hash " + ipfsHash + ": " + e.getMessage());
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Failed to extract announcement data using reflection: " + e.getMessage());
            return null;
        }
    }

    /**
     * Fetches manifest JSON from IPFS using the Pinata utility
     */
    public String fetchManifestFromIPFS(String ipfsHash) throws Exception {
        return pinataUtil.fetchFromIPFS(ipfsHash);
    }

    /**
     * Parses manifest JSON string into PinataManifest object
     */
    public PinataManifest parseManifest(String manifestJson) throws Exception {
        return pinataUtil.parseManifest(manifestJson);
    }

    /**
     * Sanitizes announcement title for use as filename
     * @param title The announcement title
     * @return Sanitized filename string
     */
    private String sanitizeFileName(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "untitled-announcement";
        }
        
        // Replace invalid filename characters with hyphens
        String sanitized = title.trim()
            .replaceAll("[^a-zA-Z0-9\\s\\-_]", "")  // Remove special chars
            .replaceAll("\\s+", "-")                // Replace spaces with hyphens
            .toLowerCase()                          
            .replaceAll("-+", "-");                 // Replace multiple hyphens with single
        
        // Ensure filename isn't too long (max 50 chars before extension)
        if (sanitized.length() > 50) {
            sanitized = sanitized.substring(0, 50);
        }
        
        // Remove trailing hyphens
        sanitized = sanitized.replaceAll("-+$", "");
        
        return sanitized.isEmpty() ? "untitled-announcement" : sanitized;
    }

    /**
     * Enhanced status conversion with proper handling of all status types
     */
    private String getStatusString(BigInteger status) {
        if (status == null) {
            logger.warn("Status is null, defaulting to UNKNOWN");
            return "UNKNOWN";
        }
        
        int statusValue = status.intValue();
        String statusString;
        
        switch (statusValue) {
            case 0: 
                statusString = "OPEN";
                break;
            case 1: 
                statusString = "CLOSED";
                break;
            case 2: 
                statusString = "MAINTENANCE";
                break;
            case 3: 
                statusString = "BOOKED";
                break;
            default: 
                logger.warn("Unknown status value: {}", statusValue);
                statusString = "UNKNOWN";
                break;
        }
        
        logger.debug("Converted status {} to string: {}", statusValue, statusString);
        return statusString;
    }

    // Sport Facility CRUD Operations
    public String addSportFacility(String facilityName, String facilityLocation, String imageIPFS,
                                  BigInteger facilityStatus, List<SportFacility.court> facilityCourts) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .addSportFacility(facilityName, facilityLocation, imageIPFS, facilityStatus, facilityCourts)
                .send();

            if (receipt.isStatusOK()) {
                List<SportFacility.SportFacilityAddedEventResponse> events =
                    SportFacility.getSportFacilityAddedEvents(receipt);

                if (!events.isEmpty()) {
                    SportFacility.SportFacilityAddedEventResponse event = events.get(0);
                    return String.format("Sport facility '%s' added successfully at location '%s' with %d courts",
                        event.facilityName, event.Location, facilityCourts.size());
                }
            }
            return "Sport facility added but event not found";
        } catch (Exception e) {
            logger.error("Error adding sport facility: {}", e.getMessage());
            throw new RuntimeException("Failed to add sport facility: " + e.getMessage());
        }
    }

    public List<SportFacilityResponse> getAllSportFacilities() {
        try {
            Tuple4<List<String>, List<String>, List<String>, List<BigInteger>> result = 
                sportFacilityContract.getAllSportFacility().send();

            List<String> names = result.component1();
            List<String> locations = result.component2();
            List<String> imageIPFSList = result.component3();
            List<BigInteger> statuses = result.component4();

            List<SportFacilityResponse> facilities = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                SportFacilityResponse facility = new SportFacilityResponse();
                facility.setName(names.get(i));
                facility.setLocation(locations.get(i));
                facility.setImageIPFS(imageIPFSList.get(i));
                facility.setStatus(getStatusString(statuses.get(i)));
                facilities.add(facility);
            }

            return facilities;
        } catch (Exception e) {
            logger.error("Error getting all sport facilities: {}", e.getMessage());
            throw new RuntimeException("Failed to get sport facilities: " + e.getMessage());
        }
    }

    public SportFacilityDetailResponse getSportFacility(String facilityName) {
        try {
            org.web3j.tuples.generated.Tuple5<String, String, String, BigInteger, List<SportFacility.court>> result =
                sportFacilityContract.getSportFacility(facilityName).send();

        SportFacilityDetailResponse facility = new SportFacilityDetailResponse();
        facility.setName(result.component1());
        facility.setLocation(result.component2());
        facility.setImageIPFS(result.component3()); // Make sure your DTO has this field
        facility.setStatus(getStatusString(result.component4()));
        facility.setCourts(result.component5());

        return facility;
        } catch (Exception e) {
            logger.error("Error getting sport facility {}: {}", facilityName, e.getMessage());
            throw new RuntimeException("Failed to get sport facility: " + e.getMessage());
        }
    }

    /**
     * Updates an existing sport facility.
     * If newImageFile is provided, upload to IPFS, unpin old image, and update imageIPFS.
     * Accepts newStatus as optional (can be null).
     */
    public String updateSportFacility(String oldName, String newName, String newLocation, String newImageIPFS, BigInteger newStatus) {
        try {
            SportFacilityDetailResponse current = getSportFacility(oldName);

            String finalName = (newName != null && !newName.isEmpty()) ? newName : current.getName();
            String finalLocation = (newLocation != null && !newLocation.isEmpty()) ? newLocation : current.getLocation();
            String finalImageIPFS = (newImageIPFS != null && !newImageIPFS.isEmpty()) ? newImageIPFS : current.getImageIPFS();

            // If new imageIPFS is provided and different, unpin old image
            if (newImageIPFS != null && !newImageIPFS.isEmpty() && !newImageIPFS.equals(current.getImageIPFS())) {
                if (current.getImageIPFS() != null && !current.getImageIPFS().isEmpty()) {
                    try {
                        pinataUtil.unpinFromIPFS(current.getImageIPFS());
                        logger.info("Unpinned old facility image from IPFS: {}", current.getImageIPFS());
                    } catch (Exception e) {
                        logger.warn("Failed to unpin old facility image: {}", e.getMessage());
                    }
                }
            }

            BigInteger finalStatus = (newStatus != null) ? newStatus :
                ("OPEN".equalsIgnoreCase(current.getStatus()) ? BigInteger.ZERO :
                 "CLOSED".equalsIgnoreCase(current.getStatus()) ? BigInteger.ONE :
                 "MAINTENANCE".equalsIgnoreCase(current.getStatus()) ? BigInteger.valueOf(2) : null);

            TransactionReceipt receipt = sportFacilityContract
                .updateSportFacility(oldName, finalName, finalLocation, finalImageIPFS, finalStatus)
                .send();
            if (receipt.isStatusOK()) {
                return String.format("Sport facility '%s' updated successfully", oldName);
            }
            return "Failed to update sport facility";
        } catch (Exception e) {
            logger.error("Error updating sport facility: {}", e.getMessage());
            throw new RuntimeException("Failed to update sport facility: " + e.getMessage());
        }
    }

    /**
     * Deletes a sport facility by name and cleans up its image from IPFS
     */
    public String deleteSportFacility(String facilityName) {
        try {
            // 1. Get facility details to retrieve image IPFS CID
            SportFacilityDetailResponse facility = getSportFacility(facilityName);
            String imageIPFS = facility.getImageIPFS();

            // 2. Delete facility from blockchain
            TransactionReceipt receipt = sportFacilityContract
                .deleteSportFacility(facilityName)
                .send();

            // 3. Unpin image from Pinata/IPFS if present
            if (imageIPFS != null && !imageIPFS.trim().isEmpty()) {
                try {
                    pinataUtil.unpinFromIPFS(imageIPFS);
                    logger.info("Unpinned facility image from IPFS: {}", imageIPFS);
                } catch (Exception e) {
                    logger.warn("Failed to unpin facility image from IPFS: {}", e.getMessage());
                }
            }

            if (receipt.isStatusOK()) {
                return String.format("Sport facility '%s' deleted successfully", facilityName);
            }
            return "Failed to delete sport facility";
        } catch (Exception e) {
            logger.error("Error deleting sport facility: {}", e.getMessage());
            throw new RuntimeException("Failed to delete sport facility: " + e.getMessage());
        }
    }

    /**
     * Adds one or more courts to a sport facility
     */
    public String addCourt(String facilityName, List<SportFacility.court> courts) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .addCourt(facilityName, courts)
                .send();
            if (receipt.isStatusOK()) {
                return String.format("Court(s) added to facility '%s' successfully", facilityName);
            }
            return "Failed to add court(s)";
        } catch (Exception e) {
            logger.error("Error adding court(s): {}", e.getMessage());
            throw new RuntimeException("Failed to add court(s): " + e.getMessage());
        }
    }

    // Get a single court by facility and court name
    public Map<String, Object> getCourt(String facilityName, String courtName) {
        try {
            Tuple4<String, BigInteger, BigInteger, BigInteger> tuple =
                sportFacilityContract.getCourt(facilityName, courtName).send();

        Map<String, Object> courtMap = new HashMap<>();
        courtMap.put("name", tuple.component1());
        courtMap.put("earliestTime", tuple.component2());
        courtMap.put("latestTime", tuple.component3());
        courtMap.put("status", tuple.component4());
        return courtMap;
        } catch (Exception e) {
            logger.error("Error getting court: {}", e.getMessage());
            throw new RuntimeException("Failed to get court: " + e.getMessage());
        }
    }

    // Get all courts for a facility
    public List<SportFacility.court> getAllCourts(String facilityName) {
        try {
            return sportFacilityContract.getAllCourts(facilityName).send();
        } catch (Exception e) {
            logger.error("Error getting all courts: {}", e.getMessage());
            throw new RuntimeException("Failed to get all courts: " + e.getMessage());
        }
    }

    /**
     * Gets the available time range for a court in a facility
     */
    public List<BigInteger> getAvailableTimeRange(String facilityName, String courtName) {
        try {
            // Returns a Tuple2<BigInteger, BigInteger>
            org.web3j.tuples.generated.Tuple2<BigInteger, BigInteger> result =
                sportFacilityContract.getAvailableTimeRange(facilityName, courtName).send();
            List<BigInteger> timeRange = new ArrayList<>();
            timeRange.add(result.component1());
            timeRange.add(result.component2());
            return timeRange;
        } catch (Exception e) {
            logger.error("Error getting available time range: {}", e.getMessage());
            throw new RuntimeException("Failed to get available time range: " + e.getMessage());
        }
    }

    public String updateCourt(String facilityName, String oldCourtName, String newCourtName,
                         BigInteger earliestTime, BigInteger latestTime, BigInteger status) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .updateCourt(facilityName, oldCourtName, newCourtName, earliestTime, latestTime, status)
                .send();
            if (receipt.isStatusOK()) {
                return String.format("Court '%s' updated successfully", oldCourtName);
            }
            return "Failed to update court";
        } catch (Exception e) {
            logger.error("Error updating court: {}", e.getMessage());
            throw new RuntimeException("Failed to update court: " + e.getMessage());
        }
    }

    /**
     * Deletes a court from a sport facility
     */
    public String deleteCourt(String facilityName, String courtName) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .deleteCourt(facilityName, courtName)
                .send();
            if (receipt.isStatusOK()) {
                return String.format("Court '%s' deleted successfully from facility '%s'", courtName, facilityName);
            }
            return "Failed to delete court";
        } catch (Exception e) {
            logger.error("Error deleting court: {}", e.getMessage());
            throw new RuntimeException("Failed to delete court: " + e.getMessage());
        }
    }

    private String _extractUserAddress(Object userStruct) throws Exception {
        try {
            Class<?> userClass = userStruct.getClass();
            java.lang.reflect.Field userAddressField = userClass.getDeclaredField("userAddress");
            userAddressField.setAccessible(true);
            return (String) userAddressField.get(userStruct);
        } catch (Exception e) {
            logger.error("Failed to extract user address: {}", e.getMessage());
            throw new Exception("Failed to extract user address from struct");
        }
    }

    /**
     * Extracts banned reason from User struct object
     */
    private String _extractBannedReason(Object userStruct) throws Exception {
        try {
            Class<?> userClass = userStruct.getClass();
            java.lang.reflect.Field bannedReasonField = userClass.getDeclaredField("bannedReason");
            bannedReasonField.setAccessible(true);
            String reason = (String) bannedReasonField.get(userStruct);
            return reason != null && !reason.trim().isEmpty() && !reason.equals("-") ? reason : null;
        } catch (Exception e) {
            logger.warn("Failed to extract banned reason: {}", e.getMessage());
            return null;
        }
    }

    private String _getFileUrl(String cid) {
        // TODO: Get gateway URL from environment variable
        String pinataGateway = "gateway.pinata.cloud"; 
        return String.format("https://%s/ipfs/%s", pinataGateway, cid);
    }

    /**
     * Formats duration in seconds to human-readable string
     */
    private String _formatDuration(long durationSeconds) {
        if (durationSeconds <= 0) {
            return "0 hours";
        }
        
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        
        if (hours > 0 && minutes > 0) {
            return String.format("%d hour%s %d minute%s", 
                hours, hours == 1 ? "" : "s", 
                minutes, minutes == 1 ? "" : "s");
        } else if (hours > 0) {
            return String.format("%d hour%s", hours, hours == 1 ? "" : "s");
        } else {
            return String.format("%d minute%s", minutes, minutes == 1 ? "" : "s");
        }
    }

    public String uploadFacilityImageToIPFS(MultipartFile imageFile) {
        try {
            return pinataUtil.uploadFileToIPFS(
                imageFile.getBytes(),
                imageFile.getOriginalFilename()
            );
        } catch (Exception e) {
            logger.error("Error uploading facility image to IPFS: {}", e.getMessage());
            throw new RuntimeException("Failed to upload facility image to IPFS: " + e.getMessage());
        }
    }

    public String createBooking(String userAddress, String ipfsHash, String facilityName, String courtName, BigInteger startTime, BigInteger endTime, String status) {
        try {
            if (userAddress == null || userAddress.trim().isEmpty()) throw new IllegalArgumentException("userAddress is required");
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) throw new IllegalArgumentException("ipfsHash is required");
            if (facilityName == null || facilityName.trim().isEmpty()) throw new IllegalArgumentException("facilityName is required");
            if (courtName == null || courtName.trim().isEmpty()) throw new IllegalArgumentException("courtName is required");
            if (startTime == null || endTime == null || endTime.compareTo(startTime) <= 0) throw new IllegalArgumentException("Invalid time range");

            Booking.timeSlot timeSlot = new Booking.timeSlot(startTime, endTime);
            // Pass userAddress as owner
            TransactionReceipt receipt = bookingContract.createBooking(userAddress, ipfsHash, facilityName, courtName, timeSlot).send();
            List<Booking.BookingCreatedEventResponse> events = Booking.getBookingCreatedEvents(receipt);
            if (!events.isEmpty()) {
                Booking.BookingCreatedEventResponse event = events.get(0);
                String eventStatus = event.status; 
                handleBookingCreatedEvent(
                    ipfsHash,
                    facilityName,
                    courtName,
                    startTime.longValue(),
                    endTime.longValue(),
                    eventStatus 
                );
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking creation failed on-chain");
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage());
            throw new RuntimeException("Failed to create booking: " + e.getMessage());
        }
    }

    /**
     * Gets a single booking by ipfsHash (admin only)
     */
    public Map<String, Object> getBookingByIpfsHash(String ipfsHash) {
        try {
            Object bookingObj = bookingContract.getBooking_(ipfsHash).send();
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

            bookingMap.put("owner", ownerField.get(bookingObj));
            bookingMap.put("ipfsHash", ipfsHashField.get(bookingObj));
            bookingMap.put("facilityName", fnameField.get(bookingObj));
            bookingMap.put("courtName", cnameField.get(bookingObj));
            bookingMap.put("startTime", startTimeField.get(timeObj));
            bookingMap.put("endTime", endTimeField.get(timeObj));
            bookingMap.put("status", statusField.get(bookingObj).toString());

            return bookingMap;
        } catch (Exception e) {
            logger.error("Error getting booking by ipfsHash: {}", e.getMessage());
            throw new RuntimeException("Failed to get booking: " + e.getMessage());
        }
    }

    public List<Object> getAllBookings(String userAddress) {
        try {
            return bookingContract.getAllBookings(userAddress).send();
        } catch (Exception e) {
            logger.error("Error getting user bookings: {}", e.getMessage());
            throw new RuntimeException("Failed to get user bookings: " + e.getMessage());
        }
    }

    /**
     * Gets all bookings from the blockchain 
     */
    public List<Map<String, Object>> getAllBookings_() {
        try {
            // Call Booking contract's getAllBookings_ (admin only)
            List<Object> rawBookings = bookingContract.getAllBookings_().send();
            List<Map<String, Object>> bookings = new ArrayList<>();

            for (Object obj : rawBookings) {
                Map<String, Object> bookingMap = new HashMap<>();
                try {
                    Class<?> bookingClass = obj.getClass();
                    // Extract fields using reflection
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

                    String owner = (String) ownerField.get(obj);
                    bookingMap.put("owner", owner);
                    bookingMap.put("userEmail", authService.getUserEmailByAddress(owner)); 
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
            logger.error("Error getting all bookings: {}", e.getMessage());
            throw new RuntimeException("Failed to get all bookings: " + e.getMessage());
        }
    }

    // Get all booked timeslots for a court 
    public List<Map<String, Object>> getBookedTimeSlots(String facilityName, String courtName) {
        try {
            List<Object> rawSlots = bookingContract.getBookedTimeSlots(facilityName, courtName).send();
            List<Map<String, Object>> slots = new ArrayList<>();
            for (Object obj : rawSlots) {
                try {
                    Class<?> slotClass = obj.getClass();
                    java.lang.reflect.Field startTimeField = slotClass.getDeclaredField("startTime");
                    java.lang.reflect.Field endTimeField = slotClass.getDeclaredField("endTime");
                    startTimeField.setAccessible(true);
                    endTimeField.setAccessible(true);
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("startTime", startTimeField.get(obj));
                    slot.put("endTime", endTimeField.get(obj));
                    slots.add(slot);
                } catch (Exception e) {
                    logger.warn("Could not extract timeslot fields: {}", e.getMessage());
                }
            }
            return slots;
        } catch (Exception e) {
            logger.error("Error getting booked timeslots: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Completes a booking by old and new ipfsHash (admin only)
     */
    public String completeBooking(String oldIpfsHash, String newIpfsHash) {
        try {
            if (oldIpfsHash == null || oldIpfsHash.trim().isEmpty() || newIpfsHash == null || newIpfsHash.trim().isEmpty()) {
                throw new IllegalArgumentException("Both oldIpfsHash and newIpfsHash are required");
            }
            // Fetch booking details before completion for logging
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                getBookingTupleByIpfsHash(oldIpfsHash);

            String userAddress = bookingTuple.component1();
            String facilityName = bookingTuple.component3();
            String courtName = bookingTuple.component4();
            long startTime = bookingTuple.component5() != null ? bookingTuple.component5().longValue() : 0L;
            long endTime = bookingTuple.component6() != null ? bookingTuple.component6().longValue() : 0L;

            // Build booking map
            Map<String, Object> booking = Map.of(
                "facilityName", facilityName,
                "courtName", courtName,
                "startTime", startTime,
                "endTime", endTime,
                "status", "completed",
                "userAddress", userAddress
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            java.time.LocalDate bookingDate = startTime != 0L
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload new JSON to IPFS
            String actualNewIpfsHash = pinataUtil.uploadJsonToIPFS(booking, fileName);

            // Complete booking on-chain (get txHash and event)
            org.web3j.protocol.core.methods.response.TransactionReceipt receipt =
                bookingContract.completeBooking(oldIpfsHash, actualNewIpfsHash).send();

            // Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(oldIpfsHash);

            // Update the new IPFS hash on-chain
            if (actualNewIpfsHash != null && !actualNewIpfsHash.equals(oldIpfsHash)) {
                updateBookingIPFSHash(oldIpfsHash, actualNewIpfsHash);
            }

            // --- For AdminService: Booking Created, Cancelled, Rejected, Completed ---
            // Use this pattern for logOutput in all relevant methods:

            // 1. Format time as "{date} {startTime} to {endTime}" using system default timezone
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String dateStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString();
            String startTimeStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String endTimeStr = java.time.Instant.ofEpochSecond(endTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String timeRange = String.format("%s %s to %s", dateStr, startTimeStr, endTimeStr);

            // 2. Use in logOutput for all booking events
            String logOutput = String.format(
                "Booking receipt updated: oldIpfsHash=%s, newIpfsHash=%s, facility=%s, court=%s, status=%s, datetime=%s",
                oldIpfsHash,
                newIpfsHash,
                facilityName,
                courtName,
                "completed",
                timeRange
            );

            // Log the event
            eventLogService.addEventLog(
                actualNewIpfsHash,
                "Booking Completed",
                userAddress,
                java.math.BigInteger.valueOf(System.currentTimeMillis() / 1000),
                logOutput,
                "BOOKING"
            );
            logger.info(logOutput);

            if (receipt.isStatusOK()) {
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking completion failed on-chain");
        } catch (Exception e) {
            logger.error("Error completing booking: {}", e.getMessage());
            throw new RuntimeException("Failed to complete booking: " + e.getMessage());
        }
    }

    /**
     * Rejects a booking by ipfsHash (admin only)
     */
    public String rejectBooking(String ipfsHash, String reason) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                throw new IllegalArgumentException("ipfsHash is required");
            }
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Rejection reason is required");
            }
            // Fetch booking details before rejection for logging
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                getBookingTupleByIpfsHash(ipfsHash);

            String userAddress = bookingTuple.component1();
            String facilityName = bookingTuple.component3();
            String courtName = bookingTuple.component4();
            long startTime = bookingTuple.component5() != null ? bookingTuple.component5().longValue() : 0L;
            long endTime = bookingTuple.component6() != null ? bookingTuple.component6().longValue() : 0L;

            // Build booking map
            Map<String, Object> booking = Map.of(
                "facilityName", facilityName,
                "courtName", courtName,
                "startTime", startTime,
                "endTime", endTime,
                "status", "rejected",
                "userAddress", userAddress,
                "rejectionReason", reason
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            java.time.LocalDate bookingDate = startTime != 0L
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload new JSON to IPFS
            String newIpfsHash = pinataUtil.uploadJsonToIPFS(booking, fileName);

            // Reject booking on-chain (get txHash and event)
            org.web3j.protocol.core.methods.response.TransactionReceipt receipt =
                bookingContract.rejectBooking(ipfsHash, newIpfsHash, reason).send();

            // Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(ipfsHash);

            // Update the new IPFS hash on-chain
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                updateBookingIPFSHash(ipfsHash, newIpfsHash);
            }

            // --- For AdminService: Booking Created, Cancelled, Rejected, Completed ---
            // Use this pattern for logOutput in all relevant methods:

            // 1. Format time as "{date} {startTime} to {endTime}" using system default timezone
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String dateStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString();
            String startTimeStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String endTimeStr = java.time.Instant.ofEpochSecond(endTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String timeRange = String.format("%s %s to %s", dateStr, startTimeStr, endTimeStr);

            // 2. Use in logOutput for all booking events
            String logOutput = String.format(
                "Booking receipt updated: oldIpfsHash=%s, newIpfsHash=%s, facility=%s, court=%s, status=rejected, reason=%s, datetime=%s",
                ipfsHash,
                newIpfsHash,
                facilityName,
                courtName,
                reason,
                timeRange
            );

            // Log the event
            eventLogService.addEventLog(
                newIpfsHash,
                "Booking Rejected",
                userAddress,
                java.math.BigInteger.valueOf(System.currentTimeMillis() / 1000),
                logOutput,
                "BOOKING"
            );
            logger.info(logOutput);

            if (receipt.isStatusOK()) {
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking rejection failed on-chain");
        } catch (Exception e) {
            logger.error("Error rejecting booking: {}", e.getMessage());
            throw new RuntimeException("Failed to reject booking: " + e.getMessage());
        }
    }

    /**
     * Cancels a booking by ipfsHash 
     */
    public String cancelBooking(String ipfsHash) {
        try {
            if (ipfsHash == null || ipfsHash.trim().isEmpty()) {
                throw new IllegalArgumentException("ipfsHash is required");
            }
            // Fetch booking details before cancellation for logging
            Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> bookingTuple =
                getBookingTupleByIpfsHash(ipfsHash);

            String userAddress = bookingTuple.component1();
            String facilityName = bookingTuple.component3();
            String courtName = bookingTuple.component4();
            long startTime = bookingTuple.component5() != null ? bookingTuple.component5().longValue() : 0L;
            long endTime = bookingTuple.component6() != null ? bookingTuple.component6().longValue() : 0L;

            // Build booking map
            Map<String, Object> booking = Map.of(
                "facilityName", facilityName,
                "courtName", courtName,
                "startTime", startTime,
                "endTime", endTime,
                "status", "cancelled",
                "userAddress", userAddress
            );

            // Format file name as booking-{yyyyMMdd}.json based on startTime
            java.time.LocalDate bookingDate = startTime != 0L
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // Upload new JSON to IPFS
            String newIpfsHash = pinataUtil.uploadJsonToIPFS(booking, fileName);

            // Cancel booking on-chain (get txHash and event)
            org.web3j.protocol.core.methods.response.TransactionReceipt receipt =
                bookingContract.cancelBooking(ipfsHash, newIpfsHash).send();

            // Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(ipfsHash);

            // Update the new IPFS hash on-chain
            if (newIpfsHash != null && !newIpfsHash.equals(ipfsHash)) {
                updateBookingIPFSHash(ipfsHash, newIpfsHash);
            }

            // --- For AdminService: Booking Created, Cancelled, Rejected, Completed ---
            // Use this pattern for logOutput in all relevant methods:

            // 1. Format time as "{date} {startTime} to {endTime}" using system default timezone
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String dateStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString();
            String startTimeStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String endTimeStr = java.time.Instant.ofEpochSecond(endTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String timeRange = String.format("%s %s to %s", dateStr, startTimeStr, endTimeStr);

            // 2. Use in logOutput for all booking events
            String logOutput = String.format(
                "Booking receipt updated: oldIpfsHash=%s, newIpfsHash=%s, facility=%s, court=%s, status=cancelled, datetime=%s",
                ipfsHash,
                newIpfsHash,
                facilityName,
                courtName,
                timeRange
            );

            // Log the event
            eventLogService.addEventLog(
                newIpfsHash,
                "Booking Cancelled",
                userAddress,
                java.math.BigInteger.valueOf(System.currentTimeMillis() / 1000),
                logOutput,
                "BOOKING"
            );
            logger.info(logOutput);

            if (receipt.isStatusOK()) {
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking cancellation failed on-chain");
        } catch (Exception e) {
            logger.error("Error cancelling booking: {}", e.getMessage());
            throw new RuntimeException("Failed to cancel booking: " + e.getMessage());
        }
    }

    /**
     * Should be called after bookingCreated event is received for admin bookings.
     */
    public void handleBookingCreatedEvent(String oldIpfsHash, String facilityName, String courtName, long startTime, long endTime, String status) {
        try {
            // 1. Fetch old booking JSON from IPFS
            String oldJson = pinataUtil.fetchFromIPFS(oldIpfsHash);
            Map<String, Object> bookingDetails = new ObjectMapper().readValue(oldJson, Map.class);

            // 2. Update status in the booking details
            bookingDetails.put("status", status);

            // 3. Format file name as booking-{yyyyMMdd}.json based on startTime
            java.time.LocalDate bookingDate = startTime != 0L
                ? java.time.Instant.ofEpochSecond(startTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                : java.time.LocalDate.now();
            String fileName = String.format("booking-%s.json", bookingDate.toString().replace("-", ""));

            // 4. Upload new JSON to IPFS
            String newIpfsHash = pinataUtil.uploadJsonToIPFS(bookingDetails, fileName);

            // 5. Unpin old IPFS hash
            pinataUtil.unpinFromIPFS(oldIpfsHash);

            // Store the mapping for later retrieval
            latestIpfsHashMap.put(oldIpfsHash, newIpfsHash);

            // 6. Log both old and new IPFS hashes in the event log, with startTime and endTime as formatted datetime
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String dateStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
                .toString();
            String startTimeStr = java.time.Instant.ofEpochSecond(startTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String endTimeStr = java.time.Instant.ofEpochSecond(endTime)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
            String timeRange = String.format("%s %s to %s", dateStr, startTimeStr, endTimeStr);

            String logOutput = String.format(
                "Booking receipt updated: oldIpfsHash=%s, newIpfsHash=%s, facility=%s, court=%s, status=%s, datetime=%s",
                oldIpfsHash,
                newIpfsHash,
                facilityName,
                courtName,
                status,
                timeRange
            );
            eventLogService.addEventLog(
                newIpfsHash,
                "Booking Created",
                null,
                java.math.BigInteger.valueOf(System.currentTimeMillis() / 1000),
                logOutput,
                "BOOKING"
            );

            logger.info(logOutput);
        } catch (Exception e) {
            logger.error("Failed to update booking receipt on IPFS: {}", e.getMessage());
        }
    }

    /**
     * Retrieve the latest IPFS hash for a booking after handleBookingCreatedEvent.
     */
    public String getLatestBookingIpfsHash(String oldIpfsHash) {
        return latestIpfsHashMap.get(oldIpfsHash);
    }

    /**
     * Updates the IPFS hash of a booking 
     */
    public String updateBookingIPFSHash(String oldIpfsHash, String newIpfsHash) {
        try {
            if (oldIpfsHash == null || oldIpfsHash.trim().isEmpty() || newIpfsHash == null || newIpfsHash.trim().isEmpty()) {
                throw new IllegalArgumentException("Both oldIpfsHash and newIpfsHash are required");
            }
            org.web3j.protocol.core.methods.response.TransactionReceipt receipt =
                bookingContract.updateIPFSHash_(oldIpfsHash, newIpfsHash).send();
            if (receipt.isStatusOK()) {
                logger.info("Booking IPFS hash updated: {} -> {}", oldIpfsHash, newIpfsHash);
                return receipt.getTransactionHash();
            }
            throw new RuntimeException("Booking IPFS hash update failed on-chain");
        } catch (Exception e) {
            logger.error("Error updating booking IPFS hash: {}", e.getMessage());
            throw new RuntimeException("Failed to update booking IPFS hash: " + e.getMessage());
        }
    }

    /**
     * Gets a booking tuple by ipfsHash 
     */
    public Tuple7<String, String, String, String, BigInteger, BigInteger, BigInteger> getBookingTupleByIpfsHash(String ipfsHash) {
        try {
            // getBooking_ returns (address, string, string, string, uint256, uint256, uint8)
            return bookingContract.getBooking_(ipfsHash).send();
        } catch (Exception e) {
            logger.error("Error getting booking tuple by ipfsHash: {}", e.getMessage());
            throw new RuntimeException("Failed to get booking tuple: " + e.getMessage());
        }
    }
}