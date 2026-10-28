package com.designtouch.backend.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight, in-memory sliding window rate limiter for newsletter subscription requests.
 * Prevents automated script spamming and brute-force abuse.
 */
@Component
public class NewsletterRateLimiter {

    private static final int MAX_REQUESTS_PER_WINDOW = 5;
    private static final long WINDOW_DURATION_SECONDS = 60;

    private final Map<String, Deque<Instant>> requestBuckets = new ConcurrentHashMap<>();

    /**
     * Checks if the given key (IP address or email) is allowed to perform a subscription request.
     *
     * @param key Unique identifier (client IP or normalized email)
     * @return true if request is permitted, false if rate limit exceeded.
     */
    public synchronized boolean isAllowed(String key) {
        if (key == null || key.isBlank()) {
            return true;
        }

        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(WINDOW_DURATION_SECONDS);

        Deque<Instant> timestamps = requestBuckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        // Evict expired timestamps
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    /**
     * Resets limits for testing purposes.
     */
    public void reset() {
        requestBuckets.clear();
    }
}

