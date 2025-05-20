package com.usfbs.springboot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class AdminController {
    @GetMapping("/")
    public String login() {
        return "admin/html/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/html/dashboard";
    }

    @GetMapping("/user-management")
    public String userManagement() {
        return "admin/html/user_management";
    }

    @GetMapping("/court-slot-management")
    public String courtSlotManagement() {
        return "admin/html/court_slot_management";
    }

    @GetMapping("/booking-management")
    public String bookingManagement() {
        return "admin/html/booking_management";
    }

    @GetMapping("/system-logs")
    public String systemLogs() {
        return "admin/html/system_logs";
    }

    @GetMapping("/admin/add-blacklist")
    public String addBlacklist() {
        return "admin/html/add_blacklist";
    }

    @GetMapping("/admin/add-announcement")
    public String addAnnouncement() {
        return "admin/html/add_announcement";
    }
}

