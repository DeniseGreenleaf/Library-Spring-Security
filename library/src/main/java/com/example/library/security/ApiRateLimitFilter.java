package com.example.library.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {


    private final Map<String, RequestInfo> requests = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 20; // max per minute
    private final long WINDOW_MS = 60_000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        long now = Instant.now().toEpochMilli();

        RequestInfo info = requests.computeIfAbsent(key, k -> new RequestInfo());
        if (now - info.firstRequestTs > WINDOW_MS) {
            info.count = 1;
            info.firstRequestTs = now;
        } else {
            info.count++;
        }

        if (info.count > MAX_REQUESTS) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestInfo {
        int count = 0;
        long firstRequestTs = Instant.now().toEpochMilli();
    }
}
