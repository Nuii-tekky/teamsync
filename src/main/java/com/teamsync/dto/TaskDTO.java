package com.teamsync.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TaskDTO {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummary assignee;
    private UserSummary assigner;
    private List<StatusHistory> statusHistory;

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public UserSummary getAssignee() { return assignee; }
    public void setAssignee(UserSummary assignee) { this.assignee = assignee; }
    public UserSummary getAssigner() { return assigner; }
    public void setAssigner(UserSummary assigner) { this.assigner = assigner; }
    public List<StatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    public static class UserSummary {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class StatusHistory {
        private LocalDateTime timestamp;
        private String status;
        private UUID updatedBy;
        private UUID taskId; // Added to replace the full Task object

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public UUID getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
        public UUID getTaskId() { return taskId; }
        public void setTaskId(UUID taskId) { this.taskId = taskId; }
    }
}