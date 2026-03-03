package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.constants.CodeConstants;
import com.neurelpress.blogs.dto.response.QuoteResponse;
import com.neurelpress.blogs.dao.Quote;
import com.neurelpress.blogs.exception.ResourceNotFoundException;
import com.neurelpress.blogs.repository.QuoteRepository;
import com.neurelpress.blogs.service.QuoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getQuoteOfTheDay() {
        List<Quote> active = quoteRepository.findAllActive();
        if (active.isEmpty()) {
            throw new ResourceNotFoundException(CodeConstants.Quote, CodeConstants.Active, "none");
        }
        int dayOfYear = LocalDate.now().getDayOfYear();
        Quote quote = active.get(Math.floorMod(dayOfYear, active.size()));
        log.info("Quote of the day: {}", quote.getText());
        return new QuoteResponse(quote.getId(), quote.getText(), quote.getAuthor(), quote.getSource());
    }
}
