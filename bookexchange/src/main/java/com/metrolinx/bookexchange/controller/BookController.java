package com.metrolinx.bookexchange.controller;

import org.springframework.security.core.Authentication;
import com.metrolinx.bookexchange.model.Book;
import com.metrolinx.bookexchange.model.CommitteeMember;
import com.metrolinx.bookexchange.service.BookService;
import com.metrolinx.bookexchange.service.CommitteeMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.*;


import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
public class BookController {
    
    private final BookService bookService;
    private final CommitteeMemberService memberService;

    public BookController(BookService bookService, CommitteeMemberService memberService) {
        this.bookService = bookService;
        this.memberService = memberService;
    }

    @GetMapping
    public String listBooks(Model model,
                           @RequestParam(required = false) String search,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           Authentication authentication) {
        
        Page<Book> bookPage;
        
        if (search != null && !search.trim().isEmpty()) {
            // For search, we'll still show all matching books without pagination
            List<Book> books = bookService.searchBooks(search);
            model.addAttribute("searchQuery", search);
            model.addAttribute("books", books);
            model.addAttribute("isSearch", true);
        } 
        else {
            // Normal view with pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            bookPage = bookService.getAllBooks(pageable);
            
            model.addAttribute("books", bookPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookPage.getTotalPages());
            model.addAttribute("totalItems", bookPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("isSearch", false);
        }
        
        // Get current user for request modal
        if (authentication != null) {
            String currentUserEmail = authentication.getName();
            Optional<CommitteeMember> currentMember = memberService.getMemberByEmail(currentUserEmail);
            currentMember.ifPresent(member -> model.addAttribute("currentMember", member));
        }
        
        model.addAttribute("isAdmin", memberService.isCurrentUserAdmin());   //ADMIN
        return "books/list";
    }

    @GetMapping("/add")
    public String showAddBookForm(Model model, Authentication authentication) {
        // Get current logged-in user (always exists due to Spring Security)
        String currentUserEmail = authentication.getName();
        CommitteeMember currentMember = memberService.getMemberByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Member not found for email: " + currentUserEmail));
        
        model.addAttribute("currentMember", currentMember);
        model.addAttribute("book", new Book());
        return "books/add";
    }
    
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, 
                         @RequestParam Long ownerId,
                         RedirectAttributes redirectAttributes) {
        try {
            Book savedBook = bookService.addBook(book, ownerId);
            redirectAttributes.addFlashAttribute("success", "Book added successfully!");
            return "redirect:/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding book: " + e.getMessage());
            return "redirect:/books/add";
        }
    }

    @GetMapping("/{id}")
    public String viewBook(@PathVariable Long id, Model model, Authentication authentication) {
        Optional<Book> book = bookService.getBookById(id);
        if (book.isPresent()) {
            model.addAttribute("book", book.get());
            model.addAttribute("isAdmin", memberService.isCurrentUserAdmin()); //Admin Check
            
            // fetch current user for request functionality
            if (authentication != null) {
                String currentUserEmail = authentication.getName();
                Optional<CommitteeMember> currentMember = memberService.getMemberByEmail(currentUserEmail);
                currentMember.ifPresent(member -> model.addAttribute("currentMember", member));
            }
            
            return "books/view";
        } else {
            return "redirect:/books";
        }
    }

    @PostMapping("/{id}/availability")
    public String updateAvailability(@PathVariable Long id,
                                   @RequestParam boolean available,
                                   RedirectAttributes redirectAttributes) {
        try {
            bookService.updateBookAvailability(id, available);
            redirectAttributes.addFlashAttribute("success", "Book availability updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating availability: " + e.getMessage());
        }
        return "redirect:/books/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Book deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting book: " + e.getMessage());
        }
        return "redirect:/books";
    }
}