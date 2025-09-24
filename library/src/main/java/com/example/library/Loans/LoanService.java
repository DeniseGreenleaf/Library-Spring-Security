package com.example.library.Loans;

import com.example.library.Books.Book;
import com.example.library.Books.BookRepository;
import com.example.library.User.User;
import com.example.library.User.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public LoanService(LoanRepository loanRepository, UserRepository userRepository, BookRepository bookRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserUserId(userId);
    }

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    @Transactional
    public Loan loanBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalStateException("Book not available");
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(14));
        loan.setReturned(false);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.isReturned()) {
            throw new IllegalStateException("Book already returned");
        }

        loan.setReturned(true);
//        loan.setDueDate(LocalDate.now());

        // 🔑 Hämta boken färskt från databasen
        Book book = bookRepository.findById(loan.getBook().getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found"));

        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.saveAndFlush(book); // säkerställ att ändringen skrivs direkt

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan extendLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        if (loan.isReturned()) {
            throw new IllegalStateException("Cannot extend returned loan");
        }

        loan.setDueDate(loan.getDueDate().plusWeeks(1));
        return loanRepository.save(loan);
    }



}
