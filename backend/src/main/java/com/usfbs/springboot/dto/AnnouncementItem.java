package com.usfbs.springboot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AnnouncementItem {
    
    @JsonProperty("ipfsHash")
    private String ipfsHash;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("startDate")
    private Long startDate;
    
    @JsonProperty("endDate")
    private Long endDate;
    
    @JsonProperty("fileCid")
    private String fileCid;
    
    @JsonProperty("blockchainStartDate")
    private Long blockchainStartDate;
    
    @JsonProperty("blockchainEndDate")
    private Long blockchainEndDate;

    public AnnouncementItem() {}

    public AnnouncementItem(String ipfsHash, String title, Long startDate, Long endDate, String fileCid) {
        this.ipfsHash = ipfsHash;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fileCid = fileCid;
    }

    // Getters and Setters
    public String getIpfsHash() {
        return ipfsHash;
    }

    public void setIpfsHash(String ipfsHash) {
        this.ipfsHash = ipfsHash;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getFileCid() {
        return fileCid;
    }

    public void setFileCid(String fileCid) {
        this.fileCid = fileCid;
    }

    public Long getBlockchainStartDate() {
        return blockchainStartDate;
    }

    public void setBlockchainStartDate(Long blockchainStartDate) {
        this.blockchainStartDate = blockchainStartDate;
    }

    public Long getBlockchainEndDate() {
        return blockchainEndDate;
    }

    public void setBlockchainEndDate(Long blockchainEndDate) {
        this.blockchainEndDate = blockchainEndDate;
    }

    // Helper methods for date formatting
    @JsonProperty("formattedStartDate")
    public String getFormattedStartDate() {
        if (startDate == null) return "Not set";
        return formatTimestamp(startDate);
    }

    @JsonProperty("formattedEndDate") 
    public String getFormattedEndDate() {
        if (endDate == null) return "Not set";
        return formatTimestamp(endDate);
    }

    @JsonProperty("dateRange")
    public String getDateRange() {
        return getFormattedStartDate() + " - " + getFormattedEndDate();
    }

    private String formatTimestamp(Long timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp), 
                ZoneId.systemDefault()
            );
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return "Invalid date";
        }
    }

    @Override
    public String toString() {
        return "AnnouncementItem{" +
                "ipfsHash='" + ipfsHash + '\'' +
                ", title='" + title + '\'' +
                ", dateRange='" + getDateRange() + '\'' +
                ", fileCid='" + fileCid + '\'' +
                '}';
    }
}