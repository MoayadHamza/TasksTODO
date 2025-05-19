package com.example.client.model;


public class Task {

    private Long id; // auto-generated

    private String title;
    private String description;


    private TaskStatus completed;


    private ImportanceLevel importance;
    private User user;
    private PGroup group;
    private Project project;

    // Constructors
    public Task() {}

    public Task(String title, String description, ImportanceLevel importance) {
        this.title = title;
        this.description = description;
        this.completed = TaskStatus.TODO;
        this.importance = importance;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus isCompleted() { return completed; }
    public TaskStatus getCompleted() { return completed; }
    public ImportanceLevel getImportance() { return importance; }
    //    public LocalDateTime getCreatedAt() { return createdAt; }
    public User getUser() { return user; }
    public PGroup getGroup() { return group; }
    public Project getProject() { return project; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCompleted(TaskStatus completed) { this.completed = completed; }
    public void setImportance(ImportanceLevel importance) { this.importance = importance; }
    public void setUser(User user) { this.user = user; }
    public void setGroup(PGroup group) { this.group = group; }
    public void setProject(Project project) { this.project = project; }


}
