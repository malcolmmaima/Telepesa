package com.maelcolium.telepesa.notification.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationStatusTest {

    @Test
    void values_ShouldReturnAllStatuses() {
        // When
        NotificationStatus[] values = NotificationStatus.values();

        // Then
        assertThat(values).hasSize(7);
        assertThat(values).contains(
                NotificationStatus.PENDING,
                NotificationStatus.SENT,
                NotificationStatus.DELIVERED,
                NotificationStatus.READ,
                NotificationStatus.FAILED,
                NotificationStatus.CANCELLED,
                NotificationStatus.RETRYING
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When & Then
        assertThat(NotificationStatus.valueOf("PENDING")).isEqualTo(NotificationStatus.PENDING);
        assertThat(NotificationStatus.valueOf("SENT")).isEqualTo(NotificationStatus.SENT);
        assertThat(NotificationStatus.valueOf("READ")).isEqualTo(NotificationStatus.READ);
        assertThat(NotificationStatus.valueOf("FAILED")).isEqualTo(NotificationStatus.FAILED);
    }
}
