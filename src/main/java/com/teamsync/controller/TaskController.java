package com.teamsync.controller;

import com.teamsync.entity.Task;
import com.teamsync.exceptions.ResourceNotFoundException;
import com.teamsync.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST controller for task-related endpoints
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Create a new task
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskService.saveTask(task);
    }

    // Retrieve a task by ID
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable UUID id) {
        return taskService.getTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
    }

    // Retrieve all tasks
    @GetMapping({"", "/"})
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // Retrieve tasks by status
    @GetMapping("/status/{status}")
    public List<Task> getTasksByStatus(@PathVariable String status) {
        return taskService.getTasksByStatus(status);
    }

    // Update a task
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable UUID id, @RequestBody Task task) {
        return taskService.getTaskById(id)
                .map(existingTask -> {
                    task.setId(id);
                    return taskService.saveTask(task);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
    }

    // Delete a task
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
    }
}