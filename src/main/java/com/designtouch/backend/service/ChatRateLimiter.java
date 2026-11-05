package com.designtouch.backend.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding window rate limiter for public chatbot endpoints.
 * Protects against bot spam, rapid loops, and abuse.
 */
@Component
public class ChatRateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 20;
    private static final long WINDOW_DURATION_SECONDS = 60;

    private final Map<String, Deque<Instant>> requestBuckets = new ConcurrentHashMap<>();

    public synchronized boolean isAllowed(String key) {
        if (key == null || key.isBlank()) {
            return true;
        }

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(WINDOW_DURATION_SECONDS);

        Deque<Instant> timestamps = requestBuckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public synchronized void reset() {
        requestBuckets.clear();
    }
}

