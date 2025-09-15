package com.maelcolium.telepesa.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TransactionServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // If the context fails to load, this test will fail
    }

    @Test
    void mainMethodShouldRun() {
        // Test that the main method can be called without throwing exceptions
        // We don't actually run it to avoid starting the full application
        String[] args = {};
        
        // Just verify the class exists and has a main method
        try {
            TransactionServiceApplication.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Main method should exist", e);
        }
    }
}
