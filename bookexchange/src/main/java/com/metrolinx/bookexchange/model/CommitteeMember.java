package com.metrolinx.bookexchange.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "committee_members")
public class CommitteeMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false , unique = true)
    private String gmail;

    //Each member can have multiple books
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Book> ownedBooks = new ArrayList<>();

    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "is_admin")
    private boolean isAdmin = false;

    public CommitteeMember()
    {
        //default constructor
    }

    public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	//parameterized Constructor
    public CommitteeMember(String name, String gmail) {
        this.name = name;
        this.gmail = gmail;
    }

    //Getters and Setters

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public List<Book> getOwnedBooks() {
        return ownedBooks;
    }

    public void setOwnedBooks(List<Book> ownedBooks) {
        this.ownedBooks = ownedBooks;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "CommitteeMember{" +
                "id=" + memberId +
                ", name='" + name + '\'' +
                ", gmail='" + gmail + '\'' +
                ", ownedBooks=" + ownedBooks +
                ", active=" + active +
                '}';
    }

}
