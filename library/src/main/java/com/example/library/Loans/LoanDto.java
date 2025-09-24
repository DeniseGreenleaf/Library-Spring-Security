package com.example.library.Loans;

import com.example.library.Books.BookDto;
import com.example.library.User.UserDto;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {

    private Long loanId;
    private UserDto user;
    private BookDto book;
    private LocalDate loanDate;
    private LocalDate dueDate;
    private Boolean returned;

    public LoanDto(Loan loan) {
        this.loanId = loan.getId();
        if (loan.getUser() != null) this.user = new UserDto(loan.getUser());
        if (loan.getBook() != null) this.book = new BookDto(loan.getBook());
        this.loanDate = loan.getLoanDate();
        this.dueDate = loan.getDueDate();
        this.returned = loan.getReturned();
    }
}
