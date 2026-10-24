package com.example.demo.sis1.library.web.controller;

import com.example.demo.sis1.library.application.service.BookService;
import com.example.demo.sis1.library.web.dto.request.CreateBookRequest;
import com.example.demo.sis1.library.web.dto.request.UpdateBookRequest;
import com.example.demo.sis1.library.web.dto.response.BookResponse;
import com.example.demo.sis1.library.web.mapper.BookWebMapper;
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
    private final BookWebMapper mapper;

    public BookController(BookService service, BookWebMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    public List<BookResponse> all() {
        return service.findAll().stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public BookResponse one(@PathVariable long id) { return mapper.toResponse(service.findById(id)); }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody CreateBookRequest request) {
        BookResponse created = mapper.toResponse(service.create(mapper.toDomain(request)));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public BookResponse update(@PathVariable long id, @Valid @RequestBody UpdateBookRequest request) {
        return mapper.toResponse(service.update(id, mapper.toCommand(request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/paged")
    public PagedModel<BookResponse> getPagedBooks(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {

        return new PagedModel<>(service.findAllPaged(pageable).map(mapper::toResponse));
    }
}
