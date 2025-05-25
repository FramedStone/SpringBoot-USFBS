package com.usfbs.springboot.dto;

public class LoginRequest {
    private String email;
    private String userAddress;

    // getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUserAddress() { return userAddress; }
    public void setUserAddress(String userAddress) { this.userAddress = userAddress; }
}