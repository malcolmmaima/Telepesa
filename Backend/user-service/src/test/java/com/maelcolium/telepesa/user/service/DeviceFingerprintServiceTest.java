package com.maelcolium.telepesa.user.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DeviceFingerprintService
 * Tests device fingerprinting and fraud detection functionality
 */
@ExtendWith(MockitoExtension.class)
class DeviceFingerprintServiceTest {

    @Mock
    private HttpServletRequest httpRequest;

    private DeviceFingerprintService deviceFingerprintService;

    @BeforeEach
    void setUp() {
        deviceFingerprintService = new DeviceFingerprintService();
    }

    @Test
    void generateDeviceFingerprint_WithStandardBrowser_ShouldReturnConsistentFingerprint() {
        // Given
        when(httpRequest.getHeader("User-Agent"))
            .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        when(httpRequest.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.9");
        when(httpRequest.getHeader("Accept-Encoding")).thenReturn("gzip, deflate, br");
        when(httpRequest.getHeader("Accept")).thenReturn("text/html,application/xhtml+xml");
        when(httpRequest.getHeader("Connection")).thenReturn("keep-alive");
        when(httpRequest.getHeader("Upgrade-Insecure-Requests")).thenReturn("1");
        when(httpRequest.getHeader("Sec-Fetch-Dest")).thenReturn("document");
        when(httpRequest.getHeader("Sec-Fetch-Mode")).thenReturn("navigate");
        when(httpRequest.getHeader("Sec-Fetch-Site")).thenReturn("none");
        when(httpRequest.getHeader("Sec-CH-UA")).thenReturn("\"Google Chrome\";v=\"91\", \"Chromium\";v=\"91\"");

        // When
        String fingerprint1 = deviceFingerprintService.generateDeviceFingerprint(httpRequest);
        String fingerprint2 = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // Then
        assertThat(fingerprint1).isNotNull();
        assertThat(fingerprint1).hasSize(64); // SHA-256 hex string
        assertThat(fingerprint1).isEqualTo(fingerprint2); // Should be consistent
    }

    @Test
    void generateDeviceFingerprint_WithDifferentUserAgents_ShouldReturnDifferentFingerprints() {
        // Given
        when(httpRequest.getHeader("Accept-Language")).thenReturn("en-US,en;q=0.9");
        when(httpRequest.getHeader("Accept-Encoding")).thenReturn("gzip, deflate, br");

        // Chrome fingerprint
        when(httpRequest.getHeader("User-Agent"))
            .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
        String chromeFingerprint = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // Firefox fingerprint
        when(httpRequest.getHeader("User-Agent"))
            .thenReturn("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0");
        String firefoxFingerprint = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // Then
        assertThat(chromeFingerprint).isNotEqualTo(firefoxFingerprint);
    }

    @Test
    void analyzeDevice_WithNewDevice_ShouldReturnNewDeviceTrue() {
        // Given
        String deviceFingerprint = "new-device-fingerprint-123";
        String username = "testuser";
        String ipAddress = "192.168.1.100";

        // When
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDeviceFingerprint()).isEqualTo(deviceFingerprint);
        assertThat(result.isNewDevice()).isTrue();
        assertThat(result.isSuspicious()).isFalse();
        assertThat(result.getSuspiciousReason()).isNull();
    }

    @Test
    void analyzeDevice_WithKnownDevice_ShouldReturnNewDeviceFalse() {
        // Given
        String deviceFingerprint = "known-device-fingerprint-456";
        String username = "testuser";
        String ipAddress = "192.168.1.100";

        // Store device first
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // When - Analyze same device again
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // Then
        assertThat(result.isNewDevice()).isFalse();
        assertThat(result.isSuspicious()).isFalse();
    }

    @Test
    void analyzeDevice_WithDeviceUsedByMultipleUsers_ShouldDetectDeviceSharing() {
        // Given
        String sharedDeviceFingerprint = "shared-device-789";
        String user1 = "user1";
        String user2 = "user2";
        String ipAddress = "192.168.1.100";

        // When - Two different users use same device
        deviceFingerprintService.analyzeDevice(sharedDeviceFingerprint, user1, ipAddress);
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(sharedDeviceFingerprint, user2, ipAddress);

        // Then
        assertThat(result.isSuspicious()).isTrue();
        assertThat(result.getSuspiciousReason()).contains("Device sharing detected");
    }

    @Test
    void analyzeDevice_WithRapidIPChanges_ShouldDetectSuspiciousActivity() {
        // Given
        String deviceFingerprint = "mobile-device-123";
        String username = "mobileuser";

        // When - Simulate rapid IP changes (more than 3 IPs in short time)
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, "192.168.1.100");
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, "10.0.0.100");
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, "172.16.0.100");
        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, "203.0.113.100");

        // Then
        assertThat(result.isSuspicious()).isTrue();
        assertThat(result.getSuspiciousReason()).contains("Rapid IP address changes");
    }

    @Test
    void analyzeDevice_WithExcessiveDailyUsage_ShouldDetectSuspiciousActivity() {
        // Given
        String deviceFingerprint = "heavy-usage-device";
        String username = "heavyuser";
        String ipAddress = "192.168.1.100";

        // When - Simulate excessive usage (more than 50 requests in day)
        for (int i = 0; i < 55; i++) {
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);
        }

        DeviceFingerprintService.DeviceAnalysisResult result = 
            deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);

        // Then
        assertThat(result.isSuspicious()).isTrue();
        assertThat(result.getSuspiciousReason()).contains("Excessive usage detected");
    }

    @Test
    void analyzeDevice_WithNormalUsagePattern_ShouldNotBeSuspicious() {
        // Given
        String deviceFingerprint = "normal-device-456";
        String username = "normaluser";
        String ipAddress = "192.168.1.100";

        // When - Normal usage pattern
        deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);
        
        // Simulate some time passing and normal usage
        for (int i = 0; i < 10; i++) {
            DeviceFingerprintService.DeviceAnalysisResult result = 
                deviceFingerprintService.analyzeDevice(deviceFingerprint, username, ipAddress);
            
            // Should not be suspicious for normal usage
            assertThat(result.isSuspicious()).isFalse();
        }
    }

    @Test
    void analyzeDevice_WithNullParameters_ShouldHandleGracefully() {
        // When & Then - Should not throw exceptions
        DeviceFingerprintService.DeviceAnalysisResult result1 = 
            deviceFingerprintService.analyzeDevice(null, "testuser", "192.168.1.100");
        assertThat(result1).isNotNull();

        DeviceFingerprintService.DeviceAnalysisResult result2 = 
            deviceFingerprintService.analyzeDevice("device123", null, "192.168.1.100");
        assertThat(result2).isNotNull();

        DeviceFingerprintService.DeviceAnalysisResult result3 = 
            deviceFingerprintService.analyzeDevice("device123", "testuser", null);
        assertThat(result3).isNotNull();
    }

    @Test
    void generateDeviceFingerprint_WithMissingHeaders_ShouldStillGenerateFingerprint() {
        // Given - Mock request with minimal headers
        when(httpRequest.getHeader("User-Agent")).thenReturn("MinimalBrowser/1.0");
        when(httpRequest.getHeader("Accept-Language")).thenReturn(null);
        when(httpRequest.getHeader("Accept-Encoding")).thenReturn(null);

        // When
        String fingerprint = deviceFingerprintService.generateDeviceFingerprint(httpRequest);

        // Then
        assertThat(fingerprint).isNotNull();
        assertThat(fingerprint).hasSize(64); // Should still generate valid SHA-256 hash
    }

    @Test
    void deviceAnalysisResult_ShouldHaveCorrectFields() {
        // Given
        String fingerprint = "test-fingerprint";
        boolean isNewDevice = true;
        boolean isSuspicious = false;
        String suspiciousReason = "Test reason";
        String recommendation = "Allow access";

        // When
        DeviceFingerprintService.DeviceAnalysisResult result = 
            new DeviceFingerprintService.DeviceAnalysisResult(
                fingerprint, isNewDevice, isSuspicious, suspiciousReason, recommendation);

        // Then
        assertThat(result.getDeviceFingerprint()).isEqualTo(fingerprint);
        assertThat(result.isNewDevice()).isEqualTo(isNewDevice);
        assertThat(result.isSuspicious()).isEqualTo(isSuspicious);
        assertThat(result.getSuspiciousReason()).isEqualTo(suspiciousReason);
        assertThat(result.getRecommendation()).isEqualTo(recommendation);
    }
} 