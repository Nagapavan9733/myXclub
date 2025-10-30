package com.metrolinx.bookexchange.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name ="books")
public class Book {

    //Book entity has id,title,author,genre,description,owner,available,borrowcount,average rating
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    private String genre;
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private CommitteeMember owner;

    @Column(nullable = false)
    private boolean available = true;

    @Column(name = "borrow_count")
    private int borrowCount = 0;

    @Column(name = "average_rating")
    private Double averageRating;

    //Collecting from the ratings Map to persist with the foreignkey as book_id
    @ElementCollection
    @CollectionTable(name = "book_ratings", joinColumns = @JoinColumn(name = "book_id"))
    @MapKeyColumn(name = "member_id")
    @Column(name = "rating")
    private Map<Long, Integer> ratings = new HashMap<>();

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    // Constructors
    public Book() {

    }

    public Book(String title, String author, CommitteeMember owner) {
        this.title = title;
        this.author = author;
        this.owner = owner;
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    // Domain methods
    public void addRating(Long memberId, int rating) {
        ratings.put(memberId, rating);
        updateAverageRating();
    }

    //Increments the Borrowed count
    public void incrementBorrowCount() {
        this.borrowCount++;
    }

    public boolean canBeBorrowed() {
        return available && owner != null && owner.isActive();
    }

    private void updateAverageRating() {
        if (!ratings.isEmpty()) {
            this.averageRating = ratings.values().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
        }
    }


    //Getter and Setters
    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long id) {
        this.bookId = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }


    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CommitteeMember getOwner() {
        return owner;
    }

    public void setOwner(CommitteeMember owner) {
        this.owner = owner;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(int borrowCount) {
        this.borrowCount = borrowCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Map<Long, Integer> getRatings() {
        return ratings;
    }

    public void setRatings(Map<Long, Integer> ratings) {
        this.ratings = ratings;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    //ToString
    @Override
    public String toString() {
        return "Book{" +
                "id=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", description='" + description + '\'' +
                ", owner=" + owner +
                ", available=" + available +
                ", borrowCount=" + borrowCount +
                ", averageRating=" + averageRating +
                ", ratings=" + ratings +
                ", createdDate=" + createdDate +
                '}';
    }
}
