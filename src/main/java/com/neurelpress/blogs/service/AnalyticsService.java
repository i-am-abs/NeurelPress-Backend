package com.neurelpress.blogs.service;

import com.neurelpress.blogs.dto.request.CrashReportRequest;
import com.neurelpress.blogs.dto.request.UsageEventRequest;
import com.neurelpress.blogs.dto.response.AnalyticsSummaryResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;


public interface AnalyticsService {

    void trackEvent(UsageEventRequest request, UUID userId, HttpServletRequest http);

    void reportClientCrash(CrashReportRequest request, UUID userId, HttpServletRequest http);

    void reportServerCrash(Throwable error, HttpServletRequest http, int status);

    AnalyticsSummaryResponse summary(int days);
}
