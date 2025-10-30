package com.metrolinx.bookexchange.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

//Exchange  Status Entity has the id, book_id, borrower_id, lender_id, requested_date, approved_date, due_date,n return date
@Entity
@Table(name = "book_exchanges")
public class BookExchange {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exchangeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private CommitteeMember borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lender_id", nullable = false)
    private CommitteeMember lender;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status = ExchangeStatus.REQUESTED;

    //Default Constructor
    public BookExchange() {}

    //Parameterized Constructor
    public BookExchange(Book book, CommitteeMember borrower) {
        this.book = book;
        this.borrower = borrower;
        this.lender = book.getOwner();
    }

    @PrePersist
    protected void onCreate() {
        requestDate = LocalDateTime.now();
    }

    //Getters and Setters
    public Long getId() {
        return exchangeId;
    }

    public void setId(Long id) {
        this.exchangeId = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public CommitteeMember getBorrower() {
        return borrower;
    }

    public void setBorrower(CommitteeMember borrower) {
        this.borrower = borrower;
    }

    public CommitteeMember getLender() {
        return lender;
    }

    public void setLender(CommitteeMember lender) {
        this.lender = lender;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDateTime returnDate) {
        this.returnDate = returnDate;
    }

    public ExchangeStatus getStatus() {
        return status;
    }

    public void setStatus(ExchangeStatus status) {
        this.status = status;
    }
}
