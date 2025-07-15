package com.teamsync.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectCreationDTO {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;
}