package com.example.library.Loans;

import com.example.library.User.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }
    //  USER kan låna
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Loan> loanBook(@RequestBody @Valid Map<String, Long> request, Authentication authentication) {
        // Säker casting med kontroll
        if (!(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("Invalid authentication principal");
        }
        User user = (User) authentication.getPrincipal(); // hämtar från JWT
        Long bookId = request.get("bookId");

        Loan loan = loanService.loanBook(user.getUserId(), bookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }


    // Hämta lån för inloggad användare
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Loan>> getMyLoans(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Loan> loans = loanService.getLoansByUserId(user.getUserId());
        return ResponseEntity.ok(loans);
    }

    // ADMIN kan hämta lån för vilken user som helst
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getLoansByUserId(@PathVariable Long userId) {
        List<Loan> loans = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Loan>> getAllLoans() {
        List<Loan> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<Loan> returnBook(@PathVariable Long id) {
        Loan loan = loanService.returnBook(id);
        return ResponseEntity.ok(loan);
    }

    @PutMapping("/{id}/extend")
    public ResponseEntity<Loan> extendLoan(@PathVariable Long id) {
        Loan loan = loanService.extendLoan(id);
        return ResponseEntity.ok(loan);
    }
}

