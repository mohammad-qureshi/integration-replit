package com.gitintegration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Git pull/merge requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestDTO {
    private Long id;
    private Integer number;
    private String title;
    private String description;
    private String author;
    private String state;
    private LocalDateTime createdAt;
    private String sourceBranch;
    private String targetBranch;
    private String repositoryId;
}