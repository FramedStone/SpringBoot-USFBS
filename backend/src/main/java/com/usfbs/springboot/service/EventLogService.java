package com.usfbs.springboot.service;

import org.springframework.stereotype.Service;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.usfbs.springboot.util.DateTimeUtil;

@Service
public class EventLogService {
    
    private final ConcurrentLinkedQueue<EventLogEntry> eventLogs = new ConcurrentLinkedQueue<>();
    private static final int MAX_LOGS = 1000; // TODO: Configure via application.yaml
    
    public static class EventLogEntry {
        private String ipfsHash;
        private String action;
        private String fromAddress;
        private String timestamp;
        private String originalOutput; // Store the complete console output
        private LocalDateTime dateAdded;
        private String eventType;
        
        public EventLogEntry(String ipfsHash, String action, String fromAddress, String timestamp, String originalOutput, String eventType) {
            this.ipfsHash = ipfsHash != null ? ipfsHash : "";
            this.action = action;
            this.fromAddress = fromAddress;
            this.timestamp = timestamp;
            this.originalOutput = originalOutput;
            this.eventType = eventType;
            this.dateAdded = LocalDateTime.now();
        }
        
        // Getters
        public String getIpfsHash() { return ipfsHash; }
        public String getAction() { return action; }
        public String getFromAddress() { return fromAddress; }
        public String getTimestamp() { return timestamp; }
        public String getOriginalOutput() { return originalOutput; }
        public LocalDateTime getDateAdded() { return dateAdded; }
        public String getEventType() { return eventType; }
    }
    
    public void addEventLog(String ipfsHash, String action, String fromAddress, BigInteger timestamp, String originalOutput, String eventType) {
        String formattedTimestamp = DateTimeUtil.formatTimestamp(timestamp);
        addEventLog(ipfsHash, action, fromAddress, formattedTimestamp, originalOutput, eventType);
    }
    
    public void addEventLog(String ipfsHash, String action, String fromAddress, String timestamp, String originalOutput, String eventType) {
        EventLogEntry entry = new EventLogEntry(ipfsHash, action, fromAddress, timestamp, originalOutput, eventType);
        
        eventLogs.offer(entry);
        
        // Keep only the latest MAX_LOGS entries
        while (eventLogs.size() > MAX_LOGS) {
            eventLogs.poll();
        }
        
        System.out.println(">>> EventLogService: Added " + action + " from " + fromAddress);
    }
    
    public List<EventLogEntry> getAllEventLogs() {
        return new ArrayList<>(eventLogs);
    }
    
    public List<EventLogEntry> getEventLogsByAction(String action) {
        return eventLogs.stream()
                .filter(log -> log.getAction().equals(action))
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
    
    public List<EventLogEntry> getEventLogsByType(String eventType) {
        return eventLogs.stream()
                .filter(log -> log.getEventType().equals(eventType))
                .collect(ArrayList::new, (list, item) -> list.add(item), ArrayList::addAll);
    }
}