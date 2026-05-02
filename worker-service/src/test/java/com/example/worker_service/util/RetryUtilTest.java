package com.example.worker_service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryUtilTest {

    @Test
    @DisplayName("Should cap maximum retry delay at 5 minutes")
    void testCalculateRetryDelayMaxCap() {
        long delay = RetryUtil.calculateRetryDelay(20);
        assertEquals(300000L, delay);
    }

    @Test
    @DisplayName("Should return valid delay for initial retries")
    void testCalculateRetryDelayNormal() {
        assertEquals(2000L, RetryUtil.calculateRetryDelay(1));
        assertEquals(4000L, RetryUtil.calculateRetryDelay(2));
    }

    @Test
    @DisplayName("Should respect max retries boundary")
    void testShouldRetry() {
        assertTrue(RetryUtil.shouldRetry(0, 5));
        assertFalse(RetryUtil.shouldRetry(5, 5));
    }
}
