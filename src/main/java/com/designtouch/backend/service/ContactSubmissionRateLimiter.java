package com.designtouch.backend.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight, in-memory sliding window rate limiter and deduplication tracker
 * for public contact form submissions.
 * Prevents rapid double-clicks, automated script spamming, and duplicate email dispatches.
 */
@Component
public class ContactSubmissionRateLimiter {

    private static final int MAX_REQUESTS_PER_MINUTE = 5;
    private static final long WINDOW_DURATION_SECONDS = 60;
    private static final long DEDUPLICATION_WINDOW_SECONDS = 30;

    private final Map<String, Deque<Instant>> requestBuckets = new ConcurrentHashMap<>();
    private final Map<String, Instant> recentSubmissions = new ConcurrentHashMap<>();

    /**
     * Checks if the given key (IP address or email) is allowed under sliding-window rate limit.
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

        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    /**
     * Checks if this exact submission (email + message) was already submitted
     * within the deduplication window (30 seconds). If not, records it.
     *
     * @param deduplicationKey Unique key representing the submission content
     * @return true if this is a duplicate submission, false if it is new
     */
    public synchronized boolean isDuplicate(String deduplicationKey) {
        if (deduplicationKey == null || deduplicationKey.isBlank()) {
            return false;
        }

        Instant now = Instant.now();
        Instant cutoff = now.minusSeconds(DEDUPLICATION_WINDOW_SECONDS);

        // Evict old entries
        recentSubmissions.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));

        Instant lastTime = recentSubmissions.get(deduplicationKey);
        if (lastTime != null && lastTime.isAfter(cutoff)) {
            return true;
        }

        recentSubmissions.put(deduplicationKey, now);
        return false;
    }

    /**
     * Resets rate limiter state for testing purposes.
     */
    public void reset() {
        requestBuckets.clear();
        recentSubmissions.clear();
    }
}

