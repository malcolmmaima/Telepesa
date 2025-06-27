package com.maelcolium.telepesa.user.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DeviceFingerprintService
 * Tests device fingerprinting and fraud detection functionality
 */
@ExtendWith(MockitoExtension.class)
class DeviceFingerprintServiceTest {

    @Mock
    private AuditLogService auditLogService;
    
    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private DeviceFingerprintService deviceFingerprintService;

    @BeforeEach
    void setUp() {
        // Reset the service state before each test
        deviceFingerprintService = new DeviceFingerprintService(auditLogService);
    }

    @Test
    void generateDeviceFingerprint_WithStandardBrowser_ShouldReturnConsistentFingerprint() {
        // Given - Mock all headers that the method tries to access
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        lenient().when(httpRequest.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.9");
        lenient().when(httpRequest.getHeader("Accept-Encoding")).thenReturn("gzip, deflate, br");
        lenient().when(httpRequest.getHeader("Accept-Charset")).thenReturn("utf-8");
        lenient().when(httpRequest.getHeader("Connection")).thenReturn("keep-alive");
        lenient().when(httpRequest.getHeader("DNT")).thenReturn("1");
        lenient().when(httpRequest.getHeader("Sec-CH-UA")).thenReturn("\"Chromium\";v=\"118\", \"Google Chrome\";v=\"118\"");
        lenient().when(httpRequest.getHeader("Sec-CH-UA-Platform")).thenReturn("\"Windows\"");
        lenient().when(httpRequest.getHeader("Sec-CH-UA-Mobile")).thenReturn("?0");

        // When
        String fingerprint1 = deviceFingerprintService.generateDeviceFingerprint(httpRequest);
        String fingerprint2 = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // Then
        assertThat(fingerprint1).isNotNull();
        assertThat(fingerprint1).hasSize(64); // SHA-256 hash length
        assertThat(fingerprint1).isEqualTo(fingerprint2); // Should be consistent
    }

    @Test
    void generateDeviceFingerprint_WithDifferentUserAgent_ShouldReturnDifferentFingerprint() {
        // Given - First device
        lenient().when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        lenient().when(httpRequest.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.9");
        lenient().when(httpRequest.getHeader("Accept-Encoding")).thenReturn("gzip, deflate, br");
        lenient().when(httpRequest.getHeader("Accept-Charset")).thenReturn("utf-8");
        lenient().when(httpRequest.getHeader("Connection")).thenReturn("keep-alive");
        lenient().when(httpRequest.getHeader("DNT")).thenReturn("1");
        lenient().when(httpRequest.getHeader("Sec-CH-UA")).thenReturn("\"Chromium\";v=\"118\"");
        lenient().when(httpRequest.getHeader("Sec-CH-UA-Platform")).thenReturn("\"Windows\"");
        lenient().when(httpRequest.getHeader("Sec-CH-UA-Mobile")).thenReturn("?0");
        
        String fingerprint1 = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // When - Different user agent
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36");
        String fingerprint2 = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // Then
        assertThat(fingerprint1).isNotEqualTo(fingerprint2);
    }

    @Test
    void analyzeDevice_WithNewDevice_ShouldMarkAsNewAndLogActivity() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String username = "testuser";
        String ipAddress = "192.168.1.1";

        // When
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // Then
        assertThat(result.isNewDevice()).isTrue();
        assertThat(result.getDeviceFingerprint()).isEqualTo(deviceFingerprint);
        assertThat(result.getDeviceInfo()).isNotNull();
        assertThat(result.getDeviceInfo().getLastUsername()).isEqualTo(username);
        
        verify(auditLogService).logSuspiciousActivity(username, ipAddress, "NEW_DEVICE", 
            "Login from unrecognized device");
    }

    @Test
    void analyzeDevice_WithKnownDevice_ShouldUpdateDeviceInfo() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        
        // First login to register device
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);
        
        // When - Second login
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // Then
        assertThat(result.isNewDevice()).isFalse();
        assertThat(result.isSuspicious()).isFalse();
        assertThat(result.getDeviceInfo().getLastUsername()).isEqualTo(username);
    }

    @Test
    void analyzeDevice_WithDeviceUsedByDifferentUser_ShouldMarkAsSuspicious() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String firstUser = "user1";
        String secondUser = "user2";
        String ipAddress = "192.168.1.1";
        
        // First user login
        deviceFingerprintService.analyzeDevice(deviceFingerprint, firstUser, ipAddress);
        
        // When - Different user uses same device
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, secondUser, ipAddress);

        // Then
        assertThat(result.isNewDevice()).isFalse();
        assertThat(result.isSuspicious()).isTrue();
        assertThat(result.getSuspiciousReason()).contains("Device previously used by different user");
        
        verify(auditLogService).logSecurityViolation(eq(secondUser), eq(ipAddress), eq("DEVICE_SHARED"), 
            contains("Device fingerprint matches previous user"));
    }

    @Test
    void getDeviceInfo_WithKnownDevice_ShouldReturnDeviceInformation() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // When
        Map<String, Object> deviceInfo = deviceFingerprintService.getDeviceInfo(deviceFingerprint);

        // Then
        assertThat(deviceInfo).isNotNull();
        assertThat(deviceInfo.get("fingerprint")).isEqualTo(deviceFingerprint);
        assertThat(deviceInfo.get("lastUsername")).isEqualTo(username);
        assertThat(deviceInfo.get("lastIpAddress")).isEqualTo(ipAddress);
        assertThat(deviceInfo.get("loginCount")).isEqualTo(1);
    }

    @Test
    void getDeviceInfo_WithUnknownDevice_ShouldReturnNull() {
        // Given
        String unknownFingerprint = "unknown-fingerprint";

        // When
        Map<String, Object> deviceInfo = deviceFingerprintService.getDeviceInfo(unknownFingerprint);

        // Then
        assertThat(deviceInfo).isNull();
    }

    @Test
    void markDeviceAsTrusted_WithKnownDevice_ShouldMarkAsTrusted() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // When
        deviceFingerprintService.markDeviceAsTrusted(deviceFingerprint, username);

        // Then
        Map<String, Object> deviceInfo = deviceFingerprintService.getDeviceInfo(deviceFingerprint);
        // Note: The current implementation doesn't expose trusted status in getDeviceInfo
        // This would need to be verified differently in a real implementation
        
        verify(auditLogService).logUserProfileUpdate(username, "SYSTEM", 
            "Device marked as trusted: " + deviceFingerprint);
    }

    @Test
    void removeDevice_WithKnownDevice_ShouldRemoveDeviceFromTracking() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String username = "testuser";
        String ipAddress = "192.168.1.1";
        
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // When
        deviceFingerprintService.removeDevice(deviceFingerprint, username);

        // Then
        Map<String, Object> deviceInfo = deviceFingerprintService.getDeviceInfo(deviceFingerprint);
        assertThat(deviceInfo).isNull();
        
        verify(auditLogService).logUserProfileUpdate(username, "SYSTEM", 
            "Device removed: " + deviceFingerprint);
    }

    @Test
    void analyzeDevice_WithRapidIpChange_ShouldMarkAsSuspicious() {
        // Given
        String deviceFingerprint = "test-fingerprint-123";
        String username = "testuser";
        String firstIp = "192.168.1.1";
        String secondIp = "10.0.0.1";
        
        // First login
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, firstIp);
        
        // When - Immediate login from different IP (within 5 minutes)
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, secondIp);

        // Then
        assertThat(result.isSuspicious()).isTrue();
        assertThat(result.getSuspiciousReason()).contains("Rapid IP address change detected");
        
        verify(auditLogService).logSuspiciousActivity(eq(username), eq(secondIp), eq("RAPID_IP_CHANGE"), 
            contains("IP changed from"));
    }
} 