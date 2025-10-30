package com.metrolinx.bookexchange.service;

import com.metrolinx.bookexchange.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.metrolinx.bookexchange.model.CommitteeMember;
import com.metrolinx.bookexchange.repository.BookRepository;
import com.metrolinx.bookexchange.repository.CommitteeMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommitteeMemberRepository memberRepository;

    public BookService(BookRepository bookRepository, CommitteeMemberRepository memberRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    public Book addBook(Book book, Long ownerId) {
        CommitteeMember owner = memberRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + ownerId));

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }

        book.setOwner(owner);
        return bookRepository.save(book);
    }
    
    //add a book , whihc are comma seperated, how ists howing in db?
    //if multiple genres are provided, input should be cleaned up and stored in db.
    // enum -> dropdown of predefined genres.

    @Transactional(readOnly = true)
    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<Book> getMemberBooks(Long memberId) {
        return bookRepository.findByOwnerMemberId(memberId);
    }

    @Transactional(readOnly = true)
    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<Book> byTitle = bookRepository.findByTitleContainingIgnoreCase(query);
        List<Book> byAuthor = bookRepository.findByAuthorContainingIgnoreCase(query);
        //List<Book> byGenre = bookRepository.findByGenreContaining(query);

        // Combine and remove duplicates
        return Stream.of(byTitle, byAuthor)
                .flatMap(List::stream)
                .distinct()
                .toList();
    }

    public Book updateBookAvailability(Long bookId, boolean available) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with ID: " + bookId));
        book.setAvailable(available);
        return bookRepository.save(book);
    }

    public List<Book> getAvailableBooksForMember(Long memberId) {
        return bookRepository.findAvailableBooksExcludingOwner(memberId);
    }

    //Delete the Book using its I'd
    public void deleteBook(Long bookId){

        bookRepository.deleteById(bookId);
    }

    @Transactional(readOnly = true)
    public Optional<Book> getBookById(Long id)
    {
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
}
