package com.maelcolium.telepesa.loan.repository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class MinimalRepositoryTest {

    @Test
    void contextLoads() {
        // This test should pass if the Spring context loads successfully
        assertTrue(true, "Context should load successfully");
    }
} 