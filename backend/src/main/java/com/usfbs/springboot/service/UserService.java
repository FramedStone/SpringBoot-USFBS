package com.usfbs.springboot.service;

import com.usfbs.springboot.contracts.Management;
import com.usfbs.springboot.dto.AnnouncementItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private Management managementContract;
    
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
}

