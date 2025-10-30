package com.metrolinx.bookexchange.controller;

import com.metrolinx.bookexchange.model.Book;
import com.metrolinx.bookexchange.model.BookExchange;
import com.metrolinx.bookexchange.model.CommitteeMember;
import com.metrolinx.bookexchange.service.BookExchangeService;
import com.metrolinx.bookexchange.service.BookService;
import com.metrolinx.bookexchange.service.CommitteeMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    
    private final BookService bookService;
    private final CommitteeMemberService memberService;
    private final BookExchangeService exchangeService;

    public DashboardController(BookService bookService, 
                             CommitteeMemberService memberService,
                             BookExchangeService exchangeService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.exchangeService = exchangeService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Gets statistics for dashboard 
            List<Book> availableBooks = bookService.getAvailableBooks();
            List<CommitteeMember> activeMembers = memberService.getActiveMembers();
            
            // Force initialization of member data
            List<CommitteeMember> initializedMembers = activeMembers.stream()
                .map(member -> {
                    // Access properties to force initialization
                    member.getName();
                    member.getGmail();
                    return member;
                })
                .collect(Collectors.toList());
            
            List<BookExchange> pendingRequests = exchangeService.getAllExchanges()
                .stream()
                .filter(ex -> "REQUESTED".equals(ex.getStatus().toString()))
                .collect(Collectors.toList());
                
            List<BookExchange> overdueExchanges = exchangeService.getOverdueExchanges();
            
            // Add data to model
            model.addAttribute("totalBooks", availableBooks.size());
            model.addAttribute("activeMembers", activeMembers.size());
            model.addAttribute("pendingRequests", pendingRequests.size());
            model.addAttribute("overdueBooks", overdueExchanges.size());
            model.addAttribute("recentBooks", availableBooks.stream().limit(5).collect(Collectors.toList()));
            model.addAttribute("recentMembers", initializedMembers.stream().limit(5).collect(Collectors.toList()));
            
            return "dashboard";
        } catch (Exception e) {
            // Fallback if there's an error
            model.addAttribute("totalBooks", 0);
            model.addAttribute("activeMembers", 0);
            model.addAttribute("pendingRequests", 0);
            model.addAttribute("overdueBooks", 0);
            model.addAttribute("recentBooks", List.of());
            model.addAttribute("recentMembers", List.of());
            return "dashboard";
        }
    }
}