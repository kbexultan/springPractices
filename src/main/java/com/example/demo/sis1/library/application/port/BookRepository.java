package com.example.demo.sis1.library.application.port;

import com.example.demo.sis1.library.domain.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<Book> findAll();

    Page<Book> findAll(Pageable pageable);

    Optional<Book> findById(long id);

    Book save(Book book);

    void delete(Book book);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, long id);
}
