package com.maelcolium.telepesa.notification.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SecurityConfigTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring context loads successfully
        // which includes loading the SecurityConfig
    }
} 