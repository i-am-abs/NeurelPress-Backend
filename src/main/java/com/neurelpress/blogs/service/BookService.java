package com.neurelpress.blogs.service;


import com.neurelpress.blogs.dto.response.BookResponse;
import com.neurelpress.blogs.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

public interface BookService {

    PageResponse<BookResponse> getAllBooks(int page, int size);

    BookResponse getBookById(UUID id);

    List<BookResponse> getTopReferenced(int limit);

    PageResponse<BookResponse> searchBooks(String query, int page, int size);
}
