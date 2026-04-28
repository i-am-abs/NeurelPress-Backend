package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.dao.CrashReport;
import com.neurelpress.blogs.dao.UsageEvent;
import com.neurelpress.blogs.dto.request.CrashReportRequest;
import com.neurelpress.blogs.dto.request.UsageEventRequest;
import com.neurelpress.blogs.dto.response.AnalyticsSummaryResponse;
import com.neurelpress.blogs.repository.CrashReportRepository;
import com.neurelpress.blogs.repository.UsageEventRepository;
import com.neurelpress.blogs.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int MAX_STACK_LEN = 7_500;
    private static final int MAX_MESSAGE_LEN = 256;
    private static final String SOURCE_SERVER = "server";
    private static final String SOURCE_CLIENT = "client";

    private final UsageEventRepository usageEventRepository;
    private final CrashReportRepository crashReportRepository;

    @Override
    @Async
    @Transactional
    public void trackEvent(@NonNull UsageEventRequest request,
                           @Nullable UUID userId,
                           @Nullable HttpServletRequest http) {
        UsageEvent event = UsageEvent.builder()
                .eventName(request.eventName())
                .entityType(request.entityType())
                .entityId(request.entityId())
                .sessionId(request.sessionId())
                .path(request.path())
                .referrer(request.referrer())
                .device(request.device())
                .metadata(request.metadata())
                .userId(userId)
                .country(http != null ? http.getHeader("X-Country") : null)
                .build();
        usageEventRepository.save(event);
    }

    @Override
    @Async
    @Transactional
    public void reportClientCrash(@NonNull CrashReportRequest request,
                                  @Nullable UUID userId,
                                  @Nullable HttpServletRequest http) {
        CrashReport report = CrashReport.builder()
                .source(SOURCE_CLIENT)
                .message(truncate(request.message(), MAX_MESSAGE_LEN))
                .exceptionType(request.exceptionType())
                .stackTrace(truncate(request.stackTrace(), MAX_STACK_LEN))
                .requestPath(request.path())
                .sessionId(request.sessionId())
                .release(request.release())
                .userAgent(request.userAgent() != null ? request.userAgent()
                        : (http != null ? http.getHeader("User-Agent") : null))
                .userId(userId)
                .build();
        crashReportRepository.save(report);
        log.warn("Client crash captured: {} on {}", report.getMessage(), report.getRequestPath());
    }

    @Override
    @Async
    @Transactional
    public void reportServerCrash(@NonNull Throwable error,
                                  @Nullable HttpServletRequest http,
                                  int status) {
        CrashReport report = CrashReport.builder()
                .source(SOURCE_SERVER)
                .message(truncate(error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName(), MAX_MESSAGE_LEN))
                .exceptionType(error.getClass().getName())
                .stackTrace(truncate(stack(error), MAX_STACK_LEN))
                .requestPath(http != null ? http.getRequestURI() : null)
                .requestMethod(http != null ? http.getMethod() : null)
                .httpStatus(String.valueOf(status))
                .userAgent(http != null ? http.getHeader("User-Agent") : null)
                .build();
        crashReportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse summary(int days) {
        Instant since = Instant.now().minus(Math.max(days, 1), ChronoUnit.DAYS);
        long totalEvents = usageEventRepository.countByCreatedAtAfter(since);
        long totalCrashes = crashReportRepository.countByCreatedAtAfter(since);

        Map<String, Long> eventsByName = new LinkedHashMap<>();
        List<AnalyticsSummaryResponse.TopEvent> topEvents = usageEventRepository.countByEventSince(since)
                .stream()
                .map(p -> {
                    eventsByName.put(p.getName(), p.getCount());
                    return new AnalyticsSummaryResponse.TopEvent(p.getName(), p.getCount());
                })
                .toList();

        return new AnalyticsSummaryResponse(totalEvents, totalCrashes, eventsByName, topEvents);
    }

    private static @NonNull String stack(@NonNull Throwable t) {
        try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        } catch (Exception e) {
            return t.toString();
        }
    }

    private static @Nullable String truncate(@Nullable String input, int max) {
        if (input == null) return null;
        return input.length() <= max ? input : input.substring(0, max);
    }
}
