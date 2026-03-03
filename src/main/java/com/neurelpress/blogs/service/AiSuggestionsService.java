package com.neurelpress.blogs.service;

import java.util.List;

public interface AiSuggestionsService {

    List<String> suggestTags(String title, String contentSnippet);

    String suggestTitle(String contentSnippet);

    String summarize(String contentSnippet);
}
