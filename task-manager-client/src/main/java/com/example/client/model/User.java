package com.example.client.model;

import java.util.List;

public class User {
    private Long id;
    private String fullName;
    private String username;
    private String password; // transient avoids serialization if needed later
    private List<PGroup> groups;

    public User() {}

    public User(Long id, String fullName, String username, String password) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
    }

    public User(Long id, String fullName, String username) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public List<PGroup> getGroups() {return groups;}
    public void setGroups(List<PGroup> groups) {this.groups = groups;}

    /*@Override
    public String toString() {
        return this.fullName;
    }*/
}
