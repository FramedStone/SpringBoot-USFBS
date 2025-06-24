package com.usfbs.springboot.util;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for handling datetime conversions between Unix timestamps and formatted strings
 * Reference: https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html
 */
public class DateTimeUtil {
    
    // Standard 24-hour format for consistent display
    public static final DateTimeFormatter DATETIME_24H_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Format with timezone for booking times
    public static final DateTimeFormatter DATETIME_WITH_ZONE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    
    // ISO format for API responses
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Converts Unix timestamp (BigInteger) to formatted datetime string
     * @param timestamp Unix timestamp in seconds
     * @return Formatted datetime string in 24-hour format
     */
    public static String formatTimestamp(BigInteger timestamp) {
        return formatTimestamp(timestamp, DATETIME_24H_FORMATTER);
    }

    /**
     * Converts Unix timestamp (BigInteger) to formatted datetime string with custom formatter
     * @param timestamp Unix timestamp in seconds
     * @param formatter Custom DateTimeFormatter
     * @return Formatted datetime string
     */
    public static String formatTimestamp(BigInteger timestamp, DateTimeFormatter formatter) {
        try {
            if (timestamp == null) {
                return "N/A";
            }
            
            // Convert BigInteger to Instant (Unix timestamp in seconds)
            Instant instant = Instant.ofEpochSecond(timestamp.longValue());
            
            // Convert to LocalDateTime using system default timezone
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            
            return dateTime.format(formatter);
        } catch (Exception e) {
            System.err.println("Error formatting timestamp " + timestamp + ": " + e.getMessage());
            return "Invalid Date";
        }
    }

    /**
     * Converts Unix timestamp (long) to formatted datetime string
     * @param timestamp Unix timestamp in seconds
     * @return Formatted datetime string in 24-hour format
     */
    public static String formatTimestamp(long timestamp) {
        return formatTimestamp(BigInteger.valueOf(timestamp));
    }

    /**
     * Formats booking time with timezone information for Malaysian local time
     * @param timestamp Unix timestamp in seconds
     * @return Formatted datetime string with Malaysian timezone
     */
    public static String formatBookingTime(BigInteger timestamp) {
        try {
            if (timestamp == null) {
                return "N/A";
            }
            
            Instant instant = Instant.ofEpochSecond(timestamp.longValue());
            // Use proper Malaysian timezone
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Kuala_Lumpur"));
            
            return zonedDateTime.format(DATETIME_WITH_ZONE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error formatting booking time " + timestamp + ": " + e.getMessage());
            return "Invalid Date";
        }
    }

    /**
     * Formats booking time in local timezone for display
     * @param timestamp Unix timestamp in seconds
     * @return Formatted datetime string in local timezone
     */
    public static String formatBookingTimeLocal(BigInteger timestamp) {
        try {
            if (timestamp == null) {
                return "N/A";
            }
            
            Instant instant = Instant.ofEpochSecond(timestamp.longValue());
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
            
            return zonedDateTime.format(DATETIME_WITH_ZONE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Error formatting booking time " + timestamp + ": " + e.getMessage());
            return "Invalid Date";
        }
    }

    /**
     * Converts current system time to Unix timestamp
     * @return Current Unix timestamp in seconds
     */
    public static long getCurrentUnixTimestamp() {
        return Instant.now().getEpochSecond();
    }

    /**
     * Converts LocalDateTime to Unix timestamp
     * @param dateTime LocalDateTime object
     * @return Unix timestamp in seconds
     */
    public static long toUnixTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    /**
     * Validates if timestamp is within reasonable range
     * @param timestamp Unix timestamp to validate
     * @return true if timestamp is valid, false otherwise
     */
    public static boolean isValidTimestamp(BigInteger timestamp) {
        if (timestamp == null) {
            return false;
        }
        
        long timestampValue = timestamp.longValue();
        long currentTime = getCurrentUnixTimestamp();
        
        // Check if timestamp is not too far in the past (before year 2000)
        // and not too far in the future (after year 2100)
        return timestampValue >= 946684800L && timestampValue <= 4102444800L;
    }

    /**
     * Parses date string in common formats to Unix timestamp
     * @param dateString Date string in format yyyy-MM-dd HH:mm:ss or yyyy-MM-dd
     * @return Unix timestamp in seconds
     */
    public static long parseToUnixTimestamp(String dateString) {
        try {
            LocalDateTime dateTime;
            
            if (dateString.length() == 10) {
                // Date only format: yyyy-MM-dd
                dateTime = LocalDateTime.parse(dateString + "T00:00:00");
            } else if (dateString.contains("T")) {
                // ISO format: yyyy-MM-ddTHH:mm:ss
                dateTime = LocalDateTime.parse(dateString);
            } else {
                // Space-separated format: yyyy-MM-dd HH:mm:ss
                dateTime = LocalDateTime.parse(dateString, DATETIME_24H_FORMATTER);
            }
            
            return toUnixTimestamp(dateTime);
        } catch (Exception e) {
            System.err.println("Error parsing date string " + dateString + ": " + e.getMessage());
            return 0L;
        }
    }

    /**
     * Formats duration between two timestamps
     * @param startTimestamp Start time in Unix timestamp
     * @param endTimestamp End time in Unix timestamp
     * @return Human-readable duration string
     */
    public static String formatDuration(BigInteger startTimestamp, BigInteger endTimestamp) {
        try {
            if (startTimestamp == null || endTimestamp == null) {
                return "N/A";
            }
            
            long durationSeconds = endTimestamp.longValue() - startTimestamp.longValue();
            
            if (durationSeconds < 0) {
                return "Invalid duration";
            }
            
            long hours = durationSeconds / 3600;
            long minutes = (durationSeconds % 3600) / 60;
            
            if (hours > 0) {
                return String.format("%d hours %d minutes", hours, minutes);
            } else {
                return String.format("%d minutes", minutes);
            }
        } catch (Exception e) {
            System.err.println("Error formatting duration: " + e.getMessage());
            return "Invalid duration";
        }
    }

    /**
     * Formats seconds as HH:mm
     * @param seconds Total seconds
     * @return Formatted time string
     */
    public static String formatSecondsAsTime(BigInteger seconds) {
        int totalSeconds = seconds.intValue();
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        return String.format("%02d:%02d", hours, minutes);
    }
}