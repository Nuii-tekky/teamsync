package com.teamsync.service;

import com.teamsync.dto.TaskDTO;
import com.teamsync.entity.Project;
import com.teamsync.entity.Task;
import com.teamsync.entity.User;
import com.teamsync.exceptions.ResourceNotFoundException;
import com.teamsync.repository.ProjectRepository;
import com.teamsync.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {
    private static final Logger logger = Logger.getLogger(TaskService.class.getName());
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ProjectRepository projectRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository, UserService userService, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.projectRepository = projectRepository;
    }

    public Project createProject(UUID adminId, Project project) {
        logger.info("Creating project for admin ID: " + adminId);
        User admin = userService.getUserById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + adminId));
        project.setAdmin(admin);
        return projectRepository.save(project);
    }

    public Project getProjectById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    public void inviteMember(UUID projectId, UUID adminId, UUID userId) {
        logger.info("Inviting member with ID: " + userId + " to project ID: " + projectId);
        Project project = getProjectById(projectId);
        if (!project.getAdmin().getId().equals(adminId)) {
            throw new ResourceNotFoundException("Only the admin can invite members");
        }
        if (adminId.equals(userId)) {
            throw new ResourceNotFoundException("Admin cannot invite themselves");
        }
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (project.getMembers().contains(user)) {
            throw new ResourceNotFoundException("User is already a member");
        }
        project.getMembers().add(user);
        projectRepository.save(project);
    }

    public void acceptInvitation(UUID projectId, UUID userId) {
        logger.info("User with ID: " + userId + " accepting invitation for project ID: " + projectId);
        Project project = getProjectById(projectId);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (!project.getMembers().contains(user)) {
            throw new ResourceNotFoundException("User not invited to this project");
        }
        projectRepository.save(project);
    }

    public void rejectInvitation(UUID projectId, UUID userId) {
        logger.info("User with ID: " + userId + " rejecting invitation for project ID: " + projectId);
        Project project = getProjectById(projectId);
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (project.getMembers().contains(user)) {
            project.getMembers().remove(user);
            projectRepository.save(project);
        }
    }

    public void removeMember(UUID projectId, UUID adminId, UUID userId) {
        logger.info("Removing member with ID: " + userId + " from project ID: " + projectId);
        Project project = getProjectById(projectId);
        if (!project.getAdmin().getId().equals(adminId)) {
            throw new ResourceNotFoundException("Only the admin can remove members");
        }
        if (adminId.equals(userId)) {
            throw new ResourceNotFoundException("Admin cannot remove themselves");
        }
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        if (project.getMembers().contains(user)) {
            project.getMembers().remove(user);
            projectRepository.save(project);
        }
    }

    public void deleteProject(UUID projectId, UUID adminId) {
        logger.info("Deleting project with ID: " + projectId + " by admin ID: " + adminId);
        Project project = getProjectById(projectId);
        if (!project.getAdmin().getId().equals(adminId)) {
            throw new ResourceNotFoundException("Only the admin can delete this project");
        }
        projectRepository.delete(project);
    }

    public TaskDTO createTask(UUID assignerId, UUID assigneeId, UUID projectId, Task task) {
        logger.info("Creating task for assigner ID: " + assignerId + ", assignee ID: " + assigneeId + ", project ID: " + projectId);
        if (task.getStatus() == null) {
            task.setStatus(Task.Status.TODO);
        }
        User assigner = userService.getUserById(assignerId)
                .orElseThrow(() -> new ResourceNotFoundException("Assigner not found with id: " + assignerId));
        User assignee = userService.getUserById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id: " + assigneeId));
        Project project = getProjectById(projectId);
        if (!project.getAdmin().getId().equals(assignerId) && !project.getMembers().stream().anyMatch(m -> m.getId().equals(assignerId))) {
            throw new ResourceNotFoundException("User not authorized to create tasks in this project");
        }
        if (assignerId.equals(assigneeId)) {
            throw new ResourceNotFoundException("Assigner cannot be the same as assignee");
        }
        task.setAssignee(assignee);
        task.setAssigner(assigner);
        task.setProject(project);
        task.setCreatedAt(LocalDateTime.now());
        Task savedTask = taskRepository.save(task);
        project.getTasks().add(savedTask);
        projectRepository.save(project);
        return convertToDTO(savedTask, assigner);
    }

    public List<TaskDTO> getTasksByUserId(UUID userId, int page, int size) {
        logger.info("Fetching tasks for user ID: " + userId + " (page=" + page + ", size=" + size + ")");
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
        if (!userId.equals(task.getAssigner().getId())) {
            throw new ResourceNotFoundException("Only the assigner can update this task");
        }
        if (taskDetails.getTitle() != null) task.setTitle(taskDetails.getTitle());
        if (taskDetails.getDescription() != null) task.setDescription(taskDetails.getDescription());
        if (taskDetails.getDueDate() != null) task.setDueDate(taskDetails.getDueDate());
        if (taskDetails.getStatus() != null) task.setStatus(taskDetails.getStatus());
        task.setUpdatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }

    public void deleteTask(UUID id, UUID userId) {
        logger.info("Deleting task with ID: " + id + " by user ID: " + userId);
        Task task = getTaskById(id);
        if (!userId.equals(task.getAssigner().getId())) {
            throw new ResourceNotFoundException("Only the assigner can delete this task");
        }
        taskRepository.delete(task);
    }

    public TaskDTO convertToDTO(Task task, User assigner) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus() != null ? task.getStatus().toString() : null);
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        
        // Map User entities to UserSummary
        TaskDTO.UserSummary assigneeSummary = new TaskDTO.UserSummary();
        if (task.getAssignee() != null) {
            assigneeSummary.setId(task.getAssignee().getId());
            assigneeSummary.setEmail(task.getAssignee().getEmail());
            assigneeSummary.setFirstName(task.getAssignee().getFirstName());
            assigneeSummary.setLastName(task.getAssignee().getLastName());
        }
        dto.setAssignee(assigneeSummary);

        TaskDTO.UserSummary assignerSummary = new TaskDTO.UserSummary();
        if (assigner != null) {
            assignerSummary.setId(assigner.getId());
            assignerSummary.setEmail(assigner.getEmail());
            assignerSummary.setFirstName(assigner.getFirstName());
            assignerSummary.setLastName(assigner.getLastName());
        }
        dto.setAssigner(assignerSummary);

        return dto;
    }
}