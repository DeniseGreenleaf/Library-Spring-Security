package com.example.library.Books;

import Exceptions.AuthorNotFoundException;
import Exceptions.BookNotFoundException;
import Exceptions.InvalidBookDataException;
import com.example.library.BookWithDetailsDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // GET /books Lista alla böcker med paginering och sorting
    @GetMapping
    public ResponseEntity<Page<Book>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer minAvailableCopies) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Book> books = bookService.searchBooks(title, author, minAvailableCopies, pageable);
        return ResponseEntity.ok(books);
    }

    // GET /books/{id}
    // GET /books/{id} Optional return
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookByIdOrThrow(id); // kastar BookNotFoundException om ej finns
        return ResponseEntity.ok(book);
    }


    // Alternative endpoint details
    @GetMapping("/{id}/details")
    public ResponseEntity<Book> getBookByIdWithException(@PathVariable Long id) {
        try {
        Book book = bookService.getBookByIdOrThrow(id);
        return ResponseEntity.ok(book);

    } catch (BookNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().build();
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}


    // POST /books
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Book> createBook(@Valid @RequestBody Book book) {
        System.out.println("BookController.createBook() reached - user is ADMIN");
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }


    // GET /books/search
    @GetMapping("/search")
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> books = bookService.searchBooks(title, author, null, pageable);
        return ResponseEntity.ok(books);
    }


    // GET /books/search/title/{title}
    @GetMapping("/search/title/{title}")
    public ResponseEntity<Book> findBookByTitle(@PathVariable String title) {
        return bookService.findBookByTitle(title)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BookNotFoundException("Bok med titel '" + title + "' hittades inte"));
    }

    // GET /books/author/{authorId}
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Book>> getBooksByAuthor(@PathVariable Long authorId) {
        List<Book> books = bookService.getBooksByAuthorId(authorId); // kan kasta AuthorNotFoundException
        return ResponseEntity.ok(books);
    }

    // GET /books/stock
    @GetMapping("/stock")
    public ResponseEntity<List<Book>> getBooksWithMinimumCopies(
            @RequestParam(defaultValue = "1") Integer minCopies) {
        List<Book> books = bookService.getBooksWithMinimumCopies(minCopies);
        return ResponseEntity.ok(books);
    }

    // GET /books/low-stock
    @GetMapping("/low-stock")
    public ResponseEntity<List<Book>> getLowStockBooks(
            @RequestParam(defaultValue = "3") Integer threshold) {
        List<Book> books = bookService.getLowStockBooks(threshold);
        return ResponseEntity.ok(books);
    }

    // GET /books/advanced-search
    @GetMapping("/advanced-search")
    public ResponseEntity<List<Book>> advancedSearch(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer minCopies) {
        List<Book> books = bookService.searchBooksAdvanced(title, author, minCopies);
        return ResponseEntity.ok(books);
    }

    // GET /books/stats/available-count
    @GetMapping("/stats/available-count")
    public ResponseEntity<Long> getAvailableBooksCount() {
        Long count = bookService.getAvailableBooksCount();
        return ResponseEntity.ok(count);
    }

    // GET /books/most-available
    @GetMapping("/most-available")
    public ResponseEntity<List<Book>> getMostAvailableBooks() {
        List<Book> books = bookService.getMostAvailableBooks();
        return ResponseEntity.ok(books);
    }

    // GET /books/simple
    @GetMapping("/simple")
    public ResponseEntity<List<Book>> getAllBooksSimple() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // GET /books/details
    @GetMapping("/details")
    public ResponseEntity<List<BookWithDetailsDTO>> getBooksWithDetails() {
        return ResponseEntity.ok(bookService.getAllBooksWithDetails());
    }
}