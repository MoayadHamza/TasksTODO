package com.example.client.model;

import java.time.LocalDateTime;
import java.util.List;

public class PGroup {
    private Long id;
    private String name;
    private String description;
/*
    private LocalDateTime createdAt;
*/
    private User admin;
    private List<User> members;

    public PGroup(){
    }

    public PGroup(String name) {
        this.name = name;
    }

    public PGroup(Long id, User admin, String name, String description) {
        this.id = id;
        this.admin = admin;
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public List<User> getMembers() {
        return members;
    }

    public void setMembers(List<User> members) {
        this.members = members;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }*/

    /*@Override
    public String toString() {
        return name;
    }*/
}
