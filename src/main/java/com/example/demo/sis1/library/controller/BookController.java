package com.example.demo.sis1.library.controller;

import com.example.demo.sis1.library.dto.*;
import com.example.demo.sis1.library.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) { this.service = service; }

    @GetMapping
    public List<BookResponse> all() { return service.findAll(); }

    @GetMapping("/{id}")
    public BookResponse one(@PathVariable long id) { return service.findById(id); }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        BookResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable long id, @Valid @RequestBody UpdateBookRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paged")
    public PagedModel<BookResponse> getPagedBooks(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return new PagedModel<>(service.findAllPaged(pageable));
    }
}
