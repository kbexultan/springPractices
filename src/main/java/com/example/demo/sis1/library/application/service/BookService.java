
package com.example.demo.sis1.library.application.service;

import com.example.demo.sis1.library.application.port.BookRepository;
import com.example.demo.sis1.library.domain.exception.BookNotFoundException;
import com.example.demo.sis1.library.domain.exception.DuplicateIsbnException;
import com.example.demo.sis1.library.domain.model.Book;

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
    public List<Book> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Book findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book create(Book book) {

        if (repository.existsByIsbn(book.getIsbn())) {
            throw new DuplicateIsbnException(book.getIsbn());
        }

        return repository.save(book);
    }

    @Transactional
    public Book update(long id, UpdateBookCommand command) {

        Book book = findById(id);

        if (repository.existsByIsbnAndIdNot(command.isbn(), id)) {
            throw new DuplicateIsbnException(command.isbn());
        }

        book.update(
                command.title(),
                command.author(),
                command.isbn(),
                command.publicationYear(),
                command.available()
        );

        return repository.save(book);
    }

    @Transactional
    public void delete(long id) {
        repository.delete(findById(id));
    }

    @Transactional(readOnly = true)
    public Page<Book> findAllPaged(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
