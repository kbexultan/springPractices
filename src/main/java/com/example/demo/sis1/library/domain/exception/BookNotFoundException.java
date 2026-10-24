package com.example.demo.sis1.library.domain.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) { super("Book with id " + id + " was not found"); }
}
