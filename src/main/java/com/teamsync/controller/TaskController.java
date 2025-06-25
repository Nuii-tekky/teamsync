package com.teamsync.controller;

import com.teamsync.dto.ApiResponse;
import com.teamsync.dto.TaskDTO;
import com.teamsync.entity.Task;
import com.teamsync.entity.User;
import com.teamsync.exceptions.ResourceNotFoundException;
import com.teamsync.service.TaskService;
import com.teamsync.service.UserService; // Added import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger logger = Logger.getLogger(TaskController.class.getName());

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService; // Added autowiring

    @PostMapping("/users/{assigneeId}")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@RequestAttribute("userId") UUID assignerId, @PathVariable UUID assigneeId, @RequestBody Task task) {
        logger.info("Admin creating task for assigner ID: " + assignerId + ", assignee ID: " + assigneeId);
        TaskDTO createdTask = taskService.createTask(assignerId, assigneeId, task);
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", createdTask));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<TaskListDTO>> getTasksByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching tasks for user ID: " + userId + " (page=" + page + ", size=" + size + ")");
        List<TaskDTO> tasks = taskService.getTasksByUserId(userId, page, size);
        TaskListDTO response = new TaskListDTO();
        response.setTasks(tasks);
        response.setTotalCount((int) taskService.countTasksByUserId(userId));
        response.setLastUpdated(LocalDateTime.now());
        response.setCurrentPage(page);
        response.setTotalPages(calculateTotalPages(response.getTotalCount(), size));
        response.setHasNext(page + 1 < response.getTotalPages());
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDetailsDTO>> getTaskById(@PathVariable UUID id) {
        logger.info("Fetching task with ID: " + id);
        Task task = taskService.getTaskById(id);
        TaskDetailsDTO details = new TaskDetailsDTO();
        details.setTask(taskService.convertToDTO(task, task.getAssignee()));
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", details));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(@PathVariable UUID id, @RequestBody Task taskDetails) {
        logger.info("Updating task with ID: " + id);
        Task updatedTask = taskService.updateTask(id, taskDetails);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.convertToDTO(updatedTask, getCurrentUser())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        logger.info("Deleting task with ID: " + id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    private User getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ResourceNotFoundException("Authentication context not found");
        }
        UUID userId;
        try {
            userId = (UUID) auth.getPrincipal(); // Assumes principal is UUID from JWT
        } catch (ClassCastException e) {
            throw new ResourceNotFoundException("Invalid user ID format in authentication");
        }
        return userService.getUserById(userId) // Corrected to use userService
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found with id: " + userId));
    }

    private int calculateTotalPages(int totalCount, int size) {
        return (int) Math.ceil((double) totalCount / size);
    }
}

class TaskListDTO {
    private List<TaskDTO> tasks;
    private int totalCount;
    private LocalDateTime lastUpdated;
    private int currentPage;
    private int totalPages;
    private boolean hasNext;

    // Getters and setters
    public List<TaskDTO> getTasks() { return tasks; }
    public void setTasks(List<TaskDTO> tasks) { this.tasks = tasks; }
    public int getTotalCount() { return totalCount; }
    public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}

class TaskDetailsDTO {
    private TaskDTO task;

    // Getters and setters
    public TaskDTO getTask() { return task; }
    public void setTask(TaskDTO task) { this.task = task; }
}

class StatusHistory {
    private LocalDateTime timestamp;
    private String status;
    private UUID updatedBy;

    // Getters and setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
}