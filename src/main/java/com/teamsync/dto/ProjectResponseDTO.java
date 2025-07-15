package com.teamsync.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ProjectResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private UUID adminId;
    private List<UUID> memberIds;
    private LocalDateTime createdAt;
}