package com.gitintegration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Git repositories
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO {
    private String id;
    private String name;
    private String fullName;
    private String description;
    private String url;
    private String defaultBranch;
    private String owner;
    private boolean isPrivate;
    private boolean isFork;
    private String providerId;
    private String repositoryId;
}