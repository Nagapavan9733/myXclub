package com.metrolinx.bookexchange.controller;

import com.metrolinx.bookexchange.model.BookExchange;
import com.metrolinx.bookexchange.model.ExchangeStatus;
import com.metrolinx.bookexchange.service.BookExchangeService;
import com.metrolinx.bookexchange.service.BookService;
import com.metrolinx.bookexchange.service.CommitteeMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/exchanges")
public class ExchangeController {
    
    private final BookExchangeService exchangeService;
    private final BookService bookService;
    private final CommitteeMemberService memberService;

    public ExchangeController(BookExchangeService exchangeService,
                            BookService bookService,
                            CommitteeMemberService memberService) {
        this.exchangeService = exchangeService;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    @GetMapping
    public String listExchanges(Model model) {
        List<BookExchange> exchanges = exchangeService.getAllExchanges();
        model.addAttribute("exchanges", exchanges);
        return "exchanges/list";
    }

    @GetMapping("/pending")
    public String pendingRequests(Model model) {
        List<BookExchange> pendingRequests = exchangeService.getAllExchanges()
            .stream()
            .filter(ex -> ex.getStatus() == ExchangeStatus.REQUESTED)
            .toList();
        model.addAttribute("requests", pendingRequests);
        return "exchanges/pending";
    }

    @GetMapping("/borrowed")
    public String borrowedBooks(Model model) {
        List<BookExchange> borrowedBooks = exchangeService.getAllExchanges()
            .stream()
            .filter(ex -> ex.getStatus() == ExchangeStatus.BORROWED || ex.getStatus() == ExchangeStatus.APPROVED)
            .toList();
        model.addAttribute("borrowedBooks", borrowedBooks);
        return "exchanges/borrowed";
    }

    @PostMapping("/request")
    public String requestBook(@RequestParam Long bookId,
                            @RequestParam Long borrowerId,
                            RedirectAttributes redirectAttributes) {
        try {
            BookExchange exchange = exchangeService.requestBook(bookId, borrowerId);
            redirectAttributes.addFlashAttribute("success", "Book request submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error requesting book: " + e.getMessage());
        }
        return "redirect:/books";
    }

    @PostMapping("/{id}/approve")
    public String approveExchange(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            BookExchange exchange = exchangeService.approveExchange(id);
            redirectAttributes.addFlashAttribute("success", "Exchange approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error approving exchange: " + e.getMessage());
        }
        return "redirect:/exchanges/pending";
    }

    @PostMapping("/{id}/reject")
    public String rejectExchange(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            BookExchange exchange = exchangeService.rejectExchange(id);
            redirectAttributes.addFlashAttribute("success", "Exchange rejected successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error rejecting exchange: " + e.getMessage());
        }
        return "redirect:/exchanges/pending";
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            BookExchange exchange = exchangeService.returnBook(id);
            redirectAttributes.addFlashAttribute("success", "Book returned successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error returning book: " + e.getMessage());
        }
        return "redirect:/exchanges/borrowed";
    }

    @GetMapping("/{id}")
    public String viewExchange(@PathVariable Long id, Model model) {
        Optional<BookExchange> exchange = exchangeService.getExchangeById(id);
        if (exchange.isPresent()) {
            model.addAttribute("exchange", exchange.get());
            return "exchanges/view";
        } else {
            return "redirect:/exchanges";
        }
    }
}