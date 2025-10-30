package com.metrolinx.bookexchange.service;

import com.metrolinx.bookexchange.model.CommitteeMember;
import com.metrolinx.bookexchange.repository.CommitteeMemberRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CommitteeMemberService {

    @Autowired
    private CommitteeMemberRepository memberRepo;

    public CommitteeMemberService(CommitteeMemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    public CommitteeMember createMember(CommitteeMember member)
    {
        //Checking if the input member object has the Gmail or not
        if(member.getGmail() == null || member.getGmail().trim().isEmpty()){
            throw new IllegalArgumentException("Member Gmail is required.");
        }

        //Check if Email already Exists
        Optional<CommitteeMember> existingMember = memberRepo.findByGmail(member.getGmail());

        if (existingMember.isPresent()) {
            throw new IllegalArgumentException("Member with email " + member.getGmail() + " already exists");
        }

        return memberRepo.save(member);
    }

    //Gets all the committee Members
//    @Transactional(readOnly = true)
//    public List<CommitteeMember> getAllMembers() {
//        return memberRepo.findAll();
//    }
    
    //Gets all the committee Members in pageable
    @Transactional(readOnly = true)
    public Page<CommitteeMember> getAllMembers(Pageable pageable) {
        return memberRepo.findAll(pageable);
    }

    //Finding the Committee Member by I'd
    @Transactional(readOnly = true)
    public Optional<CommitteeMember> getMemberById(Long id) {
        return memberRepo.findById(id);
    }

    //Get member by his gmail
    @Transactional(readOnly = true)
    public Optional<CommitteeMember> getMemberByEmail(String email) {
        return memberRepo.findByGmail(email);
    }

    @Transactional(readOnly = true)
    public List<CommitteeMember> getActiveMembers() {
        return memberRepo.findByActiveTrue();
    }

    //Deactivating the Member
    public void deactivateMember(Long id) {
        CommitteeMember member = memberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + id));

        member.setActive(false);
        memberRepo.save(member);
    }

    //gets the member book count
    public int getMemberBookCount(Long memberId) {
        CommitteeMember member = memberRepo.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));

        return member.getOwnedBooks().size();
    }

    //Update the Member
    public CommitteeMember updateMember(Long id, CommitteeMember memberDetails) {
        CommitteeMember member = memberRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + id));

        // Business logic for update
        if (memberDetails.getName() != null) {
            member.setName(memberDetails.getName());
        }

        if (memberDetails.getGmail() != null) {
            // Check if new email is unique
            Optional<CommitteeMember> existing = memberRepo.findByGmail(memberDetails.getGmail());

            //checks if someone else has the same email
            if (existing.isPresent() && existing.get().getMemberId() != id) {
                throw new IllegalArgumentException("Email already exists");
            }

            member.setGmail(memberDetails.getGmail());
        }
        if (memberDetails.isActive() != member.isActive()) {
            member.setActive(memberDetails.isActive());
        }

        return memberRepo.save(member);
    }

    
    //checks for admin
    public boolean isCurrentUserAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }
            
            String currentEmail = authentication.getName();
            Optional<CommitteeMember> currentMember = memberRepo.findByGmail(currentEmail);
            
            return currentMember.map(CommitteeMember::isAdmin).orElse(false);
            
        } catch (Exception e) {
            
        	// Log error and return false for safety
            System.err.println("Error checking admin status: " + e.getMessage());
            return false;
        }
    }
    
    
    //search Members
    @Transactional(readOnly = true)
    public Page<CommitteeMember> searchMembers(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return memberRepo.findAll(pageable);
        }
        return memberRepo.searchMembers(query.trim(), pageable);
    }
    
    @Transactional(readOnly = true)
    public List<CommitteeMember> searchMembers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return memberRepo.findAll();
        }
        return memberRepo.findByNameContainingIgnoreCase(query.trim());
    }
    


}
