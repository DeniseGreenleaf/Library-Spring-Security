package com.example.library.Author;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDto {

    private Long authorId;
    private String firstName;
    private String lastName;

    public AuthorDto(Author author) {
        this.authorId = author.getAuthorId();
        this.firstName = author.getFirstName();
        this.lastName = author.getLastName();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
