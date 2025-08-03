package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GitHub 레포지토리 정보")
public class GitHubRepository {
    
    @Schema(description = "레포지토리 ID", example = "123456789")
    private Long id;
    
    @Schema(description = "레포지토리 이름", example = "my-awesome-project")
    private String name;
    
    @Schema(description = "레포지토리 전체 이름", example = "username/my-awesome-project")
    private String fullName;
    
    @Schema(description = "레포지토리 설명", example = "This is an awesome project")
    private String description;
    
    @Schema(description = "레포지토리 URL", example = "https://github.com/username/my-awesome-project")
    private String htmlUrl;
    
    @Schema(description = "기본 브랜치", example = "main")
    private String defaultBranch;
    
    @Schema(description = "비공개 레포지토리 여부", example = "false")
    private Boolean privateRepo;
    
    @Schema(description = "생성일", example = "2023-01-01T00:00:00Z")
    private String createdAt;
    
    @Schema(description = "업데이트일", example = "2023-12-01T00:00:00Z")
    private String updatedAt;
    
    public GitHubRepository() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getHtmlUrl() {
        return htmlUrl;
    }
    
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
    
    public String getDefaultBranch() {
        return defaultBranch;
    }
    
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }
    
    public Boolean getPrivateRepo() {
        return privateRepo;
    }
    
    public void setPrivateRepo(Boolean privateRepo) {
        this.privateRepo = privateRepo;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
