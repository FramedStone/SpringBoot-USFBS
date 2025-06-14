package com.usfbs.springboot.dto;

import com.usfbs.springboot.contracts.SportFacility;
import java.util.List;

public class SportFacilityDetailResponse {
    private String name;
    private String location;
    private String status;
    private List<SportFacility.court> courts;
    
    // Constructors
    public SportFacilityDetailResponse() {}
    
    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<SportFacility.court> getCourts() { return courts; }
    public void setCourts(List<SportFacility.court> courts) { this.courts = courts; }
}