package com.neurelpress.blogs.service;


import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dto.response.PageResponse;

import java.util.UUID;


public interface BookmarkService {

    void toggleBookmark(UUID userId, String slug);

    boolean isBookmarked(UUID userId, UUID articleId);

    PageResponse<ArticleSummaryResponse> getUserBookmarks(UUID userId, int page, int size);
}
