package com.gitintegration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Git commits
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommitDTO {
    private String sha;
    private String message;
    private String author;
    private LocalDateTime timestamp;
    private String repositoryId;
}