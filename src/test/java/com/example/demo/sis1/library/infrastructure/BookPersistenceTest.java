package com.example.demo.sis1.library.infrastructure;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.sis1.library.domain.model.Book;
import com.example.demo.sis1.library.infrastructure.adapter.BookRepositoryAdapter;
import com.example.demo.sis1.library.infrastructure.persistence.entity.BookEntity;
import com.example.demo.sis1.library.infrastructure.persistence.mapper.BookPersistenceMapper;
import com.example.demo.sis1.library.infrastructure.persistence.repository.SpringDataBookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, BookRepositoryAdapter.class, BookPersistenceMapper.class})
class BookPersistenceTest {

    private static final String SEEDED_ISBN = "9786019000001";

    @Autowired
    private BookRepositoryAdapter adapter;

    @Autowired
    private SpringDataBookRepository jpaRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void runsOnPostgresWithFlywaySchema() {
        String version = jdbcTemplate.queryForObject("SELECT version()", String.class);
        Integer applied = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success", Integer.class);

        assertThat(version).startsWith("PostgreSQL");
        assertThat(applied).isEqualTo(2);
        assertThat(jpaRepository.existsByIsbn(SEEDED_ISBN)).isTrue();
    }

    @Test
    void savesDomainBookAndReadsItBack() {
        Book saved = adapter.save(Book.create("Clean Code", "Robert Martin", "9780132350884", 2008));
        jpaRepository.flush();

        assertThat(saved.getId()).isNotNull();

        Book loaded = adapter.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getTitle()).isEqualTo("Clean Code");
        assertThat(loaded.getAuthor()).isEqualTo("Robert Martin");
        assertThat(loaded.getIsbn()).isEqualTo("9780132350884");
        assertThat(loaded.getPublicationYear()).isEqualTo(2008);
        assertThat(loaded.isAvailable()).isTrue();

        Integer rows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM books WHERE isbn = ?", Integer.class, "9780132350884");
        assertThat(rows).isEqualTo(1);
    }

    @Test
    void updatesExistingRow() {
        Book saved = adapter.save(Book.create("Draft", "Author", "9780132350884", 2008));

        saved.update("Final", "Author", "9780132350884", 2008, false);
        adapter.save(saved);
        jpaRepository.flush();

        Boolean available = jdbcTemplate.queryForObject(
                "SELECT available FROM books WHERE id = ?", Boolean.class, saved.getId());
        assertThat(available).isFalse();
        assertThat(adapter.findById(saved.getId()).orElseThrow().getTitle()).isEqualTo("Final");
    }

    @Test
    void isbnUniquenessQueriesRespectTheCurrentBook() {
        Book saved = adapter.save(Book.create("Clean Code", "Robert Martin", "9780132350884", 2008));

        assertThat(adapter.existsByIsbn("9780132350884")).isTrue();
        assertThat(adapter.existsByIsbn("9780000000000")).isFalse();
        assertThat(adapter.existsByIsbnAndIdNot("9780132350884", saved.getId())).isFalse();
        assertThat(adapter.existsByIsbnAndIdNot(SEEDED_ISBN, saved.getId())).isTrue();
    }

    @Test
    void findAvailableBooksReturnsOnlyAvailableOnes() {
        jpaRepository.save(new BookEntity(null, "On shelf", "A", "9780000000001", 2001, true));
        jpaRepository.save(new BookEntity(null, "Lent out", "B", "9780000000002", 2002, false));

        assertThat(jpaRepository.findAvailableBooks())
                .extracting(BookEntity::getIsbn)
                .contains("9780000000001", SEEDED_ISBN)
                .doesNotContain("9780000000002");
    }

    @Test
    void findByAuthorMatchesExactAuthor() {
        jpaRepository.save(new BookEntity(null, "Book 1", "Robert Martin", "9780000000001", 2001, true));
        jpaRepository.save(new BookEntity(null, "Book 2", "Martin Fowler", "9780000000002", 2002, true));

        assertThat(jpaRepository.findByAuthor("Robert Martin"))
                .extracting(BookEntity::getTitle)
                .containsExactly("Book 1");
    }

    @Test
    void pagingIsAppliedBySql() {
        adapter.save(Book.create("Book 1", "A", "9780000000001", 2001));
        adapter.save(Book.create("Book 2", "B", "9780000000002", 2002));

        Page<Book> page = adapter.findAll(PageRequest.of(0, 2, Sort.by("id")));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getContent().get(0).getIsbn()).isEqualTo(SEEDED_ISBN);
    }

    @Test
    void deleteRemovesRow() {
        Book saved = adapter.save(Book.create("Temp", "A", "9780000000001", 2001));

        adapter.delete(saved);
        jpaRepository.flush();

        assertThat(adapter.findById(saved.getId())).isEmpty();
    }

    @Test
    void databaseEnforcesUniqueIsbn() {
        // IDENTITY ids make Hibernate INSERT immediately on save, so the constraint fires there
        assertThatThrownBy(() -> jpaRepository.saveAndFlush(
                new BookEntity(null, "Copy", "A", SEEDED_ISBN, 2001, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
