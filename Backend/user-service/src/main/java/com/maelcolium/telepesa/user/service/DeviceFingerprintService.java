package com.maelcolium.telepesa.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Device fingerprinting service for fraud detection and suspicious activity monitoring
 * Tracks device characteristics to identify returning users and detect suspicious patterns
 */
@Slf4j
@Service
public class DeviceFingerprintService {

    private final AuditLogService auditLogService;
    
    // In-memory storage for demo - in production use Redis or database
    private final ConcurrentHashMap<String, DeviceInfo> knownDevices = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> deviceLoginCounts = new ConcurrentHashMap<>();

    public DeviceFingerprintService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * Generate device fingerprint from HTTP request headers
     */
    public String generateDeviceFingerprint(HttpServletRequest request) {
        StringBuilder fingerprintData = new StringBuilder();
        
        // Collect device characteristics
        String userAgent = request.getHeader("User-Agent");
        String acceptLanguage = request.getHeader("Accept-Language");
        String acceptEncoding = request.getHeader("Accept-Encoding");
        String acceptCharset = request.getHeader("Accept-Charset");
        String connection = request.getHeader("Connection");
        String dnt = request.getHeader("DNT"); // Do Not Track
        
        // Browser-specific headers
        String secChUa = request.getHeader("Sec-CH-UA");
        String secChUaPlatform = request.getHeader("Sec-CH-UA-Platform");
        String secChUaMobile = request.getHeader("Sec-CH-UA-Mobile");
        
        fingerprintData.append(userAgent != null ? userAgent : "")
                      .append("|")
                      .append(acceptLanguage != null ? acceptLanguage : "")
                      .append("|")
                      .append(acceptEncoding != null ? acceptEncoding : "")
                      .append("|")
                      .append(acceptCharset != null ? acceptCharset : "")
                      .append("|")
                      .append(connection != null ? connection : "")
                      .append("|")
                      .append(dnt != null ? dnt : "")
                      .append("|")
                      .append(secChUa != null ? secChUa : "")
                      .append("|")
                      .append(secChUaPlatform != null ? secChUaPlatform : "")
                      .append("|")
                      .append(secChUaMobile != null ? secChUaMobile : "");
        
        return hashFingerprint(fingerprintData.toString());
    }

    /**
     * Check if device is recognized and analyze for suspicious patterns
     */
    public DeviceAnalysisResult analyzeDevice(String deviceFingerprint, String username, String ipAddress) {
        DeviceInfo deviceInfo = knownDevices.get(deviceFingerprint);
        boolean isNewDevice = deviceInfo == null;
        boolean isSuspicious = false;
        String suspiciousReason = null;
        
        if (isNewDevice) {
            // New device detected
            deviceInfo = new DeviceInfo(deviceFingerprint, ipAddress, username, LocalDateTime.now());
            knownDevices.put(deviceFingerprint, deviceInfo);
            deviceLoginCounts.put(deviceFingerprint, 1);
            
            log.info("New device detected for user: {} from IP: {}", username, ipAddress);
            auditLogService.logSuspiciousActivity(username, ipAddress, "NEW_DEVICE", 
                "Login from unrecognized device");
            
        } else {
            // Known device - update info
            String previousIpAddress = deviceInfo.getLastIpAddress(); // Store old IP before updating
            LocalDateTime previousLastSeen = deviceInfo.getLastSeen(); // Store old timestamp before updating
            
            deviceInfo.setLastSeen(LocalDateTime.now());
            deviceInfo.setLastIpAddress(ipAddress);
            int loginCount = deviceLoginCounts.getOrDefault(deviceFingerprint, 0) + 1;
            deviceLoginCounts.put(deviceFingerprint, loginCount);
            
            // Check for suspicious patterns
            if (!deviceInfo.getLastUsername().equals(username)) {
                isSuspicious = true;
                suspiciousReason = "Device previously used by different user: " + deviceInfo.getLastUsername();
                auditLogService.logSecurityViolation(username, ipAddress, "DEVICE_SHARED", 
                    "Device fingerprint matches previous user: " + deviceInfo.getLastUsername());
            }
            
            // Check for rapid location changes (basic IP change detection)
            if (!previousIpAddress.equals(ipAddress)) {
                long timeDiff = java.time.Duration.between(previousLastSeen, LocalDateTime.now()).toMinutes();
                if (timeDiff < 5) { // Suspicious if IP changes within 5 minutes
                    isSuspicious = true;
                    suspiciousReason = "Rapid IP address change detected";
                    auditLogService.logSuspiciousActivity(username, ipAddress, "RAPID_IP_CHANGE", 
                        "IP changed from " + previousIpAddress + " within " + timeDiff + " minutes");
                }
            }
            
            deviceInfo.setLastUsername(username);
        }
        
        // Check for excessive usage from this device
        int dailyUsage = getDailyUsageCount(deviceFingerprint);
        if (dailyUsage > 100) { // Threshold for suspicious activity
            isSuspicious = true;
            suspiciousReason = "Excessive daily usage: " + dailyUsage + " logins";
            auditLogService.logSuspiciousActivity(username, ipAddress, "EXCESSIVE_USAGE", 
                "Device used " + dailyUsage + " times today");
        }
        
        return new DeviceAnalysisResult(deviceFingerprint, isNewDevice, isSuspicious, suspiciousReason, deviceInfo);
    }

    /**
     * Get device information for a user
     */
    public Map<String, Object> getDeviceInfo(String deviceFingerprint) {
        DeviceInfo deviceInfo = knownDevices.get(deviceFingerprint);
        if (deviceInfo == null) {
            return null;
        }
        
        Map<String, Object> info = new HashMap<>();
        info.put("fingerprint", deviceInfo.getFingerprint());
        info.put("firstSeen", deviceInfo.getFirstSeen());
        info.put("lastSeen", deviceInfo.getLastSeen());
        info.put("lastIpAddress", deviceInfo.getLastIpAddress());
        info.put("lastUsername", deviceInfo.getLastUsername());
        info.put("loginCount", deviceLoginCounts.getOrDefault(deviceFingerprint, 0));
        
        return info;
    }

    /**
     * Mark device as trusted for a user
     */
    public void markDeviceAsTrusted(String deviceFingerprint, String username) {
        DeviceInfo deviceInfo = knownDevices.get(deviceFingerprint);
        if (deviceInfo != null) {
            deviceInfo.setTrusted(true);
            auditLogService.logUserProfileUpdate(username, "SYSTEM", "Device marked as trusted: " + deviceFingerprint);
        }
    }

    /**
     * Remove device from known devices (device logout/reset)
     */
    public void removeDevice(String deviceFingerprint, String username) {
        knownDevices.remove(deviceFingerprint);
        deviceLoginCounts.remove(deviceFingerprint);
        auditLogService.logUserProfileUpdate(username, "SYSTEM", "Device removed: " + deviceFingerprint);
    }

    private String hashFingerprint(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private int getDailyUsageCount(String deviceFingerprint) {
        // Simplified implementation - in production, track by date
        return deviceLoginCounts.getOrDefault(deviceFingerprint, 0);
    }

    /**
     * Device information storage class
     */
    public static class DeviceInfo {
        private String fingerprint;
        private String lastIpAddress;
        private String lastUsername;
        private LocalDateTime firstSeen;
        private LocalDateTime lastSeen;
        private boolean trusted;

        public DeviceInfo(String fingerprint, String ipAddress, String username, LocalDateTime timestamp) {
            this.fingerprint = fingerprint;
            this.lastIpAddress = ipAddress;
            this.lastUsername = username;
            this.firstSeen = timestamp;
            this.lastSeen = timestamp;
            this.trusted = false;
        }

        // Getters and setters
        public String getFingerprint() { return fingerprint; }
        public String getLastIpAddress() { return lastIpAddress; }
        public void setLastIpAddress(String lastIpAddress) { this.lastIpAddress = lastIpAddress; }
        public String getLastUsername() { return lastUsername; }
        public void setLastUsername(String lastUsername) { this.lastUsername = lastUsername; }
        public LocalDateTime getFirstSeen() { return firstSeen; }
        public LocalDateTime getLastSeen() { return lastSeen; }
        public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
        public boolean isTrusted() { return trusted; }
        public void setTrusted(boolean trusted) { this.trusted = trusted; }
    }

    /**
     * Device analysis result
     */
    public static class DeviceAnalysisResult {
        private String deviceFingerprint;
        private boolean isNewDevice;
        private boolean isSuspicious;
        private String suspiciousReason;
        private DeviceInfo deviceInfo;

        public DeviceAnalysisResult(String deviceFingerprint, boolean isNewDevice, boolean isSuspicious, 
                                  String suspiciousReason, DeviceInfo deviceInfo) {
            this.deviceFingerprint = deviceFingerprint;
            this.isNewDevice = isNewDevice;
            this.isSuspicious = isSuspicious;
            this.suspiciousReason = suspiciousReason;
            this.deviceInfo = deviceInfo;
        }

        // Getters
        public String getDeviceFingerprint() { return deviceFingerprint; }
        public boolean isNewDevice() { return isNewDevice; }
        public boolean isSuspicious() { return isSuspicious; }
        public String getSuspiciousReason() { return suspiciousReason; }
        public DeviceInfo getDeviceInfo() { return deviceInfo; }
    }
} 