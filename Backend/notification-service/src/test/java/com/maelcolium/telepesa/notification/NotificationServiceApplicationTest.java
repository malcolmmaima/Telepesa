package com.maelcolium.telepesa.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test ensures that the Spring Boot application context loads successfully
        // and all beans are properly configured
    }

    @Test
    void main() {
        // Test the main method - this will be covered when the context loads
        // but we can test it explicitly
        NotificationServiceApplication.main(new String[]{});
    }
} 