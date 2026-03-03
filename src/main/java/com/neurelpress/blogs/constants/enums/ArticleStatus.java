package com.neurelpress.blogs.constants.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ArticleStatus {
    DRAFT("Draft"),
    PUBLISHED("Published");

    private final String ArticleStatus;
}
