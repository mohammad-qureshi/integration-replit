package com.gitintegration.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Git repositories
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO {
    private String id;
    private String name;
    private String fullName;
    private String url;
    private String description;
    private String defaultBranch;
    private String providerId;
}