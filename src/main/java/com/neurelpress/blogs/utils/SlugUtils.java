package com.neurelpress.blogs.utils;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.text.Normalizer;
import java.util.Locale;

@Slf4j
@NoArgsConstructor
public final class SlugUtils {

    public static @NonNull String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        log.info("Normalized input: {}", normalized);
        String slug = normalized
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");

        if (slug.length() > 200) {
            slug = slug.substring(0, 200);
        }

        return slug;
    }
}
