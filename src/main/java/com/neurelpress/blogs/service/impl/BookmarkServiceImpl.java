package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dao.Article;
import com.neurelpress.blogs.dao.Bookmark;
import com.neurelpress.blogs.dao.User;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.mapper.ArticleMapper;
import com.neurelpress.blogs.repository.ArticleRepository;
import com.neurelpress.blogs.repository.BookmarkRepository;
import com.neurelpress.blogs.repository.UserRepository;
import com.neurelpress.blogs.service.BookmarkService;
import com.neurelpress.blogs.utils.PageResponseSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public void toggleBookmark(UUID userId, String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.Article, CodeConstants.ID, slug));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.ID, userId));

        bookmarkRepository
                .findByUserIdAndArticleId(userId, article.getId()).ifPresentOrElse(bookmarkRepository::delete,
                        () -> bookmarkRepository.save(Bookmark.builder().user(user).article(article).build()));
        article.setBookmarksCount((int) bookmarkRepository.countByArticleId(article.getId()));
        articleRepository.save(article);
        log.info("Bookmark toggled: {} by {}", slug, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(UUID userId, UUID articleId) {
        log.info("Checking if user: {} is bookmarked article: {}", userId, articleId);
        return bookmarkRepository.existsByUserIdAndArticleId(userId, articleId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> getUserBookmarks(UUID userId, int page, int size) {
        Page<Bookmark> p = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(userId, Pageable.ofSize(size).withPage(page));
        log.info("Getting user bookmarks with page {} and size {}", page, size);
        return PageResponseSupport.from(p, b -> articleMapper.toSummaryResponse(b.getArticle()));
    }
}
