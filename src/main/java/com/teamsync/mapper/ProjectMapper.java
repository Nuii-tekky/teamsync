package com.teamsync.mapper;

import com.teamsync.dto.ProjectResponseDTO;
import com.teamsync.entity.Project;
import com.teamsync.entity.User;

import java.util.stream.Collectors;

public class ProjectMapper {
    public static ProjectResponseDTO toResponseDTO(Project project) {
        ProjectResponseDTO dto = new ProjectResponseDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setAdminId(project.getAdmin() != null ? project.getAdmin().getId() : null);
        dto.setMemberIds(project.getMembers().stream().map(User::getId).collect(Collectors.toList()));
        dto.setCreatedAt(project.getCreatedAt());
        return dto;
    }
}