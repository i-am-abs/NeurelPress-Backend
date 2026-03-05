package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.response.BookResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.Api_Books)
@RequiredArgsConstructor
@Tag(name = "Books", description = "Book reference endpoints")
public class BookController {

    private final BookService bookService;

    @GetMapping
    @Operation(summary = "Get all books (paginated)")
    public ResponseEntity<PageResponse<BookResponse>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.info("Getting books with page {} and size {}", page, size);
        return ResponseEntity.ok(bookService.getAllBooks(page, size));
    }

    @GetMapping(ApiConstants.Top)
    @Operation(summary = "Get top referenced books")
    public ResponseEntity<List<BookResponse>> getTopBooks(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Getting top referenced books with limit: {}", limit);
        return ResponseEntity.ok(bookService.getTopReferenced(limit));
    }

    @GetMapping(ApiConstants.Search)
    @Operation(summary = "Search books")
    public ResponseEntity<PageResponse<BookResponse>> searchBooks(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.info("Searching books with query: {}, page: {}, size: {}", q, page, size);
        return ResponseEntity.ok(bookService.searchBooks(q, page, size));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get books by category (paginated)")
    public ResponseEntity<PageResponse<BookResponse>> getBooksByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        log.info("Getting books by category: {} with page {} and size {}", category, page, size);
        return ResponseEntity.ok(bookService.getBooksByCategory(category, page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID")
    public ResponseEntity<BookResponse> getBook(@PathVariable UUID id) {
        log.info("Getting book with ID: {}", id);
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}
