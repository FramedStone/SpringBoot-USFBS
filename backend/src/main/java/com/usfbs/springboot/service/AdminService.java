package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.dto.AnnouncementItem;
import com.usfbs.springboot.dto.PinataManifest;
import com.usfbs.springboot.util.PinataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.tx.RawTransactionManager;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final PinataUtil pinataUtil;
    private final Management managementContract;

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
            // Get existing manifest to preserve file CID
            String existingManifestJson = fetchManifestFromIPFS(ipfsHash);
            PinataManifest existingManifest = parseManifest(existingManifestJson);
            
            // Build new metadata JSON manifest with existing file CID
            Map<String, Object> updatedManifest = Map.of(
                "title", newTitle,
                "startDate", newStartDate,
                "endDate", newEndDate,
                "fileCid", existingManifest.getFileCid() // Keep existing file
            );

            // Pin the updated JSON manifest to get its new CID with new title
            String manifestFileName = sanitizeFileName(newTitle) + "-manifest.json";
            String updatedMetaCid = pinataUtil.uploadJsonToIPFS(updatedManifest, manifestFileName);

            // Update IPFS hash in blockchain
            TransactionReceipt ipfsReceipt = managementContract.updateAnnouncementIpfsHash(
                ipfsHash,
                updatedMetaCid
            ).send();

            // Update time in blockchain using the new manifest CID
            TransactionReceipt timeReceipt = managementContract.updateAnnouncementTime(
                updatedMetaCid,
                BigInteger.valueOf(newStartDate),
                BigInteger.valueOf(newEndDate)
            ).send();

            // Cleanup old manifest after successful blockchain updates
            try {
                pinataUtil.unpinFromIPFS(ipfsHash); // Unpin old manifest only
                System.out.println("Successfully cleaned up old manifest: " + ipfsHash);
            } catch (Exception e) {
                System.err.println("Warning: Failed to unpin old manifest: " + e.getMessage());
                // Continue execution as blockchain update was successful
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
}
