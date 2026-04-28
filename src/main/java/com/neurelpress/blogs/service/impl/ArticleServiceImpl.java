package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.constants.ArticleStatus;
import com.neurelpress.blogs.dao.*;
import com.neurelpress.blogs.dto.request.ArticleRequest;
import com.neurelpress.blogs.dto.response.ArticleResponse;
import com.neurelpress.blogs.dto.response.ArticleSummaryResponse;
import com.neurelpress.blogs.dto.response.PageResponse;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.exception.UnauthorizedException;
import com.neurelpress.blogs.mapper.ArticleMapper;
import com.neurelpress.blogs.repository.*;
import com.neurelpress.blogs.service.AiSuggestionsService;
import com.neurelpress.blogs.service.ArticleService;
import com.neurelpress.blogs.service.BookmarkService;
import com.neurelpress.blogs.utils.PageResponseSupport;
import com.neurelpress.blogs.utils.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.neurelpress.blogs.constants.CodeConstants.WORDS_PER_MINUTE;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleClapRepository articleClapRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final BookRepository bookRepository;
    private final ArticleMapper articleMapper;
    private final AiSuggestionsService aiSuggestionsService;
    private final BookmarkService bookmarkService;

    private static int computeReadTime(String content) {
        if (content == null || content.isBlank()) {
            return 0;
        }
        int words = content.trim().split("\\s+").length;
        int minutes = Math.max(1, words / WORDS_PER_MINUTE);
        log.debug("Computed read time: {} words -> {} min", words, minutes);
        return minutes;
    }

    @Override
    @Transactional
    public ArticleResponse createArticle(UUID authorId, @NonNull ArticleRequest request) {
        User author = findUserById(authorId);
        String slug = uniqueSlug(SlugUtils.toSlug(request.title()));
        Article article = buildDraftArticle(author, slug, request);

        article = articleRepository.save(article);
        log.info("Created new article: {}", article.getSlug());
        return articleMapper.toResponse(article);
    }

    @Override
    @Transactional
    public ArticleResponse updateArticle(UUID authorId, String slug, @NonNull ArticleRequest request) {
        Article article = findArticleBySlug(slug);
        ensureAuthorOwnership(authorId, article, "update");

        article.setTitle(request.title());
        article.setSummary(request.summary());
        article.setContent(request.content());
        article.setCoverImage(request.coverImage());
        article.setSeoTitle(request.seoTitle());
        article.setSeoDescription(request.seoDescription());
        article.setCanonicalUrl(request.canonicalUrl());
        article.setReadTime(computeReadTime(request.content()));
        article.setTags(resolveTags(request.tagSlugs()));
        article.setBooks(resolveBooks(request.bookIds()));

        article = articleRepository.save(article);
        log.info("Updated article: {}", article.getSlug());
        return articleMapper.toResponse(article);
    }

    @Override
    @Transactional
    public ArticleResponse publishArticle(UUID authorId, String slug) {
        Article article = findArticleBySlug(slug);
        ensureAuthorOwnership(authorId, article, "publish");

        if (article.getTags().isEmpty() && article.getContent() != null && !article.getContent().isBlank()) {
            try {
                var suggestedSlugs = aiSuggestionsService.suggestTags(article.getTitle(), article.getContent());
                if (suggestedSlugs != null && !suggestedSlugs.isEmpty()) {
                    var found = tagRepository.findBySlugIn(suggestedSlugs);
                    if (!found.isEmpty()) {
                        article.setTags(new HashSet<>(found));
                        log.info("AI attached {} tag(s) to article {}: {}", found.size(), slug,
                                String.join(", ", found.stream().map(Tag::getSlug).toList()));
                    }
                }
            } catch (Exception e) {
                log.warn("AI tag suggestion failed for article {}: {}", slug, e.getMessage());
            }
        }

        article.setStatus(ArticleStatus.PUBLISHED);
        article.setPublishedAt(Instant.now());
        article = articleRepository.save(article);
        log.info("Published article: {}", article.getSlug());
        return articleMapper.toResponse(article);
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleResponse getArticleBySlug(UUID viewerId, String slug) {
        if (viewerId == null) {
            Article published = articleRepository.findPublishedBySlug(slug)
                    .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.Article, CodeConstants.SLUG, slug));
            log.info("Article viewed: {}", slug);
            return articleMapper.toResponse(published, null);
        }

        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.Article, CodeConstants.SLUG, slug));

        if (article.getStatus() != ArticleStatus.PUBLISHED
                && !article.getAuthor().getId().equals(viewerId)) {
            throw new ResourceNotFoundException(CodeConstants.Article, CodeConstants.SLUG, slug);
        }

        Boolean bookmarked = bookmarkService.isBookmarked(viewerId, article.getId());
        log.info("Article viewed: {}", slug);
        return articleMapper.toResponse(article, bookmarked);
    }

    @Override
    @Transactional
    public void recordView(String slug) {
        articleRepository.findBySlug(slug)
                .ifPresent(a -> articleRepository.incrementViews(a.getId()));
        log.debug("Article view recorded: {}", slug);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> getPublishedArticles(int page, int size) {
        Page<Article> p = articleRepository.findByStatusOrderByPublishedAtDesc(
                ArticleStatus.PUBLISHED, Pageable.ofSize(size).withPage(page));
        log.info("Getting published articles with page {} and size {}", page, size);
        return PageResponseSupport.from(p, articleMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> getArticlesByTag(String tagSlug, int page, int size) {
        Page<Article> p = articleRepository.findByTagSlug(tagSlug, Pageable.ofSize(size).withPage(page));
        log.info("Getting articles by tag {} with page {} and size {}", tagSlug, page, size);
        return PageResponseSupport.from(p, articleMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> getArticlesByAuthor(UUID authorId, int page, int size) {
        Page<Article> p = articleRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(
                authorId, ArticleStatus.PUBLISHED, Pageable.ofSize(size).withPage(page));
        log.info("Getting articles by author {} with page {} and size {}", authorId, page, size);
        return PageResponseSupport.from(p, articleMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> getDraftsByAuthor(UUID authorId, int page, int size) {
        Page<Article> p = articleRepository.findByAuthorIdAndStatusOrderByCreatedAtDesc(
                authorId, ArticleStatus.DRAFT, Pageable.ofSize(size).withPage(page));
        log.info("Getting drafts by author {} with page {} and size {}", authorId, page, size);
        return PageResponseSupport.from(p, articleMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ArticleSummaryResponse> getArticlesByAuthorAllStatus(UUID authorId, int page, int size) {
        Page<Article> p = articleRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, Pageable.ofSize(size).withPage(page));
        log.info("Getting all articles by author {} with page {} and size {}", authorId, page, size);
        return PageResponseSupport.from(p, articleMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public void deleteArticle(UUID authorId, String slug) {
        Article article = findArticleBySlug(slug);
        ensureAuthorOwnership(authorId, article, "delete");
        articleRepository.delete(article);
    }

    @Override
    @Transactional
    public void clapArticle(UUID userId, String slug) {
        Article article = articleRepository.findPublishedBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.Article, CodeConstants.SLUG, slug));
        if (articleClapRepository.findByUserIdAndArticleId(userId, article.getId()).isPresent()) {
            log.debug("User {} already clapped article {}", userId, slug);
            return;
        }
        User user = findUserById(userId);
        articleClapRepository.save(ArticleClap.builder().user(user).article(article).build());
        articleRepository.incrementClaps(article.getId());
        log.info("Article clapped: {} by user {}", slug, userId);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.USER, CodeConstants.ID, userId));
    }

    private Article findArticleBySlug(String slug) {
        return articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException(CodeConstants.Article, CodeConstants.SLUG, slug));
    }

    private void ensureAuthorOwnership(UUID authorId, @NonNull Article article, String operation) {
        if (!article.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedException("Not authorized to " + operation + " this article");
        }
    }

    private @NonNull Article buildDraftArticle(User author, String slug, @NonNull ArticleRequest request) {
        return Article.builder()
                .author(author)
                .title(request.title())
                .slug(slug)
                .summary(request.summary())
                .content(request.content())
                .coverImage(request.coverImage())
                .status(ArticleStatus.DRAFT)
                .readTime(computeReadTime(request.content()))
                .seoTitle(request.seoTitle())
                .seoDescription(request.seoDescription())
                .canonicalUrl(request.canonicalUrl())
                .tags(resolveTags(request.tagSlugs()))
                .books(resolveBooks(request.bookIds()))
                .build();
    }

    private String uniqueSlug(String base) {
        String slug = base;
        int suffix = 0;
        while (articleRepository.existsBySlug(slug)) {
            slug = base + "-" + (++suffix);
        }
        return slug;
    }

    @Contract("null -> new")
    private @NonNull Set<Tag> resolveTags(List<String> tagSlugs) {
        if (tagSlugs == null || tagSlugs.isEmpty()) return new HashSet<>();
        List<Tag> found = tagRepository.findBySlugIn(tagSlugs);
        if (found.size() != tagSlugs.size()) {
            throw new ResourceNotFoundException(CodeConstants.TAGS, CodeConstants.SLUG, String.join(", ", tagSlugs));
        }
        log.info("Resolved {} tags: {}", found.size(), String.join(", ", tagSlugs));
        return new HashSet<>(found);
    }

    @Contract("null -> new")
    private @NonNull Set<Book> resolveBooks(List<UUID> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Book> found = bookRepository.findAllById(bookIds);
        if (found.size() != bookIds.size()) {
            throw new ResourceNotFoundException(CodeConstants.BOOK, CodeConstants.ID, bookIds.toString());
        }
        log.info("Resolved {} books", found.size());
        return new HashSet<>(found);
    }
}
