package com.example.demo.sis1.library.infrastructure.persistence.repository;

import com.example.demo.sis1.library.infrastructure.persistence.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SpringDataBookRepository extends JpaRepository<BookEntity, Long> {

    List<BookEntity> findByAuthor(String author);

    @Query("SELECT b FROM BookEntity b WHERE b.available = true")
    List<BookEntity> findAvailableBooks();

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);
}