package com.metrolinx.bookexchange.controller;

import com.metrolinx.bookexchange.model.CommitteeMember;
import com.metrolinx.bookexchange.service.CommitteeMemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/members")
public class MemberController {
    
    private final CommitteeMemberService memberService;

    public MemberController(CommitteeMemberService memberService) {
        this.memberService = memberService;
    }

//    @GetMapping
//    public String listMembers(Model model, @RequestParam(required = false) String type) {
//        List<CommitteeMember> members;
//        if ("active".equals(type)) {
//            members = memberService.getActiveMembers();
//            model.addAttribute("filterType", "active");
//        } else {
//            members = memberService.getAllMembers();
//            model.addAttribute("filterType", "all");
//        }
//        
//        //admin check
//        model.addAttribute("isAdmin", memberService.isCurrentUserAdmin());
//        model.addAttribute("members", members);
//        return "members/list";
//    }
    
    @GetMapping
    public String listMembers(Model model,
                             @RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String type) {
        
        Page<CommitteeMember> memberPage;
        boolean isSearch = false;
        
        // Handle search vs normal listing
        if (search != null && !search.trim().isEmpty()) {
            // Search results (show all matches without pagination for now, or use pagination)
            List<CommitteeMember> members = memberService.searchMembers(search);
            model.addAttribute("members", members);
            model.addAttribute("searchQuery", search);
            model.addAttribute("isSearch", true);
        } else {
            // Normal paginated view
            Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
            
            if ("active".equals(type)) {
                // For active filter, you might need a custom repository method
                memberPage = memberService.getAllMembers(pageable); // Filter in service if needed
                model.addAttribute("filterType", "active");
            } else {
                memberPage = memberService.getAllMembers(pageable);
                model.addAttribute("filterType", "all");
            }
            
            model.addAttribute("members", memberPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", memberPage.getTotalPages());
            model.addAttribute("totalItems", memberPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("isSearch", false);
        }
        
        model.addAttribute("isAdmin", memberService.isCurrentUserAdmin());
        return "members/list";
    }

    @GetMapping("/add")
    public String showAddMemberForm(Model model) {
        model.addAttribute("member", new CommitteeMember());
        return "members/add";
    }

    @PostMapping("/add")
    public String addMember(@ModelAttribute CommitteeMember member,
                          RedirectAttributes redirectAttributes) {
        try {
            CommitteeMember savedMember = memberService.createMember(member);
            redirectAttributes.addFlashAttribute("success", "Member added successfully!");
            return "redirect:/members";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding member: " + e.getMessage());
            return "redirect:/members/add";
        }
    }

    @GetMapping("/{id}")
    public String viewMember(@PathVariable Long id, Model model) {
        Optional<CommitteeMember> member = memberService.getMemberById(id);
        if (member.isPresent()) {
        	
        	//checks for admin
        	model.addAttribute("isAdmin", memberService.isCurrentUserAdmin());
            model.addAttribute("member", member.get());
            model.addAttribute("bookCount", memberService.getMemberBookCount(id));
            return "members/view";
        } else {
            return "redirect:/members";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditMemberForm(@PathVariable Long id, Model model) {
        Optional<CommitteeMember> member = memberService.getMemberById(id);
        if (member.isPresent()) {
        	
        	//admin check
        	model.addAttribute("isAdmin", memberService.isCurrentUserAdmin());
            model.addAttribute("member", member.get());
            return "members/edit";
        } else {
            return "redirect:/members";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateMember(@PathVariable Long id,
                             @ModelAttribute CommitteeMember member,
                             RedirectAttributes redirectAttributes) {
        try {
            CommitteeMember updatedMember = memberService.updateMember(id, member);
            redirectAttributes.addFlashAttribute("success", "Member updated successfully!");
            return "redirect:/members/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating member: " + e.getMessage());
            return "redirect:/members/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/deactivate")
    public String deactivateMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            memberService.deactivateMember(id);
            redirectAttributes.addFlashAttribute("success", "Member deactivated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deactivating member: " + e.getMessage());
        }
        return "redirect:/members";
    }
}