package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.dto.properties.NeuralPressCorsProperties;
import com.neurelpress.blogs.constants.ArticleStatus;
import com.neurelpress.blogs.dao.Article;
import com.neurelpress.blogs.repository.ArticleRepository;
import com.neurelpress.blogs.service.FeedService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private static final int RSS_LIMIT = 50;
    private static final DateTimeFormatter RFC_822_FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");

    private final ArticleRepository articleRepository;
    private final NeuralPressCorsProperties corsProperties;

    @Override
    public String generateRssFeed() {
        String frontendUrl = corsProperties.app().primaryFrontendUrl();
        List<Article> articles = articleRepository.findByStatusOrderByPublishedAtDesc(
                ArticleStatus.PUBLISHED, PageRequest.of(0, RSS_LIMIT)).getContent();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("<channel>\n");
        xml.append("  <title>NeuralPress</title>\n");
        xml.append("  <link>").append(frontendUrl).append("</link>\n");
        xml.append("  <description>AI Engineering Publishing Platform</description>\n");
        xml.append("  <language>en-us</language>\n");
        xml.append("  <atom:link href=\"").append(frontendUrl).append("/api/feed/rss\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        for (Article article : articles) {
            appendItem(xml, article, frontendUrl);
        }

        xml.append("</channel>\n");
        xml.append("</rss>");
        return xml.toString();
    }

    private void appendItem(@NonNull StringBuilder xml, @NonNull Article article, @NonNull String frontendUrl) {
        String articleUrl = frontendUrl + "/u/" + article.getAuthor().getUsername() + "/" + article.getSlug();
        xml.append("  <item>\n");
        xml.append("    <title><![CDATA[").append(article.getTitle()).append("]]></title>\n");
        xml.append("    <link>").append(articleUrl).append("</link>\n");
        xml.append("    <description><![CDATA[").append(article.getSummary() != null ? article.getSummary() : "").append("]]></description>\n");
        if (article.getPublishedAt() != null) {
            xml.append("    <pubDate>")
                    .append(article.getPublishedAt().atOffset(ZoneOffset.UTC).format(RFC_822_FORMATTER))
                    .append("</pubDate>\n");
        }
        xml.append("    <guid>").append(articleUrl).append("</guid>\n");
        xml.append("  </item>\n");
    }
}
