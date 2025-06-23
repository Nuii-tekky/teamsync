package com.teamsync.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TaskDTO {
    private UUID id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDate;
    private UserSummary assigner;
    private UserSummary assignee;

    @Data
    public static class UserSummary {
        private UUID id;
        private String email;
        private String firstName;
        private String lastName;
    }
}