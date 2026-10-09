package com.example.demo.sis1.library.exception;

public class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(String isbn) { super("Book with ISBN " + isbn + " already exists"); }
}
