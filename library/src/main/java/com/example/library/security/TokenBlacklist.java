package com.example.library.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {
    // token -> expiryEpochMs
    private final Map<String, Long> blacklist = new ConcurrentHashMap<>();

    public void add(String token, long expiryEpochMs) {
        blacklist.put(token, expiryEpochMs);
    }

    //  - Förhindrar återanvändning av utloggade tokens
    public boolean contains(String token) {
        Long exp = blacklist.get(token);
        if (exp == null) return false;

        //  AUTO-CLEANUP - Ta bort utgångna tokens
        if (System.currentTimeMillis() > exp) {
            blacklist.remove(token);
            return false;
        }
        return true; // Token är blacklistad och fortfarande giltig
    }

    // cleanup
    public void purgeExpired() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> e.getValue() <= now);
    }
}
