package com.gitintegration.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Git pull requests or merge requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a pull request (GitHub) or merge request (GitLab)")
public class PullRequestDTO {
    
    @Schema(description = "Unique identifier of the request", example = "123456789")
    private Long id;
    
    @Schema(description = "Number or IID of the request", example = "42")
    private Long number;
    
    @Schema(description = "Title of the pull/merge request", example = "Add new feature")
    private String title;
    
    @Schema(description = "Author of the pull/merge request", example = "johndoe")
    private String author;
    
    @Schema(description = "Current state of the request", example = "open")
    private String state;
    
    @Schema(description = "Timestamp when the request was created", example = "2023-08-15T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Source branch", example = "feature/new-feature")
    private String sourceBranch;
    
    @Schema(description = "Target branch", example = "main")
    private String targetBranch;
}
