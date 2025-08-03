package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "레포지토리 저장 요청")
public class SaveRepositoryRequest {
    
    @Schema(description = "레포지토리 ID", example = "123456789", required = true)
    @NotNull(message = "레포지토리 ID는 필수입니다.")
    private Long repositoryId;
    
    @Schema(description = "레포지토리 이름", example = "my-awesome-project", required = true)
    @NotNull(message = "레포지토리 이름은 필수입니다.")
    private String repositoryName;
    
    @Schema(description = "레포지토리 전체 이름", example = "username/my-awesome-project", required = true)
    @NotNull(message = "레포지토리 전체 이름은 필수입니다.")
    private String repositoryFullName;
    
    @Schema(description = "레포지토리 설명", example = "This is an awesome project")
    private String repositoryDescription;
    
    @Schema(description = "레포지토리 URL", example = "https://github.com/username/my-awesome-project", required = true)
    @NotNull(message = "레포지토리 URL은 필수입니다.")
    private String repositoryUrl;
    
    @Schema(description = "기본 브랜치", example = "main")
    private String defaultBranch;
    
    @Schema(description = "비공개 레포지토리 여부", example = "false")
    private Boolean isPrivate;
    
    @Schema(description = "레포지토리 생성일", example = "2023-01-01T00:00:00Z")
    private String repositoryCreatedAt;
    
    @Schema(description = "레포지토리 업데이트일", example = "2023-12-01T00:00:00Z")
    private String repositoryUpdatedAt;
    
    public SaveRepositoryRequest() {}
    
    // Getters and Setters
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
