package com.neurelpress.blogs.mapper;

import com.neurelpress.blogs.dto.response.BookResponse;
import com.neurelpress.blogs.dao.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        log.info("Mapping book: {}", book);
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getDescription(),
                book.getCoverUrl(),
                book.getCategory(),
                book.getRating(),
                book.getReferencedCount(),
                book.getAffiliateUrl()
        );
    }

    public Set<BookResponse> toResponseSet(Set<Book> books) {
        log.info("Mapping books: {}", books);
        return books.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
}
