package com.usfbs.springboot.dto;

import com.usfbs.springboot.contracts.SportFacility;
import java.math.BigInteger;
import java.util.List;

public class SportFacilityRequest {
    private String facilityName;
    private String facilityLocation;
    private BigInteger facilityStatus;
    private List<CourtRequest> courts;
    
    // Constructors
    public SportFacilityRequest() {}
    
    // Getters and Setters
    public String getFacilityName() { return facilityName; }
    public void setFacilityName(String facilityName) { this.facilityName = facilityName; }
    
    public String getFacilityLocation() { return facilityLocation; }
    public void setFacilityLocation(String facilityLocation) { this.facilityLocation = facilityLocation; }
    
    public BigInteger getFacilityStatus() { return facilityStatus; }
    public void setFacilityStatus(BigInteger facilityStatus) { this.facilityStatus = facilityStatus; }
    
    public List<CourtRequest> getCourts() { return courts; }
    public void setCourts(List<CourtRequest> courts) { this.courts = courts; }
    
    // Inner class for court data
    public static class CourtRequest {
        private String name;
        private BigInteger earliestTime;
        private BigInteger latestTime;
        private BigInteger status;
        
        // Constructors
        public CourtRequest() {}
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigInteger getEarliestTime() { return earliestTime; }
        public void setEarliestTime(BigInteger earliestTime) { this.earliestTime = earliestTime; }
        
        public BigInteger getLatestTime() { return latestTime; }
        public void setLatestTime(BigInteger latestTime) { this.latestTime = latestTime; }
        
        public BigInteger getStatus() { return status; }
        public void setStatus(BigInteger status) { this.status = status; }
    }
}