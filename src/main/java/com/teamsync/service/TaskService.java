package com.teamsync.service;

import com.teamsync.dto.TaskDTO;
import com.teamsync.entity.StatusHistory;
import com.teamsync.entity.Task;
import com.teamsync.entity.User;
import com.teamsync.exceptions.ResourceNotFoundException;
import com.teamsync.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class TaskService {
    private static final Logger logger = Logger.getLogger(TaskService.class.getName());
    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    public TaskDTO createTask(UUID assignerId, UUID assigneeId, Task task) {
        logger.info("Creating task for assigner ID: " + assignerId + ", assignee ID: " + assigneeId);
        if (task.getStatus() == null) {
            task.setStatus(Task.Status.TODO); // Ensure status is set
        }
        User assigner = userService.getUserById(assignerId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner not found with id: " + assignerId));
        User assignee = userService.getUserById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assigneeId));
        logger.info("Setting assignee with ID: " + assignee.getId());
        task.setAssignee(assignee);
        task.setAssigner(assigner); // Set the assigner based on the bearer token userId
        Task savedTask = taskRepository.save(task);
        return convertToDTO(savedTask, assigner);
    }

    public List<TaskDTO> getTasksByUserId(UUID userId, int page, int size) {
        logger.info("Fetching tasks for user ID: " + userId + " (page=" + page + ", size=" + size + ")");
        userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Pageable pageable = PageRequest.of(page, size);
        Page<Task> taskPage = taskRepository.findByAssigneeId(userId, pageable);
        return taskPage.getContent().stream()
                .map(task -> convertToDTO(task, task.getAssigner())) 
                .collect(Collectors.toList());
    }

    public long countTasksByUserId(UUID userId) {
        logger.info("Counting tasks for user ID: " + userId);
        return taskRepository.countByAssigneeId(userId);
    }

    public Task getTaskById(UUID id) {
        logger.info("Fetching task with ID: " + id);
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    public Task updateTask(UUID id, Task taskDetails, UUID userId) {
        logger.info("Updating task with ID: " + id + " by user ID: " + userId);
        Task task = getTaskById(id);
        
        // Only update non-null fields from taskDetails
        if (taskDetails.getTitle() != null) {
            task.setTitle(taskDetails.getTitle());
        }
        if (taskDetails.getDescription() != null) {
            task.setDescription(taskDetails.getDescription());
        }
        if (taskDetails.getDueDate() != null) {
            task.setDueDate(taskDetails.getDueDate());
        }
        if (taskDetails.getStatus() != null) {
            if (!taskDetails.getStatus().equals(task.getStatus())) {
                // Add status history only if status changes
                StatusHistory history = new StatusHistory();
                history.setTask(task);
                history.setStatus(taskDetails.getStatus().toString());
                history.setUpdatedBy(userId);
                history.setTimestamp(LocalDateTime.now());
                task.getStatusHistory().add(history);
            }
            task.setStatus(taskDetails.getStatus());
        }

        task.setUpdatedAt(LocalDateTime.now()); // Always update timestamp
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id, UUID userId) {
        logger.info("Deleting task with ID: " + id + " by user ID: " + userId);
        Task task = getTaskById(id);
        if (!userId.equals(task.getAssigner().getId())) {
            throw new ResourceNotFoundException("Only the assigner can delete this task");
        }
        User assignee = task.getAssignee();
        if (assignee != null) {
            assignee.getTasks().remove(task);
            userService.saveUser(assignee);
        }
        taskRepository.delete(task);
    }

    public TaskDTO convertToDTO(Task task, User assigner) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus().toString()); // Safe due to default
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setAssignee(createUserSummary(task.getAssignee()));
        dto.setAssigner(createUserSummary(assigner)); // Already set from task creation
        dto.setStatusHistory(task.getStatusHistory().stream()
                .map(h -> {
                    TaskDTO.StatusHistory dtoHist = new TaskDTO.StatusHistory();
                    dtoHist.setTimestamp(h.getTimestamp());
                    dtoHist.setStatus(h.getStatus());
                    dtoHist.setUpdatedBy(h.getUpdatedBy());
                    if (h.getTask() != null) {
                        dtoHist.setTaskId(h.getTask().getId());
                    }
                    return dtoHist;
                }).collect(Collectors.toList()));
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