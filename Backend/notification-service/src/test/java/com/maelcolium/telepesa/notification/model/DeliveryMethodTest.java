package com.maelcolium.telepesa.notification.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeliveryMethodTest {

    @Test
    void values_ShouldReturnAllDeliveryMethods() {
        // When
        DeliveryMethod[] values = DeliveryMethod.values();

        // Then
        assertThat(values).hasSize(5);
        assertThat(values).contains(
                DeliveryMethod.EMAIL,
                DeliveryMethod.SMS,
                DeliveryMethod.PUSH_NOTIFICATION,
                DeliveryMethod.IN_APP,
                DeliveryMethod.WEBHOOK
        );
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        // When & Then
        assertThat(DeliveryMethod.valueOf("EMAIL")).isEqualTo(DeliveryMethod.EMAIL);
        assertThat(DeliveryMethod.valueOf("SMS")).isEqualTo(DeliveryMethod.SMS);
        assertThat(DeliveryMethod.valueOf("PUSH_NOTIFICATION")).isEqualTo(DeliveryMethod.PUSH_NOTIFICATION);
        assertThat(DeliveryMethod.valueOf("IN_APP")).isEqualTo(DeliveryMethod.IN_APP);
        assertThat(DeliveryMethod.valueOf("WEBHOOK")).isEqualTo(DeliveryMethod.WEBHOOK);
    }
} 