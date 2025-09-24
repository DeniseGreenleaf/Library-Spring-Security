package com.example.library.security;

import com.example.library.User.User;
import com.example.library.User.UserRepository;
import jakarta.servlet.ServletException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, TokenBlacklist tokenBlacklist) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.tokenBlacklist = tokenBlacklist;
    }

    // - Körs före varje skyddad endpoint
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws IOException, ServletException {

        String path = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            //  VALIDERA token + kontrollera blacklist
            if (jwtUtil.validateToken(token) && !tokenBlacklist.contains(token)) {

                //  HÄMTA användare från token
                String email = jwtUtil.getUsername(token);
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    //  SKAPA Spring Security authorities (ROLE_USER, ROLE_ADMIN)
                    List<SimpleGrantedAuthority> authorities = user.getRoleList().stream()
                            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.trim()))
                            .collect(Collectors.toList());

                    // Spring Security - vem anropar och vilken roll
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    //  Sliding expiration: skicka nytt token
                    String newToken = jwtUtil.generateToken(user);
                    response.setHeader("X-Auth-Refresh", newToken);
                }
            }
        }

        filterChain.doFilter(request, response); // Fortsätt till controller
    }

}

