package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends MongoRepository<Book, UUID> {

    Page<Book> findByCategory(String category, Pageable pageable);

    @Query(value = "{}", sort = "{ 'referencedCount': -1 }")
    List<Book> findTopReferenced(Pageable pageable);

    @Query("{ '$or': [ { 'title': { '$regex': ?0, '$options': 'i' } }, { 'author': { '$regex': ?0, '$options': 'i' } } ] }")
    Page<Book> search(String query, Pageable pageable);
}
