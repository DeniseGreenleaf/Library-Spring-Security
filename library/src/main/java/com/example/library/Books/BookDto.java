package com.example.library.Books;

import com.example.library.Author.AuthorDto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    private Long bookId;
    private String title;
    private AuthorDto author;
    private int availableCopies;

    public BookDto(Book book) {
        this.bookId = book.getBookId();
        this.title = book.getTitle();
        if (book.getAuthor() != null) {
            this.author = new AuthorDto(book.getAuthor());
        }
        this.availableCopies = book.getAvailableCopies();
    }
}
