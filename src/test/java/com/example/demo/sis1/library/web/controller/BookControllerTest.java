package com.example.demo.sis1.library.web.controller;

import com.example.demo.sis1.library.application.service.BookService;
import com.example.demo.sis1.library.application.service.UpdateBookCommand;
import com.example.demo.sis1.library.domain.exception.BookNotFoundException;
import com.example.demo.sis1.library.domain.exception.DuplicateIsbnException;
import com.example.demo.sis1.library.domain.model.Book;
import com.example.demo.sis1.library.web.mapper.BookWebMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(BookWebMapper.class)
class BookControllerTest {

    private static final String ISBN = "9780132350884";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService service;

    private static Book storedBook() {
        return Book.restore(1L, "Clean Code", "Robert Martin", ISBN, 2008, true);
    }

    @Test
    void getAllReturnsBooksAsJson() throws Exception {
        when(service.findAll()).thenReturn(List.of(storedBook()));

        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Robert Martin"))
                .andExpect(jsonPath("$[0].isbn").value(ISBN))
                .andExpect(jsonPath("$[0].publicationYear").value(2008))
                .andExpect(jsonPath("$[0].available").value(true));
    }

    @Test
    void getOneReturnsBook() throws Exception {
        when(service.findById(1L)).thenReturn(storedBook());

        mockMvc.perform(get("/api/v1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void getOneReturns404ProblemWhenMissing() throws Exception {
        when(service.findById(99L)).thenThrow(new BookNotFoundException(99L));

        mockMvc.perform(get("/api/v1/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Book with id 99 was not found"))
                .andExpect(jsonPath("$.instance").value("/api/v1/books/99"));
    }

    @Test
    void getOneReturns400ForNonNumericId() throws Exception {
        mockMvc.perform(get("/api/v1/books/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid parameter: id"));

        verifyNoInteractions(service);
    }

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(service.create(any(Book.class))).thenReturn(storedBook());

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert Martin",
                                 "isbn":"9780132350884","publicationYear":2008}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/books/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.available").value(true));

        ArgumentCaptor<Book> passed = ArgumentCaptor.forClass(Book.class);
        verify(service).create(passed.capture());
        assertThat(passed.getValue().getId()).isNull();
        assertThat(passed.getValue().getIsbn()).isEqualTo(ISBN);
        assertThat(passed.getValue().isAvailable()).isTrue();
    }

    @Test
    void createReturns400WithFieldErrorsForInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"","author":"Robert Martin","isbn":"123","publicationYear":1000}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(containsString("title: Title is required")))
                .andExpect(jsonPath("$.detail").value(containsString("isbn: ISBN must contain 13 digits")))
                .andExpect(jsonPath("$.detail").value(containsString("publicationYear: Invalid publication year")));

        verifyNoInteractions(service);
    }

    @Test
    void createReturns400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Invalid or missing JSON request body"));

        verifyNoInteractions(service);
    }

    @Test
    void createReturns415ForNonJsonContentType() throws Exception {
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Clean Code"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void createReturns409ForDuplicateIsbn() throws Exception {
        when(service.create(any(Book.class))).thenThrow(new DuplicateIsbnException(ISBN));

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert Martin",
                                 "isbn":"9780132350884","publicationYear":2008}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Book with ISBN " + ISBN + " already exists"));
    }

    @Test
    void updateMapsRequestToCommand() throws Exception {
        when(service.update(eq(1L), any(UpdateBookCommand.class)))
                .thenReturn(Book.restore(1L, "Clean Code", "Robert Martin", ISBN, 2008, false));

        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert Martin",
                                 "isbn":"9780132350884","publicationYear":2008,"available":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));

        verify(service).update(1L, new UpdateBookCommand("Clean Code", "Robert Martin", ISBN, 2008, false));
    }

    @Test
    void updateReturns400WhenAvailabilityIsMissing() throws Exception {
        mockMvc.perform(put("/api/v1/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Clean Code","author":"Robert Martin",
                                 "isbn":"9780132350884","publicationYear":2008}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("available: Availability is required"));

        verify(service, never()).update(anyLong(), any());
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/books/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new BookNotFoundException(99L)).when(service).delete(99L);

        mockMvc.perform(delete("/api/v1/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void pagedEndpointUsesDefaultPagingAndReturnsPageMetadata() throws Exception {
        when(service.findAllPaged(any(Pageable.class))).thenAnswer(invocation -> {
            Pageable pageable = invocation.getArgument(0);
            return new PageImpl<>(List.of(storedBook()), pageable, 1);
        });

        mockMvc.perform(get("/api/v1/books/paged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(service).findAllPaged(pageable.capture());
        assertThat(pageable.getValue()).isEqualTo(PageRequest.of(0, 10, Sort.by("id")));
    }
}
