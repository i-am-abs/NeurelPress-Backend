package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, UUID> {

    @Query("SELECT u.eventName as name, COUNT(u) as count FROM UsageEvent u " +
            "WHERE u.createdAt >= :since GROUP BY u.eventName ORDER BY COUNT(u) DESC")
    List<EventCount> countByEventSince(@Param("since") Instant since);

    long countByCreatedAtAfter(Instant since);

    long countByEventNameAndCreatedAtAfter(String eventName, Instant since);

    interface EventCount {
        String getName();
        long getCount();
    }
}
