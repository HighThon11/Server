package com.mc.mc_server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_repositories")
public class SavedRepository {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;
    
    @Column(name = "repository_name", nullable = false)
    private String repositoryName;
    
    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;
    
    @Column(name = "repository_description")
    private String repositoryDescription;
    
    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;
    
    @Column(name = "default_branch")
    private String defaultBranch;
    
    @Column(name = "is_private")
    private Boolean isPrivate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "repository_created_at")
    private String repositoryCreatedAt;
    
    @Column(name = "repository_updated_at")
    private String repositoryUpdatedAt;
    
    public SavedRepository() {}
    
    public SavedRepository(User user, Long repositoryId, String repositoryName, 
                          String repositoryFullName, String repositoryDescription,
                          String repositoryUrl, String defaultBranch, Boolean isPrivate,
                          String repositoryCreatedAt, String repositoryUpdatedAt) {
        this.user = user;
        this.repositoryId = repositoryId;
        this.repositoryName = repositoryName;
        this.repositoryFullName = repositoryFullName;
        this.repositoryDescription = repositoryDescription;
        this.repositoryUrl = repositoryUrl;
        this.defaultBranch = defaultBranch;
        this.isPrivate = isPrivate;
        this.repositoryCreatedAt = repositoryCreatedAt;
        this.repositoryUpdatedAt = repositoryUpdatedAt;
        this.createdAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
