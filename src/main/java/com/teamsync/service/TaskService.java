package com.teamsync.service;

import com.teamsync.dto.TaskDTO;
import com.teamsync.entity.Task;
import com.teamsync.entity.User;
import com.teamsync.exceptions.ResourceNotFoundException;
import com.teamsync.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger logger = Logger.getLogger(TaskService.class.getName());

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    public TaskDTO createTask(UUID assignerId, UUID assigneeId, Task task) {
        logger.info("Creating task for assigner ID: " + assignerId + ", assignee ID: " + assigneeId);
        User assigner = userService.getUserById(assignerId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner not found with id: " + assignerId));
        User assignee = userService.getUserById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assigneeId));
        task.setAssignee(assignee);
        task.setPriority(Task.Priority.MEDIUM); // Default priority
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask, assigner);
    }

    public List<TaskDTO> getTasksByUserId(UUID userId, int page, int size) {
        logger.info("Fetching tasks for user ID: " + userId + " (page=" + page + ", size=" + size + ")");
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.findByAssigneeId(userId, pageable);
        return taskPage.getContent().stream()
                .map(task -> convertToDTO(task, task.getAssignee()))
                .collect(Collectors.toList());
    }

    public Task getTaskById(UUID id) {
        logger.info("Fetching task with ID: " + id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public Task updateTask(UUID id, Task taskDetails) {
        logger.info("Updating task with ID: " + id);
        Task task = getTaskById(id);
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setDueDate(taskDetails.getDueDate());
        task.setPriority(taskDetails.getPriority());
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id) {
        logger.info("Deleting task with ID: " + id);
        Task task = getTaskById(id);
        User assignee = task.getAssignee();
        if (assignee != null) {
            assignee.getTasks().remove(task);
            userService.saveUser(assignee);
        }
        taskRepository.delete(task);
    }

    public long countTasksByUserId(UUID userId) {
        return taskRepository.countByAssigneeId(userId);
    }

    public TaskDTO convertToDTO(Task task, User assigner) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus().toString());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setPriority(task.getPriority() != null ? task.getPriority().toString() : null);
        dto.setAssignee(createUserSummary(task.getAssignee()));
        dto.setAssigner(createUserSummary(assigner));
        return dto;
    }

    private TaskDTO.UserSummary createUserSummary(User user) {
        if (user == null) return null;
        TaskDTO.UserSummary summary = new TaskDTO.UserSummary();
        summary.setId(user.getId());
        summary.setEmail(user.getEmail());
        summary.setFirstName(user.getFirstName());
        summary.setLastName(user.getLastName());
        return summary;
    }
}