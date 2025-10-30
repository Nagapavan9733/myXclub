package com.metrolinx.bookexchange.repository;

import com.metrolinx.bookexchange.model.BookExchange;
import com.metrolinx.bookexchange.model.ExchangeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookExchangeRepository extends JpaRepository<BookExchange , Long > {
    List<BookExchange> findByBorrowerMemberId(Long borrowerId);
    List<BookExchange> findByLenderMemberId(Long lenderId);
    List<BookExchange> findByStatus(ExchangeStatus status);
    
    List<BookExchange> findByBook_BookIdAndStatus(Long bookId, ExchangeStatus status);

    @Query("SELECT be FROM BookExchange be WHERE be.book.bookId = :bookId AND be.status IN :statuses")
    List<BookExchange> findByBook_BookIdAndStatusIn(@Param("bookId") Long bookId,
                                                    @Param("statuses") List<ExchangeStatus> statuses);

    @Query("SELECT be FROM BookExchange be WHERE be.dueDate < :dueDate AND be.status = :status")
    List<BookExchange> findByDueDateBeforeAndStatus(@Param("dueDate") LocalDateTime dueDate,
                                                    @Param("status") ExchangeStatus status);

    @Query("SELECT be FROM BookExchange be WHERE be.dueDate BETWEEN :startDate AND :endDate AND be.status = :status")
    List<BookExchange> findDueSoonExchanges(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("status") ExchangeStatus status);

    // Get pending requests for a lender
    List<BookExchange> findByLenderMemberIdAndStatus(Long lenderId, ExchangeStatus status);

    // Get all exchanges for a specific book
    List<BookExchange> findByBook_BookId(Long bookId);

    // Get exchanges by borrower and status
    List<BookExchange> findByBorrowerMemberIdAndStatus(Long borrowerId, ExchangeStatus status);

    // Get active exchanges (approved or borrowed) for a borrower
    @Query("SELECT be FROM BookExchange be WHERE be.borrower.id = :borrowerId AND be.status IN ('APPROVED', 'BORROWED')")
    List<BookExchange> findActiveExchangesByBorrower(@Param("borrowerId") Long borrowerId);

    // Get exchanges that need reminder (due in next X days)
    @Query("SELECT be FROM BookExchange be WHERE be.dueDate BETWEEN :startDate AND :endDate AND be.status = 'BORROWED'")
    List<BookExchange> findExchangesDueForReminder(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Count pending requests for a lender
    long countByLenderMemberIdAndStatus(Long lenderId, ExchangeStatus status);

    // Check if book has any active exchanges
    @Query("SELECT COUNT(be) > 0 FROM BookExchange be WHERE be.book.bookId = :bookId AND be.status IN ('REQUESTED', 'APPROVED', 'BORROWED')")
    boolean existsActiveExchangeForBook(@Param("bookId") Long bookId);

}


