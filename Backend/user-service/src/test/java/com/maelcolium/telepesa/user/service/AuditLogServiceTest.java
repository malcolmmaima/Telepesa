package com.maelcolium.telepesa.user.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AuditLogService
 * Tests audit logging functionality for banking compliance
 */
class AuditLogServiceTest {

    private AuditLogService auditLogService;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        auditLogService = new AuditLogService();
        
        // Set up log capture for testing
        logger = (Logger) LoggerFactory.getLogger(AuditLogService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void logAuthenticationAttempt_WithSuccessfulLogin_ShouldLogInfoLevel() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.100";
        boolean successful = true;
        String reason = "Valid credentials";

        // When
        auditLogService.logAuthenticationAttempt(username, ipAddress, successful, reason);

        // Then
        assertThat(listAppender.list).hasSize(2); // INFO and AUDIT_EVENT logs
        
        ILoggingEvent infoEvent = listAppender.list.get(0);
        assertThat(infoEvent.getLevel().toString()).isEqualTo("INFO");
        assertThat(infoEvent.getFormattedMessage())
            .contains("AUDIT: Successful login")
            .contains(username)
            .contains(ipAddress);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getLevel().toString()).isEqualTo("INFO");
        assertThat(auditEvent.getFormattedMessage())
            .contains("AUDIT_EVENT:")
            .contains("AUTHENTICATION_ATTEMPT")
            .contains(username)
            .contains(ipAddress);
    }

    @Test
    void logAuthenticationAttempt_WithFailedLogin_ShouldLogWarnLevel() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.100";
        boolean successful = false;
        String reason = "Invalid password";

        // When
        auditLogService.logAuthenticationAttempt(username, ipAddress, successful, reason);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent warnEvent = listAppender.list.get(0);
        assertThat(warnEvent.getLevel().toString()).isEqualTo("WARN");
        assertThat(warnEvent.getFormattedMessage())
            .contains("AUDIT: Failed login attempt")
            .contains(username)
            .contains(ipAddress)
            .contains(reason);
    }

    @Test
    void logUserRegistration_ShouldLogRegistrationEvent() {
        // Given
        String username = "newuser";
        String email = "newuser@example.com";
        String ipAddress = "192.168.1.101";
        boolean successful = true;
        String reason = "Registration successful";

        // When
        auditLogService.logUserRegistration(username, email, ipAddress, successful, reason);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent infoEvent = listAppender.list.get(0);
        assertThat(infoEvent.getLevel().toString()).isEqualTo("INFO");
        assertThat(infoEvent.getFormattedMessage())
            .contains("AUDIT: User registration")
            .contains(username)
            .contains(email)
            .contains(ipAddress);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("USER_REGISTRATION")
            .contains(username)
            .contains(email);
    }

    @Test
    void logAccountLockout_ShouldLogWarnLevel() {
        // Given
        String username = "lockeduser";
        String ipAddress = "192.168.1.102";
        int failedAttempts = 5;

        // When
        auditLogService.logAccountLockout(username, ipAddress, failedAttempts);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent warnEvent = listAppender.list.get(0);
        assertThat(warnEvent.getLevel().toString()).isEqualTo("WARN");
        assertThat(warnEvent.getFormattedMessage())
            .contains("AUDIT: Account locked")
            .contains(username)
            .contains(ipAddress)
            .contains("5");

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("ACCOUNT_LOCKOUT")
            .contains(username);
    }

    @Test
    void logPasswordChange_ShouldLogPasswordChangeEvent() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.103";
        boolean successful = true;

        // When
        auditLogService.logPasswordChange(username, ipAddress, successful);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent infoEvent = listAppender.list.get(0);
        assertThat(infoEvent.getLevel().toString()).isEqualTo("INFO");
        assertThat(infoEvent.getFormattedMessage())
            .contains("AUDIT: Password change")
            .contains(username)
            .contains(ipAddress);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("PASSWORD_CHANGE")
            .contains(username);
    }

    @Test
    void logUserProfileUpdate_ShouldLogProfileUpdateEvent() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.104";
        String fieldsUpdated = "email,firstName";

        // When
        auditLogService.logUserProfileUpdate(username, ipAddress, fieldsUpdated);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent infoEvent = listAppender.list.get(0);
        assertThat(infoEvent.getLevel().toString()).isEqualTo("INFO");
        assertThat(infoEvent.getFormattedMessage())
            .contains("AUDIT: Profile update")
            .contains(username)
            .contains(fieldsUpdated);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("PROFILE_UPDATE")
            .contains(fieldsUpdated);
    }

    @Test
    void logAdministrativeAction_ShouldLogWarnLevel() {
        // Given
        String adminUsername = "admin";
        String targetUsername = "targetuser";
        String action = "ACCOUNT_SUSPENSION";
        String ipAddress = "192.168.1.105";

        // When
        auditLogService.logAdministrativeAction(adminUsername, targetUsername, action, ipAddress);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent warnEvent = listAppender.list.get(0);
        assertThat(warnEvent.getLevel().toString()).isEqualTo("WARN");
        assertThat(warnEvent.getFormattedMessage())
            .contains("AUDIT: Admin action")
            .contains(adminUsername)
            .contains(targetUsername)
            .contains(action);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("ADMINISTRATIVE_ACTION")
            .contains(adminUsername)
            .contains(targetUsername);
    }

    @Test
    void logSecurityViolation_ShouldLogErrorLevel() {
        // Given
        String username = "suspicioususer";
        String ipAddress = "192.168.1.106";
        String violationType = "SQL_INJECTION_ATTEMPT";
        String details = "Malicious SQL detected in input";

        // When
        auditLogService.logSecurityViolation(username, ipAddress, violationType, details);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent errorEvent = listAppender.list.get(0);
        assertThat(errorEvent.getLevel().toString()).isEqualTo("ERROR");
        assertThat(errorEvent.getFormattedMessage())
            .contains("AUDIT: SECURITY VIOLATION")
            .contains(username)
            .contains(violationType)
            .contains(details);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("SECURITY_VIOLATION")
            .contains("HIGH")
            .contains(violationType);
    }

    @Test
    void logSuspiciousActivity_ShouldLogWarnLevel() {
        // Given
        String username = "testuser";
        String ipAddress = "192.168.1.107";
        String activityType = "UNUSUAL_LOGIN_PATTERN";
        String description = "Login from new geographical location";

        // When
        auditLogService.logSuspiciousActivity(username, ipAddress, activityType, description);

        // Then
        assertThat(listAppender.list).hasSize(2);
        
        ILoggingEvent warnEvent = listAppender.list.get(0);
        assertThat(warnEvent.getLevel().toString()).isEqualTo("WARN");
        assertThat(warnEvent.getFormattedMessage())
            .contains("AUDIT: Suspicious activity")
            .contains(username)
            .contains(activityType)
            .contains(description);

        ILoggingEvent auditEvent = listAppender.list.get(1);
        assertThat(auditEvent.getFormattedMessage())
            .contains("SUSPICIOUS_ACTIVITY")
            .contains("MEDIUM")
            .contains(activityType);
    }
} 