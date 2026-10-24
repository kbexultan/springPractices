package com.example.demo.sis1.library.domain.model;

import com.example.demo.sis1.library.domain.exception.InvalidBookException;

import java.util.Objects;

public class Book {

    public static final int MAX_TITLE_LENGTH = 200;
    public static final int MIN_PUBLICATION_YEAR = 1450;
    private static final String ISBN_PATTERN = "\\d{13}";

    private final Long id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private boolean available;

    private Book(Long id, String title, String author, String isbn, Integer publicationYear, boolean available) {
        this.id = id;
        applyDetails(title, author, isbn, publicationYear);
        this.available = available;
    }

    public static Book create(String title, String author, String isbn, Integer publicationYear) {
        return new Book(null, title, author, isbn, publicationYear, true);
    }

    public static Book restore(Long id, String title, String author, String isbn,
                               Integer publicationYear, boolean available) {
        return new Book(Objects.requireNonNull(id, "id"), title, author, isbn, publicationYear, available);
    }

    public void update(String title, String author, String isbn, Integer publicationYear, boolean available) {
        applyDetails(title, author, isbn, publicationYear);
        this.available = available;
    }

    private void applyDetails(String title, String author, String isbn, Integer publicationYear) {
        if (title == null || title.isBlank()) {
            throw new InvalidBookException("Title is required");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new InvalidBookException("Title must not exceed " + MAX_TITLE_LENGTH + " characters");
        }
        if (author == null || author.isBlank()) {
            throw new InvalidBookException("Author is required");
        }
        if (isbn == null || !isbn.matches(ISBN_PATTERN)) {
            throw new InvalidBookException("ISBN must contain 13 digits");
        }
        if (publicationYear == null || publicationYear < MIN_PUBLICATION_YEAR) {
            throw new InvalidBookException("Invalid publication year");
        }
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getAuthor() { return author; }

    public String getIsbn() { return isbn; }

    public Integer getPublicationYear() { return publicationYear; }

    public boolean isAvailable() { return available; }
}
