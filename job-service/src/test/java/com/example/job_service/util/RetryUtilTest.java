package com.example.job_service.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RetryUtilTest {

    @Test
    @DisplayName("Should calculate exponential retry delay in milliseconds")
    void testCalculateRetryDelay() {
        assertEquals(2000L, RetryUtil.calculateRetryDelay(1));
        assertEquals(4000L, RetryUtil.calculateRetryDelay(2));
        assertEquals(8000L, RetryUtil.calculateRetryDelay(3));
    }

    @Test
    @DisplayName("Should calculate exponential retry delay in seconds")
    void testCalculateRetryDelaySeconds() {
        assertEquals(2L, RetryUtil.calculateRetryDelaySeconds(1));
        assertEquals(4L, RetryUtil.calculateRetryDelaySeconds(2));
    }

    @Test
    @DisplayName("Should determine if retry is allowed within max retries limit")
    void testShouldRetry() {
        assertTrue(RetryUtil.shouldRetry(1, 3));
        assertTrue(RetryUtil.shouldRetry(2, 3));
        assertFalse(RetryUtil.shouldRetry(3, 3));
        assertFalse(RetryUtil.shouldRetry(4, 3));
    }
}
