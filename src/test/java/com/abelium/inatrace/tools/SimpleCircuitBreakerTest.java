package com.abelium.inatrace.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SimpleCircuitBreakerTest {

    private SimpleCircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        // Breaker with 3 failure threshold and 100ms reset timeout for fast testing
        circuitBreaker = new SimpleCircuitBreaker("TestBreaker", 3, 100);
    }

    @Test
    void testInitialStateIsClosed() {
        assertEquals(SimpleCircuitBreaker.State.CLOSED, circuitBreaker.getState());
        assertTrue(circuitBreaker.allowExecution());
    }

    @Test
    void testSuccessfulExecutionReturnsValue() {
        String result = circuitBreaker.execute(() -> "success", () -> "fallback");
        assertEquals("success", result);
        assertEquals(SimpleCircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }

    @Test
    void testFailuresTripToOpenState() {
        AtomicInteger fallbackCalls = new AtomicInteger(0);

        for (int i = 0; i < 3; i++) {
            final int attempt = i;
            String res = circuitBreaker.execute(() -> {
                throw new RuntimeException("API error " + attempt);
            }, () -> {
                fallbackCalls.incrementAndGet();
                return "fallback";
            });
            assertEquals("fallback", res);
        }

        assertEquals(3, fallbackCalls.get());
        assertEquals(SimpleCircuitBreaker.State.OPEN, circuitBreaker.getState());
        assertFalse(circuitBreaker.allowExecution());
    }

    @Test
    void testOpenStateFastFailsWithoutCallingAction() {
        // Force 3 failures
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("timeout");
            }, () -> "fallback");
        }
        assertEquals(SimpleCircuitBreaker.State.OPEN, circuitBreaker.getState());

        AtomicBoolean actionCalled = new AtomicBoolean(false);
        String result = circuitBreaker.execute(() -> {
            actionCalled.set(true);
            return "should not be called";
        }, () -> "fast_fallback");

        assertFalse(actionCalled.get(), "Action should NOT be executed when circuit is OPEN");
        assertEquals("fast_fallback", result);
    }

    @Test
    void testResetAfterCoolOffPeriodTransitionsThroughHalfOpenToClosed() throws InterruptedException {
        // Force 3 failures to trip to OPEN
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("error");
            }, () -> "fallback");
        }
        assertEquals(SimpleCircuitBreaker.State.OPEN, circuitBreaker.getState());

        // Wait past reset timeout (100ms)
        Thread.sleep(150);

        // Next execution should probe (HALF_OPEN) and on success return to CLOSED
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        String result = circuitBreaker.execute(() -> {
            actionCalled.set(true);
            return "recovered";
        }, () -> "fallback");

        assertTrue(actionCalled.get());
        assertEquals("recovered", result);
        assertEquals(SimpleCircuitBreaker.State.CLOSED, circuitBreaker.getState());
    }
}
