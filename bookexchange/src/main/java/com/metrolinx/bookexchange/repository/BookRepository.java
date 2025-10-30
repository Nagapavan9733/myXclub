package com.metrolinx.bookexchange.repository;

import com.metrolinx.bookexchange.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long > {
    List<Book> findByAvailableTrue();
    List<Book> findByOwnerMemberId(Long ownerMemberId);
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);

    @Query("SELECT b FROM Book b WHERE b.available = true AND b.owner.memberId != :ownerMemberId")
    List<Book> findAvailableBooksExcludingOwner(@Param("ownerMemberId") Long ownerMemberId);

    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%')) AND b.available = true")
    List<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre);

    
    Page<Book> findAll(Pageable pageable);
    
}

