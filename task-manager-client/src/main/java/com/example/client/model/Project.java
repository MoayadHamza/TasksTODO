package com.example.client.model;

import java.time.LocalDateTime;

public class Project {
    private Long id;
    private String name;
    private String description;
/*
    private LocalDateTime createdAt;
*/
    private User ownerUser;   // optional: personal project
    private PGroup ownerGroup; // optional: group project

    public Project() { }
    public Project(Long id, String name, String description, User user, PGroup group) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerUser = user;
        this.ownerGroup = group;
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

    public User getOwnerUser() {
        return ownerUser;
    }

    public void setOwnerUser(User ownerUser) {
        this.ownerUser = ownerUser;
    }

    public PGroup getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(PGroup ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

/*    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }*/

    @Override
    public String toString() {
        return name;
    }
}
