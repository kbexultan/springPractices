package com.example.demo.sis1.library.web.mapper;

import com.example.demo.sis1.library.application.service.UpdateBookCommand;
import com.example.demo.sis1.library.domain.model.Book;
import com.example.demo.sis1.library.web.dto.request.CreateBookRequest;
import com.example.demo.sis1.library.web.dto.request.UpdateBookRequest;
import com.example.demo.sis1.library.web.dto.response.BookResponse;
import org.springframework.stereotype.Component;

@Component
public class BookWebMapper {

    public Book toDomain(CreateBookRequest request) {
        return Book.create(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPublicationYear()
        );
    }

    public UpdateBookCommand toCommand(UpdateBookRequest request) {
        return new UpdateBookCommand(
                request.getTitle(),
                request.getAuthor(),
                request.getIsbn(),
                request.getPublicationYear(),
                request.getAvailable()
        );
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.isAvailable()
        );
    }
}
