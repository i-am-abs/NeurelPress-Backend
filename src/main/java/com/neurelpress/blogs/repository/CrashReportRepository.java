package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.CrashReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface CrashReportRepository extends MongoRepository<CrashReport, UUID> {

    long countByCreatedAtAfter(Instant since);
}
