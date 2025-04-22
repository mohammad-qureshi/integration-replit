package com.rishabh.fiveday.integration.binding;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for binding repositories to projects
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryBinding {
    private String id;
    private String projectId;
    private String repositoryId;
    private String provider;
    private String name;
    private String description;
}