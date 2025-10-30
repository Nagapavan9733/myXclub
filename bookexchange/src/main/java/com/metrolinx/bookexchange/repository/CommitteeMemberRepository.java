package com.metrolinx.bookexchange.repository;

import com.metrolinx.bookexchange.model.CommitteeMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommitteeMemberRepository extends JpaRepository<CommitteeMember , Long> {

    Optional<CommitteeMember> findByGmail(String Gmail);
    List<CommitteeMember> findByActiveTrue();
    
    Page<CommitteeMember> findAll(Pageable pageable);
    
    @Query("SELECT m FROM CommitteeMember m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.gmail) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<CommitteeMember> searchMembers(@Param("query") String query, Pageable pageable);
    
    List<CommitteeMember> findByNameContainingIgnoreCase(String name);

}
