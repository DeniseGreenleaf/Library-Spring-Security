package com.example.library.User;

import Exceptions.UserNotFoundException;
import com.example.library.DTOMapper;
import com.example.library.Loans.Loan;
import com.example.library.Loans.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;
    private final LoanService loanService;

    public UserController(UserService userService, LoanService loanService) {
        this.userService = userService;
        this.loanService = loanService;
    }

    // 🔴 Hämta en användare via email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        Optional<User> userOpt = userService.getUserByEmail(email);
        return userOpt.map(user -> ResponseEntity.ok(new UserDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    // 🟢 Registrera ny användare
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserDto(createdUser));
    }


    // GET hämta användarlån med user id
    @GetMapping("/{userId}/loans")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<?> getUserLoans(@PathVariable Long userId, Authentication authentication) {
        System.out.println("DEBUG - Controller reached! UserId: " + userId);
        // Om användaren inte finns, kasta UserNotFoundException
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new UserNotFoundException("Användare med ID " + userId + " hittades inte"));

        // Extra säkerhetskontroll om ej admin, kontrollera att userId matchar inloggad användare
        if (!authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {

            // Säker casting med kontroll
            if (!(authentication.getPrincipal() instanceof User)) {
                throw new IllegalStateException("Invalid authentication principal");
            }

            // Returnera direkt - ingen exception
            User currentUser = (User) authentication.getPrincipal();
            if (!currentUser.getUserId().equals(userId)) {
                // Returnera direkt - ingen exception
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now(),
                                "status", 403,
                                "message", "Du kan bara hämta dina egna lån",
                                "path", "/api/users/" + userId + "/loans"
                        ));
            }
        }

        // Hämta lån
        List<Loan> loans = loanService.getLoansByUserId(userId);

        return ResponseEntity.ok(loans);
    }


    // 🔵 Hämta alla användare (ADMIN krävs enligt SecurityConfig)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDto> dtos = users.stream().map(UserDto::new).toList();
        return ResponseEntity.ok(dtos);
    }

    // 🟡 Hämta en användare via id
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        return userOpt.map(user -> ResponseEntity.ok(new UserDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

}