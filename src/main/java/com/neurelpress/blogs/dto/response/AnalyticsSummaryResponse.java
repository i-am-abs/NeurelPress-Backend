package com.neurelpress.blogs.dto.response;

import java.util.List;
import java.util.Map;

public record AnalyticsSummaryResponse(
        long totalEvents,
        long totalCrashes,
        Map<String, Long> eventsByName,
        List<TopEvent> topEvents
) {
    public record TopEvent(String name, long count) {}
}
