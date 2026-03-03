package com.neurelpress.blogs.repository;

import com.neurelpress.blogs.dao.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    Page<Book> findByCategory(String category, Pageable pageable);

    @Query("SELECT b FROM Book b ORDER BY b.referencedCount DESC")
    List<Book> findTopReferenced(Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> search(String query, Pageable pageable);
}
