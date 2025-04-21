package com.gitintegration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Git pull/merge requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestDTO {
    private Long id;
    private int number;
    private String title;
    private String description;
    private String state;
    private String author;
    private String sourceBranch;
    private String targetBranch;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isMerged;
    private boolean isDraft;
    private String url;
}