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
import org.web3j.tx.RawTransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

        // Store the manifest CID on-chain
        TransactionReceipt receipt = managementContract.addAnnouncement(
            metaCid,
            BigInteger.valueOf(startDate),
            BigInteger.valueOf(endDate)
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

            // Pin new file to get new CID
            String newFileCid = pinataUtil.uploadFileToIPFS(
                newFile.getBytes(),
                newFile.getOriginalFilename()
            );

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

            // Update IPFS hash in blockchain using Management contract function
            TransactionReceipt ipfsReceipt = managementContract.updateAnnouncementIpfsHash(
                oldIpfsHash,
                newMetaCid
            ).send();

            // Update time in blockchain using the new CID
            TransactionReceipt timeReceipt = managementContract.updateAnnouncementTime(
                newMetaCid,
                BigInteger.valueOf(newStartDate),
                BigInteger.valueOf(newEndDate)
            ).send();

            // Cleanup old IPFS files after successful blockchain updates
            try {
                pinataUtil.unpinFromIPFS(oldIpfsHash); // Unpin old manifest
                pinataUtil.unpinFromIPFS(oldFileCid);  // Unpin old file
                System.out.println("Successfully cleaned up old IPFS files: " + oldIpfsHash + ", " + oldFileCid);
            } catch (Exception e) {
                System.err.println("Warning: Failed to unpin old IPFS files: " + e.getMessage());
                // Continue execution as blockchain update was successful
            }

            return ipfsReceipt.getTransactionHash();

        } catch (Exception e) {
            System.err.println("Failed to update announcement: " + e.getMessage());
            throw new Exception("Announcement update failed: " + e.getMessage());
        }
    }

    /**
     * Updates only the metadata of an existing announcement
     */
    public String updateAnnouncementMetadata(String ipfsHash, String newTitle, 
                                           long newStartDate, long newEndDate) throws Exception {
        try {
            // Get existing manifest to preserve file CID and get old title
            String existingManifestJson = fetchManifestFromIPFS(ipfsHash);
            PinataManifest existingManifest = parseManifest(existingManifestJson);
            String oldTitle = existingManifest.getTitle();
            
            // Build new metadata JSON manifest with existing file CID
            Map<String, Object> updatedManifest = Map.of(
                "title", newTitle,
                "startDate", newStartDate,
                "endDate", newEndDate,
                "fileCid", existingManifest.getFileCid()
            );

            // Pin the updated JSON manifest to get its new CID with new title
            String manifestFileName = sanitizeFileName(newTitle) + "-manifest.json";
            String updatedMetaCid = pinataUtil.uploadJsonToIPFS(updatedManifest, manifestFileName);

            // Update IPFS hash in blockchain
            TransactionReceipt ipfsReceipt = managementContract.updateAnnouncementIpfsHash(
                ipfsHash,
                updatedMetaCid
            ).send();

            // Update title in blockchain if title changed
            if (!oldTitle.equals(newTitle)) {
                TransactionReceipt titleReceipt = managementContract.updateAnnouncementTitle(
                    updatedMetaCid,
                    oldTitle,
                    newTitle
                ).send();
            }

            // Update time in blockchain
            TransactionReceipt timeReceipt = managementContract.updateAnnouncementTime(
                updatedMetaCid,
                BigInteger.valueOf(newStartDate),
                BigInteger.valueOf(newEndDate)
            ).send();
            
            // Cleanup old manifest after successful blockchain update
            try {
                pinataUtil.unpinFromIPFS(ipfsHash);
                System.out.println("Successfully cleaned up old manifest: " + ipfsHash);
            } catch (Exception e) {
                System.err.println("Warning: Failed to unpin old manifest: " + e.getMessage());
            }

            return ipfsReceipt.getTransactionHash();

        } catch (Exception e) {
            System.err.println("Failed to update announcement metadata: " + e.getMessage());
            throw new Exception("Announcement metadata update failed: " + e.getMessage());
        }
    }

    /**
     * Updates only the time range of an existing announcement
     */
    public String updateAnnouncementTimeOnly(String ipfsHash, long newStartDate, long newEndDate) throws Exception {
        try {
            // Update time in blockchain only
            TransactionReceipt receipt = managementContract.updateAnnouncementTime(
                ipfsHash,
                BigInteger.valueOf(newStartDate),
                BigInteger.valueOf(newEndDate)
            ).send();

            System.out.println("Successfully updated announcement time for IPFS hash: " + ipfsHash);
            return receipt.getTransactionHash();

        } catch (Exception e) {
            System.err.println("Failed to update announcement time: " + e.getMessage());
            throw new Exception("Announcement time update failed: " + e.getMessage());
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
     * Updates only the title of an announcement without changing file or dates
     * @param ipfsHash The IPFS hash of the announcement manifest
     * @param oldTitle The current title (for blockchain event logging)
     * @param newTitle The new title to update
     * @return Transaction hash of the blockchain operation
     * @throws Exception if update fails
     */
    public String updateAnnouncementTitleOnly(String ipfsHash, String oldTitle, String newTitle) throws Exception {
        try {
            // Get existing manifest to preserve file and dates
            String existingManifestJson = fetchManifestFromIPFS(ipfsHash);
            PinataManifest existingManifest = parseManifest(existingManifestJson);
            
            // Build updated metadata JSON manifest with new title but existing dates and file
            Map<String, Object> updatedManifest = Map.of(
                "title", newTitle,
                "startDate", existingManifest.getStartDate(),
                "endDate", existingManifest.getEndDate(),
                "fileCid", existingManifest.getFileCid()
            );

            // Pin the updated JSON manifest to get its new CID
            String manifestFileName = sanitizeFileName(newTitle) + "-manifest.json";
            String updatedMetaCid = pinataUtil.uploadJsonToIPFS(updatedManifest, manifestFileName);

            // Update IPFS hash in blockchain first
            TransactionReceipt ipfsReceipt = managementContract.updateAnnouncementIpfsHash(
                ipfsHash,
                updatedMetaCid
            ).send();

            // Update title in blockchain using the new CID - this will emit announcementTitleModified event
            TransactionReceipt titleReceipt = managementContract.updateAnnouncementTitle(
                updatedMetaCid,
                oldTitle,
                newTitle
            ).send();

            // Cleanup old manifest after successful blockchain updates
            try {
                pinataUtil.unpinFromIPFS(ipfsHash);
                System.out.println("Successfully cleaned up old manifest: " + ipfsHash);
            } catch (Exception e) {
                System.err.println("Warning: Failed to unpin old manifest: " + e.getMessage());
            }

            System.out.println("Successfully updated announcement title for IPFS hash: " + ipfsHash);
            return titleReceipt.getTransactionHash();

        } catch (Exception e) {
            System.err.println("Failed to update announcement title: " + e.getMessage());
            throw new Exception("Announcement title update failed: " + e.getMessage());
        }
    }

    /**
     * Selectively updates announcement based on what has changed
     * @param ipfsHash The IPFS hash of the announcement manifest
     * @param oldTitle The current title
     * @param newTitle The new title
     * @param newStartDate The new start date
     * @param newEndDate The new end date
     * @return Transaction hash of the blockchain operation
     * @throws Exception if update fails
     */
    public String updateAnnouncementSelectively(String ipfsHash, String oldTitle, String newTitle, 
                                          long newStartDate, long newEndDate) throws Exception {
        try {
            // Get existing manifest to compare what needs updating
            String existingManifestJson = fetchManifestFromIPFS(ipfsHash);
            PinataManifest existingManifest = parseManifest(existingManifestJson);
            
            // Determine what has changed
            boolean titleChanged = oldTitle != null && !oldTitle.equals(newTitle);
            boolean timeChanged = existingManifest.getStartDate() != newStartDate || 
                                existingManifest.getEndDate() != newEndDate;
            
            System.out.println("Selective update - Title changed: " + titleChanged + 
                              ", Time changed: " + timeChanged);
            
            if (titleChanged && timeChanged) {
                // Both title and time changed - update metadata
                return updateAnnouncementMetadata(ipfsHash, newTitle, newStartDate, newEndDate);
            } else if (titleChanged) {
                // Only title changed
                return updateAnnouncementTitleOnly(ipfsHash, oldTitle, newTitle);
            } else if (timeChanged) {
                // Only time changed
                return updateAnnouncementTimeOnly(ipfsHash, newStartDate, newEndDate);
            } else {
                // Nothing changed
                throw new Exception("No changes detected in announcement");
            }
            
        } catch (Exception e) {
            System.err.println("Failed to update announcement selectively: " + e.getMessage());
            throw new Exception("Selective announcement update failed: " + e.getMessage());
        }
    }

    // Sport Facility CRUD Operations
    public String addSportFacility(String facilityName, String facilityLocation, 
                                  BigInteger facilityStatus, List<SportFacility.court> facilityCourts) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .addSportFacility(facilityName, facilityLocation, facilityStatus, facilityCourts)
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
            Tuple3<List<String>, List<String>, List<BigInteger>> result = 
                sportFacilityContract.getAllSportFacility().send();
            
            List<String> names = result.component1();
            List<String> locations = result.component2();
            List<BigInteger> statuses = result.component3();
            
            List<SportFacilityResponse> facilities = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                SportFacilityResponse facility = new SportFacilityResponse();
                facility.setName(names.get(i));
                facility.setLocation(locations.get(i));
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
            Tuple4<String, String, BigInteger, List<SportFacility.court>> result = 
                sportFacilityContract.getSportFacility(facilityName).send();
            
            SportFacilityDetailResponse facility = new SportFacilityDetailResponse();
            facility.setName(result.component1());
            facility.setLocation(result.component2());
            facility.setStatus(getStatusString(result.component3()));
            facility.setCourts(result.component4());
            
            return facility;
        } catch (Exception e) {
            logger.error("Error getting sport facility {}: {}", facilityName, e.getMessage());
            throw new RuntimeException("Failed to get sport facility: " + e.getMessage());
        }
    }

    public String updateSportFacilityName(String oldFacilityName, String newFacilityName) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .updateSportFacilityName(oldFacilityName, newFacilityName)
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Sport facility name updated from '%s' to '%s'", 
                    oldFacilityName, newFacilityName);
            }
            return "Failed to update sport facility name";
        } catch (Exception e) {
            logger.error("Error updating sport facility name: {}", e.getMessage());
            throw new RuntimeException("Failed to update sport facility name: " + e.getMessage());
        }
    }

    public String updateSportFacilityLocation(String facilityName, String newLocation) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .updateSportFacilityLocation(facilityName, newLocation)
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Sport facility '%s' location updated to '%s'", 
                    facilityName, newLocation);
            }
            return "Failed to update sport facility location";
        } catch (Exception e) {
            logger.error("Error updating sport facility location: {}", e.getMessage());
            throw new RuntimeException("Failed to update sport facility location: " + e.getMessage());
        }
    }

    public String updateSportFacilityStatus(String facilityName, BigInteger status) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .updateSportFacilityStatus(facilityName, status)
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Sport facility '%s' status updated to '%s'", 
                    facilityName, getStatusString(status));
            }
            return "Failed to update sport facility status";
        } catch (Exception e) {
            logger.error("Error updating sport facility status: {}", e.getMessage());
            throw new RuntimeException("Failed to update sport facility status: " + e.getMessage());
        }
    }

    public String deleteSportFacility(String facilityName) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .deleteSportFacility(facilityName)
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Sport facility '%s' deleted successfully", facilityName);
            }
            return "Failed to delete sport facility";
        } catch (Exception e) {
            logger.error("Error deleting sport facility: {}", e.getMessage());
            throw new RuntimeException("Failed to delete sport facility: " + e.getMessage());
        }
    }

    public List<SportFacility.court> getAllCourts(String facilityName) {
        try {
            List<SportFacility.court> courts = sportFacilityContract.getAllCourts(facilityName).send();
            logger.info("Successfully retrieved {} courts for facility: {}", courts.size(), facilityName);
            return courts;
        } catch (Exception e) {
            logger.error("Error getting courts for facility {}: {}", facilityName, e.getMessage());
            throw new RuntimeException("Failed to get courts for facility: " + e.getMessage());
        }
    }

    public SportFacility.court getCourt(String facilityName, String courtName) {
        try {
            SportFacility.court court = sportFacilityContract.getCourt(facilityName, courtName).send();
            logger.info("Successfully retrieved court {} from facility: {}", courtName, facilityName);
            return court;
        } catch (Exception e) {
            logger.error("Error getting court {} from facility {}: {}", courtName, facilityName, e.getMessage());
            throw new RuntimeException("Failed to get court: " + e.getMessage());
        }
    }

    public Map<String, Object> getCourtAvailableTimeRange(String facilityName, String courtName) throws Exception {
        try {
            String adminAddress = rawTransactionManager.getFromAddress();
            
            // Try to call getAvailableTimeRange_ first (only works for OPEN courts)
            try {
                Tuple2<BigInteger, BigInteger> timeRange = sportFacilityContract
                    .getAvailableTimeRange_(facilityName, courtName, adminAddress)
                    .send();
                
                BigInteger earliestTime = timeRange.component1();
                BigInteger latestTime = timeRange.component2();
                
                String earliestTimeStr = secondsToTimeString(earliestTime.longValue());
                String latestTimeStr = secondsToTimeString(latestTime.longValue());
                
                logger.info("Retrieved time range for OPEN court {} in facility {}: {} - {}", 
                           courtName, facilityName, earliestTimeStr, latestTimeStr);
                
                Map<String, Object> result = new HashMap<>();
                result.put("facilityName", facilityName);
                result.put("courtName", courtName);
                result.put("earliestTime", earliestTime.longValue());
                result.put("latestTime", latestTime.longValue());
                result.put("earliestTimeStr", earliestTimeStr);
                result.put("latestTimeStr", latestTimeStr);
                result.put("status", "OPEN");
                result.put("available", true);
                
                return result;
                
            } catch (Exception e) {
                logger.warn("getAvailableTimeRange_ failed for court {} in facility {}: {}. Using getAllCourts fallback method.", 
                           courtName, facilityName, e.getMessage());
                
                // Fallback: Get all courts and find the specific court
                List<SportFacility.court> allCourts = sportFacilityContract.getAllCourts(facilityName).send();
                
                SportFacility.court targetCourt = null;
                for (Object courtObj : allCourts) {
                    try {
                        if (courtObj instanceof SportFacility.court) {
                            SportFacility.court court = (SportFacility.court) courtObj;
                            if (court.name.equals(courtName)) {
                                targetCourt = court;
                                break;
                            }
                        } else {
                            String name = extractCourtFieldString(courtObj, "name");
                            if (courtName.equals(name)) {
                                targetCourt = createCourtFromObject(courtObj);
                                break;
                            }
                        }
                    } catch (ClassCastException cce) {
                        try {
                            String name = extractCourtFieldString(courtObj, "name");
                            if (courtName.equals(name)) {
                                targetCourt = createCourtFromObject(courtObj);
                                break;
                            }
                        } catch (Exception reflectionEx) {
                            logger.warn("Failed to extract court data using reflection: {}", reflectionEx.getMessage());
                            continue;
                        }
                    }
                }
                
                if (targetCourt == null) {
                    throw new Exception("Court '" + courtName + "' not found in facility '" + facilityName + "'");
                }
                
                String earliestTimeStr = secondsToTimeString(targetCourt.earliestTime.longValue());
                String latestTimeStr = secondsToTimeString(targetCourt.latestTime.longValue());
                String courtStatus = getStatusString(targetCourt.status);
                
                logger.info("Retrieved court details via getAllCourts fallback for {} court {} in facility {}: {} - {}", 
                           courtStatus, courtName, facilityName, earliestTimeStr, latestTimeStr);
                
                Map<String, Object> result = new HashMap<>();
                result.put("facilityName", facilityName);
                result.put("courtName", targetCourt.name);
                result.put("earliestTime", targetCourt.earliestTime.longValue());
                result.put("latestTime", targetCourt.latestTime.longValue());
                result.put("earliestTimeStr", earliestTimeStr);
                result.put("latestTimeStr", latestTimeStr);
                result.put("status", courtStatus);
                result.put("available", courtStatus.equals("OPEN"));
                
                return result;
            }
            
        } catch (Exception e) {
            logger.error("Error getting court information for {} in facility {}: {}", 
                        courtName, facilityName, e.getMessage());
            throw new Exception("Failed to get court information: " + e.getMessage());
        }
    }

    /**
     * Extracts string field from court object using reflection to handle classloader issues
     */
    private String extractCourtFieldString(Object courtObj, String fieldName) throws Exception {
        try {
            Class<?> courtClass = courtObj.getClass();
            java.lang.reflect.Field field = courtClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (String) field.get(courtObj);
        } catch (Exception e) {
            throw new Exception("Failed to extract field " + fieldName + ": " + e.getMessage());
        }
    }

    /**
     * Extracts BigInteger field from court object using reflection
     */
    private BigInteger extractCourtFieldBigInteger(Object courtObj, String fieldName) throws Exception {
        try {
            Class<?> courtClass = courtObj.getClass();
            java.lang.reflect.Field field = courtClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            return (BigInteger) field.get(courtObj);
        } catch (Exception e) {
            throw new Exception("Failed to extract field " + fieldName + ": " + e.getMessage());
        }
    }

    /**
     * Creates a SportFacility.court object from a potentially different classloader object
     */
    private SportFacility.court createCourtFromObject(Object courtObj) throws Exception {
        try {
            String name = extractCourtFieldString(courtObj, "name");
            BigInteger earliestTime = extractCourtFieldBigInteger(courtObj, "earliestTime");
            BigInteger latestTime = extractCourtFieldBigInteger(courtObj, "latestTime");
            BigInteger status = extractCourtFieldBigInteger(courtObj, "status");
            
            logger.debug("Creating court object: name={}, status={}", name, status);
            
            return new SportFacility.court(name, earliestTime, latestTime, status);
        } catch (Exception e) {
            logger.error("Failed to create court from object: {}", e.getMessage());
            throw new Exception("Failed to create court from object: " + e.getMessage());
        }
    }

    private String secondsToTimeString(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
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

    public String addCourtsToFacility(String facilityName, List<SportFacility.court> courts) {
        try {
            // Validate input to prevent duplicate calls
            if (courts == null || courts.isEmpty()) {
                throw new RuntimeException("No courts provided for addition");
            }
            
            // Create request signature for deduplication
            String requestSignature = facilityName + "-" + courts.size() + "-"+ 
                courts.stream().map(c -> c.name).sorted().reduce("", String::concat);
            long currentTime = System.currentTimeMillis();
            
            // Check for recent duplicate requests (within 5 seconds)
            Long lastRequestTime = recentRequests.get(requestSignature);
            if (lastRequestTime != null && (currentTime - lastRequestTime) < 5000) {
                logger.warn("Duplicate request detected for facility '{}' with {} courts. Ignoring.", 
                           facilityName, courts.size());
                return "Duplicate request ignored";
            }
            
            // Store request timestamp
            recentRequests.put(requestSignature, currentTime);
            
            // Clean up old requests (older than 30 seconds)
            recentRequests.entrySet().removeIf(entry -> 
                (currentTime - entry.getValue()) > 30000
            );
            
            logger.info("Processing court addition for facility '{}' with {} court(s)", 
                       facilityName, courts.size());
            
            // Single transaction call
            TransactionReceipt receipt = sportFacilityContract
                .addCourt(facilityName, courts)
                .send();
            
            if (receipt.isStatusOK()) {
                logger.info("Courts added successfully to facility '{}'", facilityName);
                
                return String.format("Successfully added %d court(s) to facility '%s'", 
                    courts.size(), facilityName);
            }
            
            return "Failed to add courts to facility";
            
        } catch (Exception e) {
            logger.error("Error adding courts to facility '{}': {}", facilityName, e.getMessage());
            throw new RuntimeException("Failed to add courts: " + e.getMessage());
        }
    }

    public String deleteCourt(String facilityName, String courtName) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .deleteCourt(facilityName, courtName)
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Court '%s' deleted successfully from facility '%s'", 
                    courtName, facilityName);
            }
            return "Failed to delete court";
        } catch (Exception e) {
            logger.error("Error deleting court: {}", e.getMessage());
            throw new RuntimeException("Failed to delete court: " + e.getMessage());
        }
    }

    public String updateCourtTime(String facilityName, String courtName, 
                                 Long earliestTime, Long latestTime) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .updateCourtTime(facilityName, courtName, 
                    BigInteger.valueOf(earliestTime), BigInteger.valueOf(latestTime))
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Court '%s' time updated successfully", courtName);
            }
            return "Failed to update court time";
        } catch (Exception e) {
            logger.error("Error updating court time: {}", e.getMessage());
            throw new RuntimeException("Failed to update court time: " + e.getMessage());
        }
    }

    /**
     * Updates court status (OPEN, CLOSED, MAINTENANCE, BOOKED)
     */
    public String updateCourtStatus(String facilityName, String courtName, String status) {
        try {
            BigInteger statusValue = getStatusValueFromString(status);
            
            TransactionReceipt receipt = sportFacilityContract
                .updateCourtStatus(facilityName, courtName, statusValue)
                .send();
            
            if (receipt.isStatusOK()) {
                logger.info("Court {} status updated to {} in facility {}", 
                           courtName, status, facilityName);
                return String.format("Court '%s' status updated to %s successfully", courtName, status);
            }
            return "Failed to update court status";
        } catch (Exception e) {
            logger.error("Error updating court status: {}", e.getMessage());
            throw new RuntimeException("Failed to update court status: " + e.getMessage());
        }
    }

    /**
     * Converts string status to BigInteger for smart contract
     */
    private BigInteger getStatusValueFromString(String status) {
        switch (status.toUpperCase()) {
            case "OPEN": return BigInteger.valueOf(0);
            case "CLOSED": return BigInteger.valueOf(1);
            case "MAINTENANCE": return BigInteger.valueOf(2);
            case "BOOKED": return BigInteger.valueOf(3);
            default: throw new IllegalArgumentException("Invalid status: " + status);
        }
    }

    /**
     * Gets booked time slots for a specific court
     */
    public List<Map<String, Object>> getBookedTimeSlots(String facilityName, String courtName) {
        try {
            List<Object> bookedSlots = bookingContract.getBookedtimeSlots(facilityName, courtName).send();
            
            List<Map<String, Object>> formattedSlots = new ArrayList<>();
            
            for (Object slot : bookedSlots) {
                try {
                    Map<String, Object> timeSlot = new HashMap<>();
                    
                    if (slot instanceof Booking.timeSlot) {
                        Booking.timeSlot bookingSlot = (Booking.timeSlot) slot;
                        timeSlot.put("startTime", bookingSlot.startTime.longValue());
                        timeSlot.put("endTime", bookingSlot.endTime.longValue());
                    } else {
                        timeSlot = extractTimeSlotFromObject(slot);
                    }
                    
                    long startSeconds = (Long) timeSlot.get("startTime");
                    long endSeconds = (Long) timeSlot.get("endTime");
                    
                    timeSlot.put("startTimeStr", secondsToTimeString(startSeconds));
                    timeSlot.put("endTimeStr", secondsToTimeString(endSeconds));
                    timeSlot.put("startHour", (int) (startSeconds / 3600));
                    timeSlot.put("endHour", (int) (endSeconds / 3600));
                    
                    formattedSlots.add(timeSlot);
                    
                } catch (Exception e) {
                    logger.warn("Failed to process booked time slot: {}", e.getMessage());
                    continue;
                }
            }
            
            logger.info("Retrieved {} booked time slots for court {} in facility {}", 
                       formattedSlots.size(), courtName, facilityName);
            return formattedSlots;
            
        } catch (Exception e) {
            logger.error("Error getting booked time slots for court {} in facility {}: {}", 
                        courtName, facilityName, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Extracts time slot data from object using reflection
     */
    private Map<String, Object> extractTimeSlotFromObject(Object slotObj) throws Exception {
        try {
            Class<?> slotClass = slotObj.getClass();
            java.lang.reflect.Field startTimeField = slotClass.getDeclaredField("startTime");
            java.lang.reflect.Field endTimeField = slotClass.getDeclaredField("endTime");
            
            startTimeField.setAccessible(true);
            endTimeField.setAccessible(true);
            
            BigInteger startTime = (BigInteger) startTimeField.get(slotObj);
            BigInteger endTime = (BigInteger) endTimeField.get(slotObj);
            
            Map<String, Object> timeSlot = new HashMap<>();
            timeSlot.put("startTime", startTime.longValue());
            timeSlot.put("endTime", endTime.longValue());
            
            return timeSlot;
        } catch (Exception e) {
            throw new Exception("Failed to extract time slot data: " + e.getMessage());
        }
    }

    /**
     * Enhanced method that includes booked time slots with court time range
     */
    public Map<String, Object> getCourtAvailableTimeRangeWithBookings(String facilityName, String courtName) throws Exception {
        try {
            Map<String, Object> result = getCourtAvailableTimeRange(facilityName, courtName);
            
            List<Map<String, Object>> bookedSlots = getBookedTimeSlots(facilityName, courtName);
            result.put("bookedTimeSlots", bookedSlots);
            
            Map<String, String> timeSlotAvailability = generateTimeSlotAvailability(result, bookedSlots);
            result.put("timeSlotAvailability", timeSlotAvailability);
            
            logger.info("Retrieved complete court information with {} booked slots for court {} in facility {}", 
                       bookedSlots.size(), courtName, facilityName);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error getting court information with bookings for {} in facility {}: {}", 
                        courtName, facilityName, e.getMessage());
            throw new Exception("Failed to get court information with bookings: " + e.getMessage());
        }
    }

    /**
     * Generates hourly time slot availability based on court hours and bookings
     */
    private Map<String, String> generateTimeSlotAvailability(Map<String, Object> courtInfo, List<Map<String, Object>> bookedSlots) {
        Map<String, String> availability = new HashMap<>();
    
        try {
        long earliestTime = (Long) courtInfo.get("earliestTime");
        long latestTime = (Long) courtInfo.get("latestTime");
        String courtStatus = (String) courtInfo.get("status");
        
        int startHour = (int) (earliestTime / 3600);
        int endHour = (int) (latestTime / 3600);
        
        for (int hour = 0; hour <= 23; hour++) {
            String timeSlot = String.format("%02d:00", hour);
            
            if (hour < startHour || hour > endHour) {
                availability.put(timeSlot, "UNAVAILABLE");
                continue;
            }
            
            if (!"OPEN".equals(courtStatus)) {
                availability.put(timeSlot, courtStatus);
                continue;
            }
            
            // Create effectively final variables for lambda
            final int currentHour = hour;
            boolean isBooked = bookedSlots.stream().anyMatch(slot -> {
                Integer startHourObj = (Integer) slot.get("startHour");
                Integer endHourObj = (Integer) slot.get("endHour");
                int slotStartHour = startHourObj != null ? startHourObj : 0;
                int slotEndHour = endHourObj != null ? endHourObj : 0;
                return currentHour >= slotStartHour && currentHour < slotEndHour;
            });
            
            availability.put(timeSlot, isBooked ? "BOOKED" : "AVAILABLE");
        }
        
    } catch (Exception e) {
        logger.error("Error generating time slot availability: {}", e.getMessage());
        // Return default unavailable if error occurs
        for (int hour = 0; hour <= 23; hour++) {
            String timeSlot = String.format("%02d:00", hour);
            availability.put(timeSlot, "UNAVAILABLE");
        }
    }
    
    return availability;
    }

    /**
     * Extracts user address from User struct object
     */
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

    /**
     * Creates a booking for admin with IPFS integration
     */
    public String createBooking(String facilityName, String courtName, String userAddress,
                          long startTime, long endTime, String eventDescription,
                          MultipartFile receiptFile) throws Exception {
    try {
        // Validate inputs
        if (facilityName == null || facilityName.trim().isEmpty()) {
            throw new Exception("Facility name is required");
        }
        if (courtName == null || courtName.trim().isEmpty()) {
            throw new Exception("Court name is required");
        }
        if (userAddress == null || userAddress.trim().isEmpty()) {
            throw new Exception("User address is required");
        }
        if (startTime >= endTime) {
            throw new Exception("End time must be after start time");
        }
        if (eventDescription == null || eventDescription.trim().isEmpty()) {
            throw new Exception("Event description is required");
        }

        // Step 1: Upload receipt file to IPFS first
        String receiptFileCid = null;
        if (receiptFile != null && !receiptFile.isEmpty()) {
            receiptFileCid = pinataUtil.uploadFileToIPFS(
                receiptFile.getBytes(),
                receiptFile.getOriginalFilename()
            );
            logger.info("Receipt file uploaded to IPFS with CID: {}", receiptFileCid);
        }

        // Step 2: Create booking manifest with initial data using HashMap
        Map<String, Object> bookingManifest = new HashMap<>();
        bookingManifest.put("facilityName", facilityName);
        bookingManifest.put("courtName", courtName);
        bookingManifest.put("userAddress", userAddress);
        bookingManifest.put("startTime", startTime);
        bookingManifest.put("endTime", endTime);
        bookingManifest.put("eventDescription", eventDescription);
        bookingManifest.put("receiptFileCid", receiptFileCid != null ? receiptFileCid : "null");
        bookingManifest.put("status", "PENDING");
        bookingManifest.put("createdAt", System.currentTimeMillis());

        // Step 3: Upload manifest to IPFS
        String manifestFileName = sanitizeFileName(
            String.format("booking-%s-%s-%d", facilityName, courtName, System.currentTimeMillis())
        ) + "-manifest.json";
        String manifestCid = pinataUtil.uploadJsonToIPFS(bookingManifest, manifestFileName);
        logger.info("Booking manifest uploaded to IPFS with CID: {}", manifestCid);

        // Step 4: Create timeSlot object for blockchain call
        Booking.timeSlot timeSlot = new Booking.timeSlot(
            BigInteger.valueOf(startTime),
            BigInteger.valueOf(endTime)
        );

        // Step 5: Call blockchain function with correct parameters
        TransactionReceipt receipt = bookingContract.createBooking_(
            facilityName,
            courtName,
            userAddress,
            eventDescription,
            timeSlot
        ).send();

        if (!receipt.isStatusOK()) {
            // Cleanup IPFS files if blockchain transaction failed
            try {
                pinataUtil.unpinFromIPFS(manifestCid);
                if (receiptFileCid != null) {
                    pinataUtil.unpinFromIPFS(receiptFileCid);
                }
                logger.warn("Cleaned up IPFS files due to failed blockchain transaction");
            } catch (Exception e) {
                logger.warn("Failed to cleanup IPFS files: {}", e.getMessage());
            }
            throw new Exception("Blockchain transaction failed");
        }

        // Step 6: Update IPFS manifest with successful blockchain data using HashMap
        Map<String, Object> updatedManifest = new HashMap<>();
        updatedManifest.put("facilityName", facilityName);
        updatedManifest.put("courtName", courtName);
        updatedManifest.put("userAddress", userAddress);
        updatedManifest.put("startTime", startTime);
        updatedManifest.put("endTime", endTime);
        updatedManifest.put("eventDescription", eventDescription);
        updatedManifest.put("receiptFileCid", receiptFileCid != null ? receiptFileCid : "null");
        updatedManifest.put("status", "CONFIRMED");
        updatedManifest.put("blockchainTxHash", receipt.getTransactionHash());
        updatedManifest.put("blockNumber", receipt.getBlockNumber().toString());
        updatedManifest.put("createdAt", System.currentTimeMillis());
        updatedManifest.put("confirmedAt", System.currentTimeMillis());

        // Step 7: Upload updated manifest to IPFS
        String updatedManifestFileName = sanitizeFileName(
            String.format("booking-confirmed-%s-%s-%d", facilityName, courtName, System.currentTimeMillis())
        ) + "-manifest.json";
        String updatedManifestCid = pinataUtil.uploadJsonToIPFS(updatedManifest, updatedManifestFileName);

        // Step 8: Cleanup old manifest
        try {
            pinataUtil.unpinFromIPFS(manifestCid);
            logger.info("Cleaned up old manifest CID: {}", manifestCid);
        } catch (Exception e) {
            logger.warn("Failed to cleanup old manifest: {}", e.getMessage());
        }

        logger.info("Booking created successfully for user {} at {}/{} with updated manifest CID: {}", 
                   userAddress, facilityName, courtName, updatedManifestCid);

        return receipt.getTransactionHash();

    } catch (Exception e) {
        logger.error("Error creating booking: {}", e.getMessage());
        throw new Exception("Failed to create booking: " + e.getMessage());
    }
}

/**
 * Gets booking details with IPFS data integration
 */
public Map<String, Object> getBookingWithDetails(String manifestCid) throws Exception {
    try {
        // Fetch manifest from IPFS
        String manifestJson = pinataUtil.fetchFromIPFS(manifestCid);
        Map<String, Object> manifest = _parseJsonToMap(manifestJson);

        // Add receipt file URL if available
        String receiptFileCid = (String) manifest.get("receiptFileCid");
        if (receiptFileCid != null && !receiptFileCid.equals("null")) {
            String receiptUrl = _getFileUrl(receiptFileCid);
            manifest.put("receiptFileUrl", receiptUrl);
        }

        // Format time fields for readability
        Object startTime = manifest.get("startTime");
        Object endTime = manifest.get("endTime");
        if (startTime instanceof Number && endTime instanceof Number) {
            manifest.put("startTimeStr", secondsToTimeString(((Number) startTime).longValue()));
            manifest.put("endTimeStr", secondsToTimeString(((Number) endTime).longValue()));
        }

        logger.info("Retrieved booking details from IPFS manifest: {}", manifestCid);
        return manifest;

    } catch (Exception e) {
        logger.error("Error getting booking details from IPFS: {}", e.getMessage());
        throw new Exception("Failed to get booking details: " + e.getMessage());
    }
}

/**
 * Parses JSON string to Map for booking manifest processing
 */
private Map<String, Object> _parseJsonToMap(String jsonString) throws Exception {
    try {
        // Simple JSON parser implementation using existing PinataManifest pattern
        Map<String, Object> result = new HashMap<>();
        
        // Remove curly braces and split by commas
        String cleaned = jsonString.replaceAll("[{}]", "").trim();
        String[] pairs = cleaned.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("\"", "");
                String value = keyValue[1].trim().replaceAll("\"", "");
                
                // Try to parse numbers
                try {
                    if (value.contains(".")) {
                        result.put(key, Double.parseDouble(value));
                    } else {
                        result.put(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    result.put(key, value);
                }
            }
        }
        
        return result;
    } catch (Exception e) {
        throw new Exception("Failed to parse JSON to Map: " + e.getMessage());
    }
}

/**
 * Gets public file URL for IPFS content
 */
private String _getFileUrl(String cid) {
    // TODO: Get gateway URL from environment variable
    String pinataGateway = "gateway.pinata.cloud"; 
    return String.format("https://%s/ipfs/%s", pinataGateway, cid);
}

/**
 * Gets a specific booking by ID for admin using blockchain contract
 */
public Map<String, Object> getBookingById(Long bookingId) throws Exception {
    try {
        if (bookingId == null || bookingId < 0) {
            throw new Exception("Valid booking ID is required");
        }

        // Call blockchain function to get booking transaction using admin method
        Booking.bookingTransaction booking = bookingContract.getBooking_(BigInteger.valueOf(bookingId)).send();
        
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("bookingId", booking.bookingId.longValue());
        bookingDetails.put("owner", booking.owner);
        bookingDetails.put("facilityName", booking.facilityName);
        bookingDetails.put("courtName", booking.courtName);
        bookingDetails.put("note", booking.note);
        bookingDetails.put("status", _getBookingStatusString(booking.status));
        bookingDetails.put("startTime", booking.time.startTime.longValue());
        bookingDetails.put("endTime", booking.time.endTime.longValue());
        bookingDetails.put("startTimeStr", secondsToTimeString(booking.time.startTime.longValue()));
        bookingDetails.put("endTimeStr", secondsToTimeString(booking.time.endTime.longValue()));
        bookingDetails.put("ipfsHash", booking.ipfsHash);

        // Add user email if available
        String email = authService.getUserEmailByAddress(booking.owner);
        bookingDetails.put("userEmail", email);
        
        // Calculate duration
        long duration = booking.time.endTime.longValue() - booking.time.startTime.longValue();
        bookingDetails.put("duration", duration);
        bookingDetails.put("durationStr", _formatDuration(duration));

        // Try to fetch additional details from IPFS if hash is available
        if (booking.ipfsHash != null && !booking.ipfsHash.trim().isEmpty()) {
            try {
                Map<String, Object> ipfsDetails = getBookingWithDetails(booking.ipfsHash);
                bookingDetails.put("ipfsDetails", ipfsDetails);
                
                // Add receipt file URL if available
                String receiptUrl = (String) ipfsDetails.get("receiptFileUrl");
                if (receiptUrl != null) {
                    bookingDetails.put("receiptFileUrl", receiptUrl);
                }
            } catch (Exception e) {
                logger.warn("Failed to fetch IPFS details for booking {}: {}", bookingId, e.getMessage());
                bookingDetails.put("ipfsDetails", null);
                bookingDetails.put("ipfsError", e.getMessage());
            }
        }

        logger.info("Retrieved booking details for ID: {}", bookingId);
        return bookingDetails;

    } catch (Exception e) {
        logger.error("Error getting booking {}: {}", bookingId, e.getMessage());
        throw new Exception("Failed to get booking: " + e.getMessage());
    }
}


/**
 * Attaches a note to an existing booking
 */
public String attachBookingNote(Long bookingId, String note) throws Exception {
    try {
        if (bookingId == null || bookingId < 0) {
            throw new Exception("Valid booking ID is required");
        }
        
        if (note == null || note.trim().isEmpty()) {
            throw new Exception("Note content is required");
        }

        // Call blockchain function to attach note
        TransactionReceipt receipt = bookingContract.attachBookingNote(
            BigInteger.valueOf(bookingId), 
            note
        ).send();
        
        if (receipt.isStatusOK()) {
            logger.info("Note attached to booking {} successfully", bookingId);
            return String.format("Note has been attached to booking %d successfully", bookingId);
        }
        
        return "Failed to attach note to booking";

    } catch (Exception e) {
        logger.error("Error attaching note to booking {}: {}", bookingId, e.getMessage());
        throw new Exception("Failed to attach booking note: " + e.getMessage());
    }
}

/**
 * Updates the booking status to check for completed bookings
 */
public String updateAllBookingStatus() throws Exception {
    try {
        // Call blockchain function to update all booking statuses
        TransactionReceipt receipt = bookingContract.updateAllBookingStatus_().send();
        
        if (receipt.isStatusOK()) {
            logger.info("All booking statuses updated successfully");
            return "All booking statuses have been updated successfully";
        }
        
        return "Failed to update booking statuses";

    } catch (Exception e) {
        logger.error("Error updating all booking statuses: {}", e.getMessage());
        throw new Exception("Failed to update booking statuses: " + e.getMessage());
    }
}

/**
 * Gets all bookings from blockchain
 */
public List<Map<String, Object>> getAllBookings() throws Exception {
    try {
        // Call blockchain function to get all booking transactions
        List<Object> rawBookings = bookingContract.getAllBookings_().send();
        
        List<Map<String, Object>> bookingsList = new ArrayList<>();
        
        for (Object obj : rawBookings) {
            try {
                Map<String, Object> bookingDetails = new HashMap<>();
                
                if (obj instanceof Booking.bookingTransaction) {
                    Booking.bookingTransaction booking = (Booking.bookingTransaction) obj;
                    bookingDetails = _processBookingDirect(booking);
                } else {
                    bookingDetails = _extractBookingFromObject(obj);
                }
                
                if (bookingDetails != null && !bookingDetails.isEmpty()) {
                    bookingsList.add(bookingDetails);
                }
                
            } catch (Exception e) {
                logger.warn("Failed to process booking object: {}", e.getMessage());
                continue;
            }
        }
        
        logger.info("Retrieved {} bookings from blockchain", bookingsList.size());
        return bookingsList;
        
    } catch (Exception e) {
        if (e.getMessage().contains("Empty bookings saved in blockchain")) {
            logger.info("No bookings found in blockchain - returning empty list");
            return new ArrayList<>();
        }
        
        logger.error("Error getting all bookings: {}", e.getMessage());
        throw new Exception("Failed to get bookings: " + e.getMessage());
    }
}

/**
 * Gets bookings with optional filtering
 */
public List<Map<String, Object>> getBookingsWithFilter(String facilityName, String courtName, String status, String userAddress) throws Exception {
    try {
        List<Map<String, Object>> allBookings = getAllBookings();
        
        return allBookings.stream()
            .filter(booking -> {
                if (facilityName != null && !facilityName.equals(booking.get("facilityName"))) {
                    return false;
                }
                if (courtName != null && !courtName.equals(booking.get("courtName"))) {
                    return false;
                }
                if (status != null && !status.equals(booking.get("status"))) {
                    return false;
                }
                if (userAddress != null && !userAddress.equals(booking.get("owner"))) {
                    return false;
                }
                return true;
            })
            .collect(Collectors.toList());
            
    } catch (Exception e) {
        logger.error("Error filtering bookings: {}", e.getMessage());
        throw new Exception("Failed to filter bookings: " + e.getMessage());
    }
}

/**
 * Processes booking object directly when proper casting is possible
 */
private Map<String, Object> _processBookingDirect(Booking.bookingTransaction booking) throws Exception {
    Map<String, Object> bookingDetails = new HashMap<>();
    bookingDetails.put("bookingId", booking.bookingId.longValue());
    bookingDetails.put("owner", booking.owner);
    bookingDetails.put("facilityName", booking.facilityName);
    bookingDetails.put("courtName", booking.courtName);
    bookingDetails.put("note", booking.note);
    bookingDetails.put("status", _getBookingStatusString(booking.status));
    bookingDetails.put("startTime", booking.time.startTime.longValue());
    bookingDetails.put("endTime", booking.time.endTime.longValue());
    bookingDetails.put("startTimeStr", secondsToTimeString(booking.time.startTime.longValue()));
    bookingDetails.put("endTimeStr", secondsToTimeString(booking.time.endTime.longValue()));
    bookingDetails.put("ipfsHash", booking.ipfsHash);

    // Add user email if available
    String email = authService.getUserEmailByAddress(booking.owner);
    bookingDetails.put("userEmail", email);
    
    // Calculate duration
    long duration = booking.time.endTime.longValue() - booking.time.startTime.longValue();
    bookingDetails.put("duration", duration);
    bookingDetails.put("durationStr", _formatDuration(duration));

    return bookingDetails;
}

/**
 * Extracts booking data from object using reflection for classloader compatibility
 */
private Map<String, Object> _extractBookingFromObject(Object obj) throws Exception {
    try {
        Class<?> bookingClass = obj.getClass();
        
        java.lang.reflect.Field ownerField = bookingClass.getDeclaredField("owner");
        java.lang.reflect.Field bookingIdField = bookingClass.getDeclaredField("bookingId");
        java.lang.reflect.Field ipfsHashField = bookingClass.getDeclaredField("ipfsHash");
        java.lang.reflect.Field facilityNameField = bookingClass.getDeclaredField("facilityName");
        java.lang.reflect.Field courtNameField = bookingClass.getDeclaredField("courtName");
        java.lang.reflect.Field noteField = bookingClass.getDeclaredField("note");
        java.lang.reflect.Field timeField = bookingClass.getDeclaredField("time");
        java.lang.reflect.Field statusField = bookingClass.getDeclaredField("status");
        
        ownerField.setAccessible(true);
        bookingIdField.setAccessible(true);
        ipfsHashField.setAccessible(true);
        facilityNameField.setAccessible(true);
        courtNameField.setAccessible(true);
        noteField.setAccessible(true);
        timeField.setAccessible(true);
        statusField.setAccessible(true);
        
        String owner = (String) ownerField.get(obj);
        BigInteger bookingId = (BigInteger) bookingIdField.get(obj);
        String ipfsHash = (String) ipfsHashField.get(obj);
        String facilityName = (String) facilityNameField.get(obj);
        String courtName = (String) courtNameField.get(obj);
        String note = (String) noteField.get(obj);
        Object timeObj = timeField.get(obj);
        Object statusObj = statusField.get(obj);
        
        // Extract time data
        Class<?> timeClass = timeObj.getClass();
        java.lang.reflect.Field startTimeField = timeClass.getDeclaredField("startTime");
        java.lang.reflect.Field endTimeField = timeClass.getDeclaredField("endTime");
        
        startTimeField.setAccessible(true);
        endTimeField.setAccessible(true);
        
        BigInteger startTime = (BigInteger) startTimeField.get(timeObj);
        BigInteger endTime = (BigInteger) endTimeField.get(timeObj);
        
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("bookingId", bookingId.longValue());
        bookingDetails.put("owner", owner);
        bookingDetails.put("facilityName", facilityName);
        bookingDetails.put("courtName", courtName);
        bookingDetails.put("note", note);
        bookingDetails.put("status", _getBookingStatusFromEnum(statusObj));
        bookingDetails.put("startTime", startTime.longValue());
        bookingDetails.put("endTime", endTime.longValue());
        bookingDetails.put("startTimeStr", secondsToTimeString(startTime.longValue()));
        bookingDetails.put("endTimeStr", secondsToTimeString(endTime.longValue()));
        bookingDetails.put("ipfsHash", ipfsHash);

        // Add user email if available
        String email = authService.getUserEmailByAddress(owner);
        bookingDetails.put("userEmail", email);
        
        // Calculate duration
        long duration = endTime.longValue() - startTime.longValue();
        bookingDetails.put("duration", duration);
        bookingDetails.put("durationStr", _formatDuration(duration));
        
        return bookingDetails;
        
    } catch (Exception e) {
        logger.error("Failed to extract booking data using reflection: {}", e.getMessage());
        throw new Exception("Failed to extract booking data: " + e.getMessage());
    }
}

/**
 * Converts booking status BigInteger to string
 */
private String _getBookingStatusString(BigInteger status) {
    if (status == null) {
        return "UNKNOWN";
    }
    
    int statusValue = status.intValue();
    switch (statusValue) {
        case 0: return "APPROVED";
        case 1: return "PENDING";
        case 2: return "REJECTED";
        case 3: return "COMPLETED";
        case 4: return "CANCELLED";
        default: return "UNKNOWN";
    }
}

/**
 * Converts booking status enum object to string
 */
private String _getBookingStatusFromEnum(Object statusObj) {
    if (statusObj == null) {
        return "UNKNOWN";
    }
    
    try {
        // Get enum ordinal value
        java.lang.reflect.Method ordinalMethod = statusObj.getClass().getMethod("ordinal");
        int ordinal = (Integer) ordinalMethod.invoke(statusObj);
        
        switch (ordinal) {
            case 0: return "APPROVED";
            case 1: return "PENDING";
            case 2: return "REJECTED";
            case 3: return "COMPLETED";
            case 4: return "CANCELLED";
            default: return "UNKNOWN";
        }
    } catch (Exception e) {
        logger.warn("Failed to extract status from enum: {}", e.getMessage());
        return "UNKNOWN";
    }
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

/**
 * Rejects a booking with admin privileges
 */
public String rejectBooking(Long bookingId, String reason) throws Exception {
    try {
        if (bookingId == null || bookingId < 0) {
            throw new Exception("Valid booking ID is required");
        }
        
        if (reason == null || reason.trim().isEmpty()) {
            throw new Exception("Rejection reason is required");
        }

        // Call blockchain function to reject booking
        TransactionReceipt receipt = bookingContract.rejectBooking(BigInteger.valueOf(bookingId), reason).send();
        
        if (receipt.isStatusOK()) {
            logger.info("Booking {} rejected successfully with reason: {}", bookingId, reason);
            return String.format("Booking %d has been rejected. Reason: %s", bookingId, reason);
        }
        
        return "Failed to reject booking";

    } catch (Exception e) {
        logger.error("Error rejecting booking {}: {}", bookingId, e.getMessage());
        throw new Exception("Failed to reject booking: " + e.getMessage());
    }
}
}