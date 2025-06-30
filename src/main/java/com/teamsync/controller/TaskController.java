package com.teamsync.controller;

import com.teamsync.dto.ApiResponse;
import com.teamsync.dto.PaginatedResponse;
import com.teamsync.dto.TaskDTO;
import com.teamsync.entity.Task;
import com.teamsync.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private static final Logger logger = Logger.getLogger(TaskController.class.getName());

    @Autowired
    private TaskService taskService;


    @PostMapping("/users/{assigneeId}")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@RequestAttribute("userId") UUID assignerId, @PathVariable UUID assigneeId, @RequestBody Task task) {
        logger.info("Admin creating task for assigner ID: " + assignerId + ", assignee ID: " + assigneeId);
        TaskDTO createdTask = taskService.createTask(assignerId, assigneeId, task);
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", createdTask));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<TaskDTO>>> getTasksByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching tasks for user ID: " + userId + " (page=" + page + ", size=" + size + ")");
        List<TaskDTO> tasks = taskService.getTasksByUserId(userId, page, size);
        long totalCount = taskService.countTasksByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalCount / size);
        PaginatedResponse<TaskDTO> response = new PaginatedResponse<>(
                tasks,
                totalCount,
                page,
                totalPages,
                page + 1 < totalPages
        );
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable UUID id) {
        logger.info("Fetching task with ID: " + id);
        Task task = taskService.getTaskById(id);
        TaskDTO dto = taskService.convertToDTO(task, task.getAssigner()); // Use assigner for consistency
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(@PathVariable UUID id, @RequestAttribute("userId") UUID userId, @RequestBody Task taskDetails) {
        logger.info("Updating task with ID: " + id + " by user ID: " + userId);
        Task updatedTask = taskService.updateTask(id, taskDetails, userId);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.convertToDTO(updatedTask, updatedTask.getAssigner()))); // Use stored assigner
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id, @RequestAttribute("userId") UUID userId) {
        logger.info("Deleting task with ID: " + id + " by user ID: " + userId);
        taskService.deleteTask(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}