package com.example.library.Author;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    // Lista alla författare
    @GetMapping
    public ResponseEntity<List<Author>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }


    // Hämta författare via efternamn
    @GetMapping("/name/{lastName}")
    public ResponseEntity<List<Author>> getAuthorsByLastName(@PathVariable String lastName) {
        List<Author> authors = authorService.findAuthorsByLastName(lastName);
        return ResponseEntity.ok(authors);
    }

    // Skapa ny författare
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Author> createAuthor(@Valid @RequestBody Author author) {
        Author created = authorService.createAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Hämta författare via ID
    @GetMapping("/{id}")
    public ResponseEntity<Author> getAuthorById(@PathVariable Long id) {
        Author author = authorService.getAuthorById(id);
        return ResponseEntity.ok(author);
    }


}
