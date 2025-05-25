package com.usfbs.springboot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
        regexp = "^[A-Za-z0-9._%+-]+@(student\\.)?mmu\\.edu\\.my$",
        message = "Email must be an MMU or student MMU email"
    )
    private String email;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}