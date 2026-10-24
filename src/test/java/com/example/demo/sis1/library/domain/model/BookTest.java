package com.example.demo.sis1.library.domain.model;

import com.example.demo.sis1.library.domain.exception.InvalidBookException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookTest {

    @Test
    void newBookIsAvailableAndHasNoId() {
        Book book = Book.create("Clean Code", "Robert Martin", "9780132350884", 2008);

        assertThat(book.getId()).isNull();
        assertThat(book.isAvailable()).isTrue();
    }

    @Test
    void updateReplacesDetailsAndAvailability() {
        Book book = Book.restore(1L, "Old", "Author", "9780132350884", 2000, true);

        book.update("New", "Other", "9780201633610", 1994, false);

        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getTitle()).isEqualTo("New");
        assertThat(book.getAuthor()).isEqualTo("Other");
        assertThat(book.getIsbn()).isEqualTo("9780201633610");
        assertThat(book.getPublicationYear()).isEqualTo(1994);
        assertThat(book.isAvailable()).isFalse();
    }

    @Test
    void rejectsBlankTitle() {
        assertThatThrownBy(() -> Book.create(" ", "Author", "9780132350884", 2008))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("Title is required");
    }

    @Test
    void rejectsTooLongTitle() {
        assertThatThrownBy(() -> Book.create("x".repeat(201), "Author", "9780132350884", 2008))
                .isInstanceOf(InvalidBookException.class);
    }

    @Test
    void rejectsMalformedIsbn() {
        assertThatThrownBy(() -> Book.create("Title", "Author", "978-0132350", 2008))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("ISBN must contain 13 digits");
    }

    @Test
    void rejectsPublicationYearBeforePrintingPress() {
        assertThatThrownBy(() -> Book.create("Title", "Author", "9780132350884", 1449))
                .isInstanceOf(InvalidBookException.class)
                .hasMessage("Invalid publication year");
    }

    @Test
    void failedUpdateLeavesBookUnchanged() {
        Book book = Book.restore(1L, "Old", "Author", "9780132350884", 2000, true);

        assertThatThrownBy(() -> book.update("New", "", "9780201633610", 1994, false))
                .isInstanceOf(InvalidBookException.class);

        assertThat(book.getTitle()).isEqualTo("Old");
        assertThat(book.isAvailable()).isTrue();
    }
}
