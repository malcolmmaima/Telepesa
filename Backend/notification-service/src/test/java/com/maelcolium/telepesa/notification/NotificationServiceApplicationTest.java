package com.maelcolium.telepesa.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class NotificationServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring Boot application context loads successfully
        // and all beans are properly configured
    }
} 