package com.teamsync.controller;

import com.teamsync.dto.ApiResponse;
import com.teamsync.dto.PaginatedResponse;
import com.teamsync.dto.TaskDTO;
import com.teamsync.dto.ProjectCreationDTO;
import com.teamsync.entity.Project;
import com.teamsync.entity.Task;
import com.teamsync.exceptions.ResourceNotFoundException;
import com.teamsync.service.TaskService;
import com.teamsync.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api")
public class TaskController {
    private static final Logger logger = Logger.getLogger(TaskController.class.getName());

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping("/projects")
    public ResponseEntity<ApiResponse<Project>> createProject(@RequestAttribute("userId") UUID adminId, @RequestBody ProjectCreationDTO projectDTO) {
        logger.info("Creating project by admin ID: " + adminId);
        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setAdmin(userService.getUserById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId)));
        Project createdProject = taskService.createProject(adminId, project);
        return ResponseEntity.ok(ApiResponse.success("Project created successfully", createdProject));
    }

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable UUID projectId) {
        logger.info("Fetching project with ID: " + projectId);
        Project project = taskService.getProjectById(projectId);
        return ResponseEntity.ok(ApiResponse.success("Project retrieved successfully", project));
    }

    @PostMapping("/projects/{projectId}/invite/{userId}")
    public ResponseEntity<ApiResponse<Void>> inviteMember(@PathVariable UUID projectId, @RequestAttribute("userId") UUID adminId, @PathVariable UUID userId) {
        logger.info("Inviting user ID: " + userId + " to project ID: " + projectId);
        taskService.inviteMember(projectId, adminId, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation sent successfully", null));
    }

    @PostMapping("/projects/{projectId}/accept/{userId}")
    public ResponseEntity<ApiResponse<Void>> acceptInvitation(@PathVariable UUID projectId, @RequestAttribute("userId") UUID userId) {
        logger.info("User ID: " + userId + " accepting invitation for project ID: " + projectId);
        taskService.acceptInvitation(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation accepted successfully", null));
    }

    @PostMapping("/projects/{projectId}/reject/{userId}")
    public ResponseEntity<ApiResponse<Void>> rejectInvitation(@PathVariable UUID projectId, @RequestAttribute("userId") UUID userId) {
        logger.info("User ID: " + userId + " rejecting invitation for project ID: " + projectId);
        taskService.rejectInvitation(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success("Invitation rejected successfully", null));
    }

    @PostMapping("/projects/{projectId}/remove/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable UUID projectId, @RequestAttribute("userId") UUID adminId, @PathVariable UUID userId) {
        logger.info("Removing user ID: " + userId + " from project ID: " + projectId);
        taskService.removeMember(projectId, adminId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }

    @DeleteMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable UUID projectId, @RequestAttribute("userId") UUID adminId) {
        logger.info("Deleting project with ID: " + projectId + " by admin ID: " + adminId);
        taskService.deleteProject(projectId, adminId);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    @PostMapping("/projects/{projectId}/tasks/users/{assigneeId}")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@PathVariable UUID projectId, @RequestAttribute("userId") UUID assignerId, @PathVariable UUID assigneeId, @RequestBody Task task) {
        logger.info("Creating task in project ID: " + projectId + " for assigner ID: " + assignerId + ", assignee ID: " + assigneeId);
        TaskDTO createdTask = taskService.createTask(assignerId, assigneeId, projectId, task);
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", createdTask));
    }

    @GetMapping("/projects/{projectId}/tasks/users/{userId}")
    public ResponseEntity<ApiResponse<PaginatedResponse<TaskDTO>>> getTasksByUserId(
            @PathVariable UUID projectId, @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching tasks for user ID: " + userId + " in project ID: " + projectId + " (page=" + page + ", size=" + size + ")");
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

    @GetMapping("/projects/{projectId}/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable UUID projectId, @PathVariable UUID id) {
        logger.info("Fetching task with ID: " + id + " in project ID: " + projectId);
        Task task = taskService.getTaskById(id);
        if (!task.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Task not found in this project");
        }
        TaskDTO dto = taskService.convertToDTO(task, task.getAssigner());
        return ResponseEntity.ok(ApiResponse.success("Task retrieved successfully", dto));
    }

    @PutMapping("/projects/{projectId}/tasks/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(@PathVariable UUID projectId, @PathVariable UUID id, @RequestAttribute("userId") UUID userId, @RequestBody Task taskDetails) {
        logger.info("Updating task with ID: " + id + " in project ID: " + projectId + " by user ID: " + userId);
        Task updatedTask = taskService.updateTask(id, taskDetails, userId);
        if (!updatedTask.getProject().getId().equals(projectId)) {
            throw new ResourceNotFoundException("Task not found in this project");
        }
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.convertToDTO(updatedTask, updatedTask.getAssigner())));
    }

    @DeleteMapping("/projects/{projectId}/tasks/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable UUID projectId, @PathVariable UUID id, @RequestAttribute("userId") UUID userId) {
        logger.info("Deleting task with ID: " + id + " in project ID: " + projectId + " by user ID: " + userId);
        taskService.deleteTask(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}