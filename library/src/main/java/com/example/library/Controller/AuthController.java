package com.example.library.Controller;

import com.example.library.User.User;
import com.example.library.security.*;
import com.example.library.User.UserDetailsService;
import com.example.library.User.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.boot.web.server.Ssl.ClientAuth.map;

@RestController
@RequestMapping("/api/auth")

public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    public AuthController(AuthenticationManager authManager,
                          JwtUtil jwtUtil,
                          UserDetailsService userDetailsService,
                          UserRepository userRepository,
                          LoginAttemptService loginAttemptService) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.loginAttemptService = loginAttemptService;
    }


    //  LOGIN - Här skapas tokens
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        String email = request.getEmail().toLowerCase();

        //  BRUTE FORCE-SKYDD - Blockera om kontot är låst
        if (loginAttemptService.isBlocked(email)) {
            return ResponseEntity.status(429)
                    .body("För många misslyckade försök, försök igen om " +
                            loginAttemptService.getSecondsUntilUnlock(request.getEmail()) + " sekunder.");
        }
        try {
            //  AUTENTISERA - Spring Security kollar email + lösenord mot databas
            Authentication authentication = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            //  Login lyckades -> nollställ räknaren
            loginAttemptService.loginSucceeded(email);

            //  SKAPA TOKENS - Access token (15 min) + Refresh token (24h)
            String token = jwtUtil.generateToken(user);     // Innehåller email + roller
            String refreshToken = jwtUtil.generateRefreshToken(user);

            return ResponseEntity.ok(new AuthResponse(token, refreshToken));

        } catch (Exception e) {

            //  LOGIN MISSLYCKADES - Räkna upp försök för rate limiting
            loginAttemptService.loginFailed(request.getEmail());
            return ResponseEntity.status(401).body("Ogiltiga inloggningsuppgifter");
        }
    }


    //  REFRESH - Förnya access token med refresh token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {
        //  VALIDERA refresh token
        if (!jwtUtil.validateToken(request.getRefreshToken())) {
            return ResponseEntity.status(401).build();
        }

        //  HÄMTA användare från token (email som subject)
        String email = jwtUtil.getUsername(request.getRefreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //  SKAPA NY access token (behåll samma refresh token
        String token = jwtUtil.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(token, request.getRefreshToken()));
    }

}