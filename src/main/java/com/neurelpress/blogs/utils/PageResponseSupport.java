package com.neurelpress.blogs.utils;

import com.neurelpress.blogs.dto.response.PageResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@Slf4j
@NoArgsConstructor
public final class PageResponseSupport {

    public static <T, R> PageResponse<R> from(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent()
                .stream()
                .map(mapper)
                .toList();
        log.info("Page response: {}", content);
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                (int) page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
