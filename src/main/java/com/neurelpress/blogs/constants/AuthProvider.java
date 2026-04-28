package com.neurelpress.blogs.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthProvider {
    LOCAL("local"),
    GOOGLE("google"),
    GITHUB("github");

    private final String AuthProvider;
}
