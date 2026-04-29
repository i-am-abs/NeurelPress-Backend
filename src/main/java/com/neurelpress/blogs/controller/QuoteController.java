package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.response.QuoteResponse;
import com.neurelpress.blogs.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.Api_Quotes)
@Tag(name = "Quotes", description = "Daily quote endpoints")
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping(ApiConstants.Today)
    @Operation(summary = "Get the quote of the day")
    public ResponseEntity<QuoteResponse> getQuoteOfTheDay() {
        log.info("Getting quote of the day");
        return ResponseEntity.ok(quoteService.getQuoteOfTheDay());
    }

    @GetMapping(ApiConstants.Random)
    @Operation(summary = "Get a random quote")
    public ResponseEntity<QuoteResponse> getRandomQuote() {
        return ResponseEntity.ok(quoteService.getRandomQuote());
    }

    @GetMapping(ApiConstants.Random_By_Domain)
    @Operation(summary = "Get a random quote for a domain")
    public ResponseEntity<QuoteResponse> getRandomQuoteByDomain(@PathVariable String domain) {
        return ResponseEntity.ok(quoteService.getRandomQuoteByDomain(domain));
    }

    @GetMapping(ApiConstants.Punchline_By_Domain)
    @Operation(summary = "Get a tech punchline for a domain")
    public ResponseEntity<QuoteResponse> getTechPunchlineByDomain(@PathVariable String domain) {
        return ResponseEntity.ok(quoteService.getTechPunchlineByDomain(domain));
    }

    @GetMapping(ApiConstants.Random_Front)
    @Operation(summary = "Get front-screen quote/punchline that changes every load")
    public ResponseEntity<QuoteResponse> getFrontScreenQuote() {
        return ResponseEntity.ok(quoteService.getFrontScreenQuote());
    }
}
