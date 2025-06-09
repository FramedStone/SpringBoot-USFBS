package com.usfbs.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.usfbs.springboot.service.EventLogService;
import java.util.List;

@RestController
@RequestMapping("/logs")
@CrossOrigin(
    origins = "${cors.allowed-origins}",
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS }
)
public class LogsController {
    
    @Autowired
    private EventLogService eventLogService;
    
    @GetMapping
    @PreAuthorize("hasRole('Moderator') or hasRole('Admin')")
    public ResponseEntity<List<EventLogService.EventLogEntry>> getAllEventLogs() {
        try {
            List<EventLogService.EventLogEntry> logs = eventLogService.getAllEventLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            System.err.println("Error fetching event logs: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/action/{action}")
    @PreAuthorize("hasRole('Moderator') or hasRole('Admin')")
    public ResponseEntity<List<EventLogService.EventLogEntry>> getEventLogsByAction(@PathVariable String action) {
        try {
            List<EventLogService.EventLogEntry> logs = eventLogService.getEventLogsByAction(action);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            System.err.println("Error fetching event logs by action: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/type/{type}")
    @PreAuthorize("hasRole('Moderator') or hasRole('Admin')")
    public ResponseEntity<List<EventLogService.EventLogEntry>> getEventLogsByType(@PathVariable String type) {
        try {
            List<EventLogService.EventLogEntry> logs = eventLogService.getEventLogsByType(type.toUpperCase());
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            System.err.println("Error fetching event logs by type: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}