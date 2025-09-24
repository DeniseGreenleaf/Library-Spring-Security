package com.example.library.security;

import com.example.library.User.User;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final String secret = "MyUltraSuperDuperSecretKeyThatIsAtLeast32Chars!!"; // byt till miljövariabel i prod
    private final long jwtExpiration = 1000 * 60 * 15; // 15 min
    private final long refreshExpiration = 1000 * 60 * 60 * 24; // 24h

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail()) //  email i subject
                .claim("roles", user.getRoleList())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody().getSubject();
    }

    public List<String> getRoles(String token) {
        return (List<String>) Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody().get("roles");
    }

    public long getExpirationEpochMs(String token) {
        return Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody().getExpiration().getTime();
    }
}
