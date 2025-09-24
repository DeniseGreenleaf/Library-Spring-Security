package com.example.library.Books;

import Exceptions.AuthorNotFoundException;
import Exceptions.BookNotFoundException;
import Exceptions.InvalidBookDataException;
import com.example.library.Author.Author;
import com.example.library.Author.AuthorRepository;
import com.example.library.BookWithDetailsDTO;
import com.example.library.DTOMapper;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }



    // För GET /books  Lista alla böcker utan paginering
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // För GET /books Lista alla böcker med paginering
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    // metod med Optional
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findBookById(id);
    }


    // Metod med custom exception
    public Book getBookByIdOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }


    // POST /books Skapa ny bok
    @PreAuthorize("hasRole('ADMIN')")
    public Book createBook(Book book) {
        validateBook(book);

        Long authorId = book.getAuthor().getAuthorId();

        // Sanitize book title (kontrollera null först)
        if (book.getTitle() != null) {
            book.setTitle(Jsoup.clean(book.getTitle(), Safelist.basic()));
        }

        // Hämta befintlig författare från databas
        Author existingAuthor = authorRepository.findById(authorId)
                .orElseThrow(() -> new AuthorNotFoundException(authorId));

        // Använd den befintliga författaren istället för att sanitiza den från request
        book.setAuthor(existingAuthor);

        return bookRepository.save(book);
    }


    // Sök metoder

    // För GET /books/search Sök böcker på title eller author utan paginering
    public List<Book> searchBooksByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookRepository.findByTitleContainingIgnoreCase(title.trim());
    }

    public List<Book> searchBooksByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            return getAllBooks();
        }
        return bookRepository.findByAuthorNameContainingIgnoreCase(author.trim());
    }

    // För GET /books/search Sök böcker med paginering
    public Page<Book> searchBooksByTitle(String title, Pageable pageable) {
        if (title == null || title.trim().isEmpty()) {
            return getAllBooks(pageable);
        }
        return bookRepository.findByTitleContainingIgnoreCase(title.trim(), pageable);
    }

    public Page<Book> searchBooksByAuthor(String author, Pageable pageable) {
        if (author == null || author.trim().isEmpty()) {
            return getAllBooks(pageable);
        }
        return bookRepository.findByAuthorNameContainingIgnoreCase(author.trim(), pageable);
    }

    // sökning med flera filter
    public Page<Book> searchBooks(String title, String author, Integer minAvailableCopies, Pageable pageable) {
        // Om vi bara filtrerar på availableCopies
        if ((title == null || title.trim().isEmpty()) &&
                (author == null || author.trim().isEmpty()) &&
                minAvailableCopies != null && minAvailableCopies > 0) {
            return bookRepository.findByAvailableCopiesGreaterThan(minAvailableCopies, pageable);
        }
        //  titel och/eller författare
        if (minAvailableCopies == null || minAvailableCopies <= 0) {
            return bookRepository.findByTitleAndAuthorName(
                    title != null && !title.trim().isEmpty() ? title.trim() : null,
                    author != null && !author.trim().isEmpty() ? author.trim() : null,
                    pageable
            );
        }

        // Fallback till alla böcker om inga filter
        return getAllBooks(pageable);
    }

    // METODER MED QUERY

    public List<Book> getBooksByAuthorId(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new AuthorNotFoundException(authorId);
        }
        return bookRepository.findBooksByAuthorId(authorId);
    }

    public List<Book> getBooksWithMinimumCopies(Integer minCopies) {
        if (minCopies < 0) {
            throw new InvalidBookDataException("Minimum copies cannot be negative");
        }
        return bookRepository.findBooksWithMinimumCopies(minCopies);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public List<Book> getLowStockBooks(Integer threshold) {
        if (threshold < 0) {
            throw new InvalidBookDataException("Threshold cannot be negative");
        }
        return bookRepository.findLowStockBooks(threshold);
    }



    public List<Book> searchBooksAdvanced(String title, String author, Integer minCopies) {
        return bookRepository.searchBooksNative(title, author, minCopies);
    }

    public Long getAvailableBooksCount() {
        return bookRepository.countAvailableBooks();
    }

    public List<Book> getMostAvailableBooks() {
        return bookRepository.findMostAvailableBooks();
    }

    // OPTIONAL Return Metoder

    public Optional<Book> findBookByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return Optional.empty();
        }
        return bookRepository.findByTitleIgnoreCase(title.trim());
    }

    public Book findBookByTitleOrThrow(String title) {
        return findBookByTitle(title)
                .orElseThrow(() -> new BookNotFoundException("Book with title '" + title + "' not found"));
    }



    public List<BookWithDetailsDTO> getAllBooksWithDetails() {
        List<Book> books = bookRepository.findAll();
        return books.stream().map(DTOMapper::toBookDTO).toList();
    }

    //Validering

    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new InvalidBookDataException("Titel får inte vara tom");
        }

        book.setTitle(book.getTitle().trim());

        if (book.getAvailableCopies() <= 0) {
            book.setAvailableCopies(1);
        }

        if (book.getAuthor() == null || book.getAuthor().getAuthorId() == null) {
            throw new InvalidBookDataException("Bok måste ha en giltig författare");
        }
    }
}