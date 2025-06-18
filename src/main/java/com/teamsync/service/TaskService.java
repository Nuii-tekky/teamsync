package com.teamsync.service;

import com.teamsync.entity.Task;
import com.teamsync.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Service layer for task-related business logic
@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Create or update a task
    public Task saveTask(Task task) {
      if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
        throw new IllegalArgumentException("Task title cannot be empty");
      }
      if (task.getStatus() == null || task.getStatus().trim().isEmpty()) {
        throw new IllegalArgumentException("Task status cannot be empty");
      }
      return taskRepository.save(task);
    }

    // Retrieve a task by ID
    public Optional<Task> getTaskById(Long id) {
      return taskRepository.findById(id);
    }

    // Retrieve all tasks
    public List<Task> getAllTasks() {
      return taskRepository.findAll();
    }

    // Retrieve tasks by status
    public List<Task> getTasksByStatus(String status) {
      return taskRepository.findByStatus(status);
    }

    // Delete a task by ID
    public void deleteTask(Long id) {
      taskRepository.deleteById(id);
    }
}