package com.sheet.cellflow.sheet.service;

public class CircuitBreaker {
    private static final int ERROR_THRESHOLD = 5;
    private static final long RESET_TIMEOUT = 60000; // 1 minute

    private int errorCount = 0;
    private long lastFailureTime = 0;
    private State state = State.CLOSED;

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    public boolean checkState() {
        if (state == State.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > RESET_TIMEOUT) {
                state = State.HALF_OPEN;
            } else {
                return false;
            }
        }
        return true;
    }

    public void recordFailure() {
        errorCount++;
        lastFailureTime = System.currentTimeMillis();
        if (errorCount >= ERROR_THRESHOLD) {
            state = State.OPEN;
        }
    }

    public void reset() {
        errorCount = 0;
        state = State.CLOSED;
    }

    public State getState() {
        return state;
    }
}
