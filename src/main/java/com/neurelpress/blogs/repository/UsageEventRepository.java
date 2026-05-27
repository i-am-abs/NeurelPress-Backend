package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.UsageEvent;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface UsageEventRepository extends MongoRepository<UsageEvent, UUID> {

    @Aggregation(pipeline = {
            "{ '$match': { 'createdAt': { '$gte': ?0 } } }",
            "{ '$group': { '_id': '$eventName', 'count': { '$sum': 1 } } }",
            "{ '$sort': { 'count': -1 } }",
            "{ '$project': { '_id': 0, 'name': '$_id', 'count': 1 } }"
    })
    List<EventCount> countByEventSince(Instant since);

    long countByCreatedAtAfter(Instant since);

    interface EventCount {
        String getName();
        long getCount();
    }
}
