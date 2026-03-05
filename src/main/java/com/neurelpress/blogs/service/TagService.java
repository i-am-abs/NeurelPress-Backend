package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.response.TagResponse;

import java.util.List;
import java.util.Optional;

public interface TagService {

    List<TagResponse> getAllTags();

    List<TagResponse> getTopTags(int limit);

    Optional<TagResponse> getBySlug(String slug);

    Optional<TagResponse> getByNameIgnoreCase(String name);
}

