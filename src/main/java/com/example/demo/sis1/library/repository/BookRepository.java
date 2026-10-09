package com.example.demo.sis1.library.repository;

import com.example.demo.sis1.library.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAuthor(String author);

    @Query("SELECT b FROM Book b WHERE b.available = true")
    List<Book> findAvailableBooks();

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);
}