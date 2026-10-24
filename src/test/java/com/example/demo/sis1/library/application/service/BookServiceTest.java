package com.example.demo.sis1.library.application.service;

import com.example.demo.sis1.library.application.port.BookRepository;
import com.example.demo.sis1.library.domain.exception.BookNotFoundException;
import com.example.demo.sis1.library.domain.exception.DuplicateIsbnException;
import com.example.demo.sis1.library.domain.exception.InvalidBookException;
import com.example.demo.sis1.library.domain.model.Book;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private static final String ISBN = "9780132350884";
    private static final String OTHER_ISBN = "9780201633610";

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService service;

    private static Book storedBook() {
        return Book.restore(1L, "Clean Code", "Robert Martin", ISBN, 2008, true);
    }

    @Test
    void findByIdReturnsBook() {
        when(repository.findById(1L)).thenReturn(Optional.of(storedBook()));

        Book book = service.findById(1L);

        assertThat(book.getTitle()).isEqualTo("Clean Code");
        verify(repository).findById(1L);
    }

    @Test
    void findByIdThrowsWhenBookIsMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(42L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessage("Book with id 42 was not found");
    }

    @Test
    void findAllDelegatesToRepository() {
        when(repository.findAll()).thenReturn(List.of(storedBook()));

        assertThat(service.findAll()).extracting(Book::getIsbn).containsExactly(ISBN);
    }

    @Test
    void findAllPagedDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<>(List.of(storedBook()), pageable, 1);
        when(repository.findAll(pageable)).thenReturn(page);

        assertThat(service.findAllPaged(pageable)).isSameAs(page);
    }

    @Test
    void createSavesNewAvailableBook() {
        Book newBook = Book.create("Clean Code", "Robert Martin", ISBN, 2008);
        when(repository.existsByIsbn(ISBN)).thenReturn(false);
        when(repository.save(newBook)).thenReturn(storedBook());

        Book created = service.create(newBook);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.isAvailable()).isTrue();
        verify(repository).existsByIsbn(ISBN);
        verify(repository).save(newBook);
    }

    @Test
    void createRejectsDuplicateIsbn() {
        Book newBook = Book.create("Clean Code", "Robert Martin", ISBN, 2008);
        when(repository.existsByIsbn(ISBN)).thenReturn(true);

        assertThatThrownBy(() -> service.create(newBook))
                .isInstanceOf(DuplicateIsbnException.class)
                .hasMessage("Book with ISBN " + ISBN + " already exists");
        verify(repository, never()).save(any());
    }

    @Test
    void updateAppliesChangesAndSaves() {
        when(repository.findById(1L)).thenReturn(Optional.of(storedBook()));
        when(repository.existsByIsbnAndIdNot(OTHER_ISBN, 1L)).thenReturn(false);
        when(repository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Book updated = service.update(1L,
                new UpdateBookCommand("Design Patterns", "Gang of Four", OTHER_ISBN, 1994, false));

        ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(1L);
        assertThat(saved.getValue().getTitle()).isEqualTo("Design Patterns");
        assertThat(saved.getValue().getAuthor()).isEqualTo("Gang of Four");
        assertThat(saved.getValue().getIsbn()).isEqualTo(OTHER_ISBN);
        assertThat(saved.getValue().getPublicationYear()).isEqualTo(1994);
        assertThat(saved.getValue().isAvailable()).isFalse();
        assertThat(updated).isSameAs(saved.getValue());
    }

    @Test
    void updateThrowsWhenBookIsMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(42L,
                new UpdateBookCommand("Title", "Author", ISBN, 2000, true)))
                .isInstanceOf(BookNotFoundException.class);
        verify(repository, never()).existsByIsbnAndIdNot(anyString(), anyLong());
        verify(repository, never()).save(any());
    }

    @Test
    void updateRejectsIsbnOwnedByAnotherBook() {
        when(repository.findById(1L)).thenReturn(Optional.of(storedBook()));
        when(repository.existsByIsbnAndIdNot(OTHER_ISBN, 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L,
                new UpdateBookCommand("Title", "Author", OTHER_ISBN, 2000, true)))
                .isInstanceOf(DuplicateIsbnException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateRejectsInvalidDataThroughDomainRules() {
        when(repository.findById(1L)).thenReturn(Optional.of(storedBook()));
        when(repository.existsByIsbnAndIdNot(ISBN, 1L)).thenReturn(false);

        assertThatThrownBy(() -> service.update(1L,
                new UpdateBookCommand("Title", "Author", ISBN, 1200, true)))
                .isInstanceOf(InvalidBookException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesExistingBook() {
        Book book = storedBook();
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.delete(1L);

        verify(repository).delete(book);
    }

    @Test
    void deleteThrowsWhenBookIsMissing() {
        when(repository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(42L)).isInstanceOf(BookNotFoundException.class);
        verify(repository, never()).delete(any());
    }
}
