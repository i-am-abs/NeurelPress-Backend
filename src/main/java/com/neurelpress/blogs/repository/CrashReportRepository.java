package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.CrashReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface CrashReportRepository extends JpaRepository<CrashReport, UUID> {

    long countByCreatedAtAfter(Instant since);

    long countBySourceAndCreatedAtAfter(String source, Instant since);
}
