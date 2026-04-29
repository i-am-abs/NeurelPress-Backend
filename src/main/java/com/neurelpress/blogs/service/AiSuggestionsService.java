package com.neurelpress.blogs.service;

import java.util.List;

public interface AiSuggestionsService {

    List<String> suggestTags(String title, String contentSnippet);

    String suggestTitle(String contentSnippet);

    String summarize(String contentSnippet);

    String humanize(String contentSnippet);

    java.util.Map<String, Object> analyzeTone(String contentSnippet);

    String generateByTone(String contentSnippet, String tone);
}
