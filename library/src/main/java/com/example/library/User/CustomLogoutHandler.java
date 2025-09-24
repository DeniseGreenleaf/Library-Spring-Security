package com.example.library.User;

import com.example.library.security.JwtUtil;
import com.example.library.security.TokenBlacklist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    public CustomLogoutHandler(JwtUtil jwtUtil, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long expiry =jwtUtil.getExpirationEpochMs(token);

            // Lägg token i blacklist
            tokenBlacklist.add(token, expiry);
            System.out.println("⚡ Utloggad token (blacklistad): " + token);
        }
        if (authentication != null) {
            System.out.println("⚡ Utloggad användare: " + authentication.getName());
        }
    }
}

