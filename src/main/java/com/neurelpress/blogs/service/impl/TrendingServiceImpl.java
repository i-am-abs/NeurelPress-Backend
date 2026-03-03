package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.mapper.ArticleMapper;
import com.neurelpress.blogs.repository.ArticleAnalyticsRepository;
import com.neurelpress.blogs.service.TrendingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrendingServiceImpl implements TrendingService {

    private final ArticleAnalyticsRepository articleAnalyticsRepository;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ArticleSummaryResponse> getTrendingArticles(int limit) {
        log.info("Getting trending articles with limit: {}", limit);
        return articleAnalyticsRepository.findTrending(PageRequest.of(0, limit))
                .stream()
                .map(aa -> articleMapper.toSummaryResponse(aa.getArticle()))
                .toList();
    }
}
