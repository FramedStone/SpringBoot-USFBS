package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.contracts.SportFacility;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminService {
    
    private final Map<String, Long> recentRequests = new ConcurrentHashMap<>();
    
    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    
    private final PinataUtil pinataUtil;
    private final Management managementContract;
    
    @Autowired
    private SportFacility sportFacilityContract;

    @Autowired
    public AdminService(PinataUtil pinataUtil, Management managementContract) {
        this.pinataUtil = pinataUtil;
        this.managementContract = managementContract;
    }

    @Autowired
    private RawTransactionManager rawTransactionManager;

    public void addUser(String userAddress) throws Exception {
        managementContract.addUser(userAddress).send();
    }

    public void banUser(String userAddress) throws Exception {
        managementContract.banUser(userAddress).send();
    }

    public void unbanUser(String userAddress) throws Exception {
        managementContract.unbanUser(userAddress).send();
    }

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

    public String fetchManifestFromIPFS(String ipfsHash) throws Exception {
        return pinataUtil.fetchFromIPFS(ipfsHash);
    }

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

    public String updateCourtStatus(String facilityName, String courtName, BigInteger status) {
        try {
            TransactionReceipt receipt = sportFacilityContract
                .updateCourtStatus(facilityName, courtName, status)
                .send();
            
            if (receipt.isStatusOK()) {
                return String.format("Court '%s' status updated successfully", courtName);
            }
            return "Failed to update court status";
        } catch (Exception e) {
            logger.error("Error updating court status: {}", e.getMessage());
            throw new RuntimeException("Failed to update court status: " + e.getMessage());
        }
    }
}
