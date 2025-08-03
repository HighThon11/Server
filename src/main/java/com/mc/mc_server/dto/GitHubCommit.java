package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GitHub 커밋 정보")
public class GitHubCommit {
    
    @Schema(description = "커밋 SHA", example = "abc123def456")
    private String sha;
    
    @Schema(description = "커밋 메시지", example = "Add new feature")
    private String message;
    
    @Schema(description = "작성자 이름", example = "John Doe")
    private String authorName;
    
    @Schema(description = "작성자 이메일", example = "john@example.com")
    private String authorEmail;
    
    @Schema(description = "커밋 날짜", example = "2023-12-01T10:30:00Z")
    private String date;
    
    @Schema(description = "커밋 URL", example = "https://github.com/username/repo/commit/abc123def456")
    private String htmlUrl;
    
    public GitHubCommit() {}
    
    // Getters and Setters
    public String getSha() {
        return sha;
    }
    
    public void setSha(String sha) {
        this.sha = sha;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public String getAuthorEmail() {
        return authorEmail;
    }
    
    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getHtmlUrl() {
        return htmlUrl;
    }
    
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
