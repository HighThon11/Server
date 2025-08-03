package com.mc.mc_server.dto;

import com.mc.mc_server.entity.SavedRepository;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "저장된 레포지토리 응답")
public class SavedRepositoryResponse {
    
    @Schema(description = "저장 ID", example = "1")
    private Long id;
    
    @Schema(description = "레포지토리 ID", example = "123456789")
    private Long repositoryId;
    
    @Schema(description = "레포지토리 이름", example = "my-awesome-project")
    private String repositoryName;
    
    @Schema(description = "레포지토리 전체 이름", example = "username/my-awesome-project")
    private String repositoryFullName;
    
    @Schema(description = "레포지토리 설명", example = "This is an awesome project")
    private String repositoryDescription;
    
    @Schema(description = "레포지토리 URL", example = "https://github.com/username/my-awesome-project")
    private String repositoryUrl;
    
    @Schema(description = "기본 브랜치", example = "main")
    private String defaultBranch;
    
    @Schema(description = "비공개 레포지토리 여부", example = "false")
    private Boolean isPrivate;
    
    @Schema(description = "저장일", example = "2023-12-01T00:00:00")
    private LocalDateTime savedAt;
    
    @Schema(description = "레포지토리 생성일", example = "2023-01-01T00:00:00Z")
    private String repositoryCreatedAt;
    
    @Schema(description = "레포지토리 업데이트일", example = "2023-12-01T00:00:00Z")
    private String repositoryUpdatedAt;
    
    public SavedRepositoryResponse() {}
    
    public SavedRepositoryResponse(SavedRepository savedRepository) {
        this.id = savedRepository.getId();
        this.repositoryId = savedRepository.getRepositoryId();
        this.repositoryName = savedRepository.getRepositoryName();
        this.repositoryFullName = savedRepository.getRepositoryFullName();
        this.repositoryDescription = savedRepository.getRepositoryDescription();
        this.repositoryUrl = savedRepository.getRepositoryUrl();
        this.defaultBranch = savedRepository.getDefaultBranch();
        this.isPrivate = savedRepository.getIsPrivate();
        this.savedAt = savedRepository.getCreatedAt();
        this.repositoryCreatedAt = savedRepository.getRepositoryCreatedAt();
        this.repositoryUpdatedAt = savedRepository.getRepositoryUpdatedAt();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRepositoryId() {
        return repositoryId;
    }
    
    public void setRepositoryId(Long repositoryId) {
        this.repositoryId = repositoryId;
    }
    
    public String getRepositoryName() {
        return repositoryName;
    }
    
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }
    
    public String getRepositoryFullName() {
        return repositoryFullName;
    }
    
    public void setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
    }
    
    public String getRepositoryDescription() {
        return repositoryDescription;
    }
    
    public void setRepositoryDescription(String repositoryDescription) {
        this.repositoryDescription = repositoryDescription;
    }
    
    public String getRepositoryUrl() {
        return repositoryUrl;
    }
    
    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
    
    public String getDefaultBranch() {
        return defaultBranch;
    }
    
    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }
    
    public Boolean getIsPrivate() {
        return isPrivate;
    }
    
    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
    
    public LocalDateTime getSavedAt() {
        return savedAt;
    }
    
    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
    
    public String getRepositoryCreatedAt() {
        return repositoryCreatedAt;
    }
    
    public void setRepositoryCreatedAt(String repositoryCreatedAt) {
        this.repositoryCreatedAt = repositoryCreatedAt;
    }
    
    public String getRepositoryUpdatedAt() {
        return repositoryUpdatedAt;
    }
    
    public void setRepositoryUpdatedAt(String repositoryUpdatedAt) {
        this.repositoryUpdatedAt = repositoryUpdatedAt;
    }
}
