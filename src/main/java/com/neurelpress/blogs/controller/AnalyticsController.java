package com.neurelpress.blogs.controller;

import com.neurelpress.blogs.constants.ApiConstants;
import com.neurelpress.blogs.dto.request.CrashReportRequest;
import com.neurelpress.blogs.dto.request.UsageEventRequest;
import com.neurelpress.blogs.dto.response.AnalyticsSummaryResponse;
import com.neurelpress.blogs.security.UserPrincipal;
import com.neurelpress.blogs.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiConstants.Api_Analytics)
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Usage events and crash reports")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping(ApiConstants.Track)
    @Operation(summary = "Track a single product event (page view, click, read, ...)")
    public ResponseEntity<Void> track(@AuthenticationPrincipal UserPrincipal principal,
                                      @Valid @RequestBody UsageEventRequest request,
                                      HttpServletRequest http) {
        UUID userId = principal != null ? principal.getId() : null;
        analyticsService.trackEvent(request, userId, http);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(ApiConstants.Crash)
    @Operation(summary = "Report a frontend / client-side crash")
    public ResponseEntity<Void> crash(@AuthenticationPrincipal UserPrincipal principal,
                                      @Valid @RequestBody CrashReportRequest request,
                                      HttpServletRequest http) {
        UUID userId = principal != null ? principal.getId() : null;
        analyticsService.reportClientCrash(request, userId, http);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Aggregated event/crash counts for the last N days (admin)")
    public ResponseEntity<AnalyticsSummaryResponse> summary(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(analyticsService.summary(days));
    }
}
