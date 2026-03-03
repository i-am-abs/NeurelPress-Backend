package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, UUID> {

    @Query("SELECT q FROM Quote q WHERE q.active = true")
    List<Quote> findAllActive();
}
