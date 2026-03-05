package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dao.Book;
import com.neurelpress.blogs.dto.response.BookResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.mapper.BookMapper;
import com.neurelpress.blogs.repository.BookRepository;
import com.neurelpress.blogs.service.BookService;
import com.neurelpress.blogs.utils.PageResponseSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getAllBooks(int page, int size) {
        Page<Book> p = bookRepository.findAll(Pageable.ofSize(size).withPage(page));
        log.info("Getting all books with page {} and size {}", page, size);
        return PageResponseSupport.from(p, bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.BOOK, CodeConstants.ID, id));
        log.info("Getting book with id {}", id);
        return bookMapper.toResponse(book);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "topReferencedBooks", key = "#limit")
    public List<BookResponse> getTopReferenced(int limit) {
        List<Book> books = bookRepository.findTopReferenced(Pageable.ofSize(limit));
        log.info("Getting top referenced books with limit {}", limit);
        return books.stream().map(bookMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> searchBooks(String query, int page, int size) {
        Page<Book> p = bookRepository.search(query, Pageable.ofSize(size).withPage(page));
        log.info("Searching books with query {} and page {} and size {}", query, page, size);
        return PageResponseSupport.from(p, bookMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getBooksByCategory(String category, int page, int size) {
        Page<Book> p = bookRepository.findByCategory(category, Pageable.ofSize(size).withPage(page));
        log.info("Getting books by category {} with page {} and size {}", category, page, size);
        return PageResponseSupport.from(p, bookMapper::toResponse);
    }
}
