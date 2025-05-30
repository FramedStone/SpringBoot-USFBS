package com.usfbs.springboot.dto;

public class AnnouncementResponse {
    private final String ipfsHash;
    private final long startDate;
    private final long endDate;

    public AnnouncementResponse(String ipfsHash, long startDate, long endDate) {
        this.ipfsHash = ipfsHash;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getIpfsHash() { return ipfsHash; }
    public long getStartDate()   { return startDate; }
    public long getEndDate()     { return endDate; }
}