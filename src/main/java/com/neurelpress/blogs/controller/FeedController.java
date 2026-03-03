package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dao.Article;
import com.neurelpress.blogs.constants.enums.ArticleStatus;
import com.neurelpress.blogs.repository.ArticleRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.Api_Feed)
@Tag(name = "Feed", description = "RSS feed endpoint")
public class FeedController {

    private final ArticleRepository articleRepository;

    @Value("${neuralpress.cors.allowed-origins}")
    private String siteUrl;

    @GetMapping(value = ApiConstants.Really_Simple_Syndication, produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Get RSS feed of latest articles")
    public ResponseEntity<String> rssFeed() {
        List<Article> articles = articleRepository.findByStatusOrderByPublishedAtDesc(
                ArticleStatus.PUBLISHED, PageRequest.of(0, 50)).getContent();

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        xml.append("<channel>\n");
        xml.append("  <title>NeuralPress</title>\n");
        xml.append("  <link>").append(siteUrl).append("</link>\n");
        xml.append("  <description>AI Engineering Publishing Platform</description>\n");
        xml.append("  <language>en-us</language>\n");
        xml.append("  <atom:link href=\"").append(siteUrl).append("/api/feed/rss\" rel=\"self\" type=\"application/rss+xml\"/>\n");

        DateTimeFormatter rfc822 = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z");

        for (Article article : articles) {
            xml.append("  <item>\n");
            xml.append("    <title><![CDATA[").append(article.getTitle()).append("]]></title>\n");
            xml.append("    <link>").append(siteUrl).append("/@")
                    .append(article.getAuthor().getUsername()).append("/")
                    .append(article.getSlug()).append("</link>\n");
            xml.append("    <description><![CDATA[").append(article.getSummary() != null ? article.getSummary() : "").append("]]></description>\n");
            if (article.getPublishedAt() != null) {
                xml.append("    <pubDate>").append(article.getPublishedAt().atOffset(ZoneOffset.UTC).format(rfc822)).append("</pubDate>\n");
            }
            xml.append("    <guid>").append(siteUrl).append("/@")
                    .append(article.getAuthor().getUsername()).append("/")
                    .append(article.getSlug()).append("</guid>\n");
            xml.append("  </item>\n");
        }

        xml.append("</channel>\n");
        xml.append("</rss>");

        log.info("Generated Really Simple Syndication feed");
        return ResponseEntity.ok(xml.toString());
    }
}
