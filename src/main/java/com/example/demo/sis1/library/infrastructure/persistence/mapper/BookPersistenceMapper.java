package com.example.demo.sis1.library.infrastructure.persistence.mapper;

import com.example.demo.sis1.library.domain.model.Book;
import com.example.demo.sis1.library.infrastructure.persistence.entity.BookEntity;
import org.springframework.stereotype.Component;

@Component
public class BookPersistenceMapper {

    public Book toDomain(BookEntity entity) {
        return Book.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getAuthor(),
                entity.getIsbn(),
                entity.getPublicationYear(),
                entity.isAvailable()
        );
    }

    public BookEntity toEntity(Book book) {
        return new BookEntity(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.isAvailable()
        );
    }
}
