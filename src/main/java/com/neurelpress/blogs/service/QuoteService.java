package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.response.QuoteResponse;

public interface QuoteService {

    QuoteResponse getQuoteOfTheDay();

    QuoteResponse getRandomQuote();

    QuoteResponse getRandomQuoteByDomain(String domain);

    QuoteResponse getTechPunchlineByDomain(String domain);

    QuoteResponse getFrontScreenQuote();
}
