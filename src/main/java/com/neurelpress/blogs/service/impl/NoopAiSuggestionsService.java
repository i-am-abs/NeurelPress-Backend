package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.service.AiSuggestionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@ConditionalOnMissingBean(AiSuggestionsService.class)
public class NoopAiSuggestionsService implements AiSuggestionsService {
    @Override
    public List<String> suggestTags(String title, String contentSnippet) {
        log.debug("AI provider not configured; skipping tag suggestions.");
        return Collections.emptyList();
    }

    @Override
    public String suggestTitle(String contentSnippet) {
        return null;
    }

    @Override
    public String summarize(String contentSnippet) {
        return null;
    }
}
