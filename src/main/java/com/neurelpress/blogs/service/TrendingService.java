package com.neurelpress.blogs.service;


import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;

import java.util.List;

public interface TrendingService {

    List<ArticleSummaryResponse> getTrendingArticles(int limit);
}
