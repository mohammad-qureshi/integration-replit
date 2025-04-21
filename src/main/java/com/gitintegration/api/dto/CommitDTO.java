package com.gitintegration.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Git commits.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a commit in a Git repository")
public class CommitDTO {
    
    @Schema(description = "SHA hash of the commit", example = "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d")
    private String sha;
    
    @Schema(description = "Commit message", example = "Fix navigation bug")
    private String message;
    
    @Schema(description = "Name of the commit author", example = "John Doe")
    private String author;
    
    @Schema(description = "Timestamp when the commit was created", example = "2023-08-15T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
