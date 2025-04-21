package com.gitintegration.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Git repositories.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a Git repository")
public class RepositoryDTO {
    
    @Schema(description = "Unique identifier of the repository", example = "12345")
    private Long id;
    
    @Schema(description = "Name of the repository", example = "my-project")
    private String name;
    
    @Schema(description = "Full name of the repository including owner/organization", example = "octocat/my-project")
    private String fullName;
    
    @Schema(description = "URL of the repository", example = "https://github.com/octocat/my-project")
    private String url;
    
    @Schema(description = "Description of the repository", example = "This is a sample project")
    private String description;
    
    @Schema(description = "Default branch of the repository", example = "main")
    private String defaultBranch;
}
