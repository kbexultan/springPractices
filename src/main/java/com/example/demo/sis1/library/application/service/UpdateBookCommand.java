package com.example.demo.sis1.library.application.service;

public record UpdateBookCommand(
        String title,
        String author,
        String isbn,
        Integer publicationYear,
        boolean available
) {
}
