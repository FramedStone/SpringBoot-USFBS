package com.usfbs.springboot.dto;

public class SportFacilityResponse {
    private String name;
    private String location;
    private String status;
    private String imageIPFS;

    // Constructors
    public SportFacilityResponse() {}

    public SportFacilityResponse(String name, String location, String status) {
        this.name = name;
        this.location = location;
        this.status = status;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getImageIPFS() {
        return imageIPFS;
    }

    public void setImageIPFS(String imageIPFS) {
        this.imageIPFS = imageIPFS;
    }
}