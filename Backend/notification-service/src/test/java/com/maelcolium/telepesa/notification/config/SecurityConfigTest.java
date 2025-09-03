package com.maelcolium.telepesa.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {com.maelcolium.telepesa.notification.NotificationServiceApplication.class}
)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "spring.main.web-application-type=servlet",
    "spring.security.jwt.secret=test-secret",
    "logging.level.org.springframework.security=WARN",
    "logging.level.org.springframework=WARN",
    "logging.level.org.hibernate=WARN",
    "management.endpoints.web.exposure.include=health",
    "server.port=0"
})
class SecurityConfigTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // which includes loading and configuring the SecurityConfig properly
    }
} 