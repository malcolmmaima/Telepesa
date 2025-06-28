package com.maelcolium.telepesa.notification.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTypeTest {

    @Test
    void values_ShouldReturnAllTypes() {
        // When
        NotificationType[] values = NotificationType.values();

        // Then
        assertThat(values).hasSize(20);
        assertThat(values).contains(
                NotificationType.TRANSACTION_SUCCESS,
                NotificationType.TRANSACTION_FAILED,
                NotificationType.ACCOUNT_CREATED,
                NotificationType.ACCOUNT_UPDATED,
                NotificationType.WELCOME_MESSAGE,
                NotificationType.PASSWORD_RESET
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When & Then
        assertThat(NotificationType.valueOf("WELCOME_MESSAGE")).isEqualTo(NotificationType.WELCOME_MESSAGE);
        assertThat(NotificationType.valueOf("ACCOUNT_CREATED")).isEqualTo(NotificationType.ACCOUNT_CREATED);
        assertThat(NotificationType.valueOf("TRANSACTION_SUCCESS")).isEqualTo(NotificationType.TRANSACTION_SUCCESS);
    }
}
