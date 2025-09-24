package com.example.library.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enkel in-memory attempt/lockout-service.
 * - maxAttempts: antal tillåtna misslyckanden inom windowMs
 * - windowMs: rullande fönster för rate limiting (ms)
 * - lockMs: hur länge kontot låses efter att maxAttempts överskrids (ms)
 */
@Component
public class LoginAttemptService {

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    // - Skydd mot brute force-attacker
    private final int maxAttempts = 5;
    private final long windowMs = 60_000;      // 1 minut
    private final long lockMs = 15 * 60_000;   // 15 minuter lockout

    public void loginSucceeded(String key) {
        attempts.remove(key);
    }

    //  RÄKNA UPP misslyckade försök
    public void loginFailed(String key) {
        Attempt a = attempts.computeIfAbsent(key, k -> new Attempt());
        long now = Instant.now().toEpochMilli();

        //  RULLANDE FÖNSTER - Om försöken är gamla, börja om
        if (now - a.firstAttemptTs > windowMs) {
            a.count = 1;
            a.firstAttemptTs = now;
        } else {
            a.count++;
        }

        //  LÅS KONTO om för många försök
        if (a.count >= maxAttempts) {
            a.lockedUntilTs = now + lockMs;
        }
        attempts.put(key, a);
    }

    public boolean isBlocked(String key) {
        Attempt a = attempts.get(key);
        if (a == null) return false;
        long now = Instant.now().toEpochMilli();

        // om lock har löpt ut -> nollställ
        if (a.lockedUntilTs > 0 && now > a.lockedUntilTs) {
            attempts.remove(key);
            return false;
        }

        // om inom lock window
        if (a.lockedUntilTs > now) return true;

        // om antalet försök är över gränsen men inte låst av någon anledning
        if (a.count >= maxAttempts && (now - a.firstAttemptTs) <= windowMs) {
            return true;
        }

        // annars inte blockad
        return false;
    }

    //  få backoff info (sec left)
    public long getSecondsUntilUnlock(String key) {
        Attempt a = attempts.get(key);
        if (a == null || a.lockedUntilTs <= 0) return 0;
        long now = Instant.now().toEpochMilli();
        long left = a.lockedUntilTs - now;
        return left > 0 ? (left / 1000) : 0;
    }

    private static class Attempt {
        int count = 0;
        long firstAttemptTs = Instant.now().toEpochMilli();
        long lockedUntilTs = 0;
    }
}

