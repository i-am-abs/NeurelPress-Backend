package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Quote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuoteRepository extends MongoRepository<Quote, UUID> {

    @Query("{ 'active': true }")
    List<Quote> findAllActive();
}
