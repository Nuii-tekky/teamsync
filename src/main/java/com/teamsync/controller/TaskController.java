package com.teamsync.controller;

import com.teamsync.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByUserId(@PathVariable UUID userId) {
        logger.info("Fetching tasks for user ID: " + userId);
        List<Task> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("Tasks retrieved successfully", tasks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTaskById(@PathVariable UUID id) {
        logger.info("Fetching task with ID: " + id);
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable UUID id, @RequestBody Task taskDetails) {
        logger.info("Updating task with ID: " + id);
        Task updatedTask = taskService.updateTask(id, taskDetails);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", updatedTask));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID id) {
        logger.info("Deleting task with ID: " + id);
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}