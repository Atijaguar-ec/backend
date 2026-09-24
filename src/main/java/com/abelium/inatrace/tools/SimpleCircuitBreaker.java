package com.abelium.inatrace.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * HU-10: Lightweight thread-safe Circuit Breaker for resilient external HTTP integrations.
 * Protects application threads from external API latency and repeated timeouts.
 */
public class SimpleCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(SimpleCircuitBreaker.class);

    public enum State {
        CLOSED,
        OPEN,
        HALF_OPEN
    }

    private final String name;
    private final int failureThreshold;
    private final long resetTimeoutMillis;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicLong lastStateChangedTime = new AtomicLong(System.currentTimeMillis());

    public SimpleCircuitBreaker(String name, int failureThreshold, long resetTimeoutMillis) {
        this.name = name;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutMillis = resetTimeoutMillis;
    }

    public SimpleCircuitBreaker(String name) {
        this(name, 3, 60_000); // 3 consecutive failures -> open for 60s
    }

    public synchronized boolean allowExecution() {
        State current = state.get();
        if (current == State.CLOSED) {
            return true;
        }

        long elapsed = System.currentTimeMillis() - lastStateChangedTime.get();
        if (current == State.OPEN) {
            if (elapsed >= resetTimeoutMillis) {
                state.set(State.HALF_OPEN);
                lastStateChangedTime.set(System.currentTimeMillis());
                log.info("CircuitBreaker '{}': OPEN -> HALF_OPEN (probing external service)", name);
                return true;
            }
            log.warn("CircuitBreaker '{}': Call rejected in OPEN state ({}ms remaining before probe)", name, resetTimeoutMillis - elapsed);
            return false;
        }

        // HALF_OPEN allows probe
        return true;
    }

    public synchronized void recordSuccess() {
        State previous = state.getAndSet(State.CLOSED);
        failureCount.set(0);
        lastStateChangedTime.set(System.currentTimeMillis());
        if (previous != State.CLOSED) {
            log.info("CircuitBreaker '{}': Reset to CLOSED state after successful response", name);
        }
    }

    public synchronized void recordFailure(Throwable t) {
        int failures = failureCount.incrementAndGet();
        State current = state.get();
        if (current == State.HALF_OPEN || failures >= failureThreshold) {
            state.set(State.OPEN);
            lastStateChangedTime.set(System.currentTimeMillis());
            log.error("CircuitBreaker '{}': Tripped to OPEN state after {} failure(s). Cause: {}",
                    name, failures, t != null ? t.getMessage() : "unknown");
        } else {
            log.warn("CircuitBreaker '{}': Recorded failure #{}/{} (Cause: {})",
                    name, failures, failureThreshold, t != null ? t.getMessage() : "unknown");
        }
    }

    public <T> T execute(Supplier<T> action, Supplier<T> fallback) {
        if (!allowExecution()) {
            return fallback != null ? fallback.get() : null;
        }
        try {
            T result = action.get();
            recordSuccess();
            return result;
        } catch (Throwable t) {
            recordFailure(t);
            return fallback != null ? fallback.get() : null;
        }
    }

    public State getState() {
        if (state.get() == State.OPEN && (System.currentTimeMillis() - lastStateChangedTime.get() >= resetTimeoutMillis)) {
            state.set(State.HALF_OPEN);
        }
        return state.get();
    }

    public String getName() {
        return name;
    }

    public void reset() {
        state.set(State.CLOSED);
        failureCount.set(0);
        lastStateChangedTime.set(System.currentTimeMillis());
    }
}
