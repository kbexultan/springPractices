
package com.example.demo.sis1.library.service;

import com.example.demo.sis1.library.repository.BookRepository;
import com.example.demo.sis1.library.dto.*;
import com.example.demo.sis1.library.entity.Book;
import com.example.demo.sis1.library.exception.BookNotFoundException;
import com.example.demo.sis1.library.exception.DuplicateIsbnException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(BookResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookResponse findById(long id) {
        Book book = repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        return BookResponse.from(book);
    }

    @Transactional
    public BookResponse create(CreateBookRequest request) {

        if (repository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        Book book = new Book();

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setAvailable(true);

        Book savedBook = repository.save(book);

        return BookResponse.from(savedBook);
    }

    @Transactional
    public BookResponse update(long id, UpdateBookRequest request) {

        Book book = repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        if (repository.existsByIsbnAndIdNot(request.getIsbn(), id)) {
            throw new DuplicateIsbnException(request.getIsbn());
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setPublicationYear(request.getPublicationYear());
        book.setAvailable(request.getAvailable());

        Book savedBook = repository.save(book);

        return BookResponse.from(savedBook);
    }

    @Transactional
    public void delete(long id) {

        Book book = repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));

        repository.delete(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponse> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable)
                .map(BookResponse::from);
    }
}
