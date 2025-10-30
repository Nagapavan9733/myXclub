package com.metrolinx.bookexchange.service;

import com.metrolinx.bookexchange.model.Book;
import com.metrolinx.bookexchange.model.BookExchange;
import com.metrolinx.bookexchange.model.CommitteeMember;
import com.metrolinx.bookexchange.model.ExchangeStatus;
import com.metrolinx.bookexchange.repository.BookExchangeRepository;
import com.metrolinx.bookexchange.repository.BookRepository;
import com.metrolinx.bookexchange.repository.CommitteeMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class BookExchangeService {
    private final BookExchangeRepository exchangeRepository;
    private final BookRepository bookRepository;
    private final CommitteeMemberRepository memberRepository;
    //private final NotificationService notificationService;

    public BookExchangeService(BookExchangeRepository exchangeRepository,
                               BookRepository bookRepository,
                               CommitteeMemberRepository memberRepository) {
        this.exchangeRepository = exchangeRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        //this.notificationService = notificationService;
    }

    public BookExchange requestBook(Long bookId, Long borrowerId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));

        CommitteeMember borrower = memberRepository.findById(borrowerId)
                .orElseThrow(() -> new RuntimeException("Borrower not found with ID: " + borrowerId));

        if (!book.isAvailable()) {
            throw new RuntimeException("Book is not available for exchange");
        }

        if (Objects.equals(book.getOwner().getMemberId(), borrowerId)) {
            throw new RuntimeException("Cannot borrow your own book");
        }

        // Check for existing pending requests
        List<BookExchange> existingRequests = exchangeRepository.findByBook_BookIdAndStatusIn(
                bookId, Arrays.asList(ExchangeStatus.REQUESTED, ExchangeStatus.APPROVED, ExchangeStatus.BORROWED)
        );

        if (!existingRequests.isEmpty()) {
            throw new RuntimeException("There is already a pending request for this book");
        }

        BookExchange exchange = new BookExchange(book, borrower);
        BookExchange savedExchange = exchangeRepository.save(exchange);

//        notificationService.sendExchangeRequestNotification(savedExchange);
        return savedExchange;
    }
    
// // Gets count of members currently borrowing this book
//    public int getCurrentlyReadingCount(Long bookId) {
//        List<BookExchange> activeExchanges = exchangeRepository.findByBook_BookIdAndStatusIn(
//            bookId, Arrays.asList(ExchangeStatus.APPROVED, ExchangeStatus.BORROWED)
//        );
//        return activeExchanges.size();
//    }
//
//    // Gets count of members in wait list (pending requests)
//    public int getWaitlistCount(Long bookId) {
//        List<BookExchange> pendingRequests = exchangeRepository.findByBook_BookIdAndStatus(
//            bookId, ExchangeStatus.REQUESTED
//        );
//        return pendingRequests.size();
//    }

    public BookExchange approveExchange(Long exchangeId) {
        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange request not found with ID: " + exchangeId));

        if (exchange.getStatus() != ExchangeStatus.REQUESTED) {
            throw new RuntimeException("Exchange request is not in requested state");
        }

        exchange.setStatus(ExchangeStatus.APPROVED);
        exchange.setApprovalDate(LocalDateTime.now());
        exchange.setDueDate(LocalDateTime.now().plusWeeks(2));

        // Mark book as unavailable
        Book book = exchange.getBook();
        book.setAvailable(false);
        bookRepository.save(book);

        BookExchange savedExchange = exchangeRepository.save(exchange);
//        notificationService.sendExchangeApprovalNotification(savedExchange);

        return savedExchange;
    }

    public BookExchange rejectExchange(Long exchangeId) {
        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange request not found with ID: " + exchangeId));

        exchange.setStatus(ExchangeStatus.REJECTED);
        return exchangeRepository.save(exchange);
    }

    public BookExchange markAsBorrowed(Long exchangeId) {
        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not found with ID: " + exchangeId));

        if (exchange.getStatus() != ExchangeStatus.APPROVED) {
            throw new RuntimeException("Exchange must be approved first");
        }

        exchange.setStatus(ExchangeStatus.BORROWED);
        return exchangeRepository.save(exchange);
    }

    public BookExchange returnBook(Long exchangeId) {
        BookExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new RuntimeException("Exchange not found with ID: " + exchangeId));

        exchange.setStatus(ExchangeStatus.RETURNED);
        exchange.setReturnDate(LocalDateTime.now());

        // Mark book as available again and update borrow count
        Book book = exchange.getBook();
        book.setAvailable(true);
        book.setBorrowCount(book.getBorrowCount() + 1);
        bookRepository.save(book);

        return exchangeRepository.save(exchange);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getMemberBorrowedBooks(Long memberId) {
        return exchangeRepository.findByBorrowerMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getMemberLentBooks(Long memberId) {
        return exchangeRepository.findByLenderMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getOverdueExchanges() {
        return exchangeRepository.findByDueDateBeforeAndStatus(LocalDateTime.now(), ExchangeStatus.BORROWED);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getDueSoonExchanges(int daysBefore) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(daysBefore);
        return exchangeRepository.findDueSoonExchanges(startDate, endDate, ExchangeStatus.BORROWED);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getPendingRequestsForLender(Long lenderId) {
        return exchangeRepository.findByLenderMemberIdAndStatus(lenderId, ExchangeStatus.REQUESTED);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getExchangeHistoryForBook(Long bookId) {
        return exchangeRepository.findByBook_BookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getActiveExchangesForBorrower(Long borrowerId) {
        return exchangeRepository.findActiveExchangesByBorrower(borrowerId);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getExchangesDueForReminder(int daysBeforeDue) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().plusDays(daysBeforeDue);
        return exchangeRepository.findExchangesDueForReminder(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public long getPendingRequestCountForLender(Long lenderId) {
        return exchangeRepository.countByLenderMemberIdAndStatus(lenderId, ExchangeStatus.REQUESTED);
    }

    @Transactional(readOnly = true)
    public boolean hasActiveExchange(Long bookId) {
        return exchangeRepository.existsActiveExchangeForBook(bookId);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getBorrowerExchangesByStatus(Long borrowerId, ExchangeStatus status) {
        return exchangeRepository.findByBorrowerMemberIdAndStatus(borrowerId, status);
    }

    @Transactional(readOnly = true)
    public List<BookExchange> getAllExchanges() {
        return exchangeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<BookExchange> getExchangeById(Long id) {
        return exchangeRepository.findById(id);
    }

}
