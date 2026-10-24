package com.example.demo.sis1.library.infrastructure.adapter;

import com.example.demo.sis1.library.application.port.BookRepository;
import com.example.demo.sis1.library.domain.model.Book;
import com.example.demo.sis1.library.infrastructure.persistence.mapper.BookPersistenceMapper;
import com.example.demo.sis1.library.infrastructure.persistence.repository.SpringDataBookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BookRepositoryAdapter implements BookRepository {

    private final SpringDataBookRepository jpaRepository;
    private final BookPersistenceMapper mapper;

    public BookRepositoryAdapter(SpringDataBookRepository jpaRepository, BookPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Book> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Optional<Book> findById(long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Book save(Book book) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(book)));
    }

    @Override
    public void delete(Book book) {
        jpaRepository.deleteById(book.getId());
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return jpaRepository.existsByIsbn(isbn);
    }

    @Override
    public boolean existsByIsbnAndIdNot(String isbn, long id) {
        return jpaRepository.existsByIsbnAndIdNot(isbn, id);
    }
}
