package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "GitHub 커밋 상세 정보")
public class GitHubCommitDetail {
    
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
    
    @Schema(description = "변경된 파일 목록")
    private List<GitHubFile> files;
    
    @Schema(description = "추가된 라인 수", example = "15")
    private Integer additions;
    
    @Schema(description = "삭제된 라인 수", example = "5")
    private Integer deletions;
    
    @Schema(description = "총 변경 라인 수", example = "20")
    private Integer total;
    
    public GitHubCommitDetail() {}
    
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
    
    public List<GitHubFile> getFiles() {
        return files;
    }
    
    public void setFiles(List<GitHubFile> files) {
        this.files = files;
    }
    
    public Integer getAdditions() {
        return additions;
    }
    
    public void setAdditions(Integer additions) {
        this.additions = additions;
    }
    
    public Integer getDeletions() {
        return deletions;
    }
    
    public void setDeletions(Integer deletions) {
        this.deletions = deletions;
    }
    
    public Integer getTotal() {
        return total;
    }
    
    public void setTotal(Integer total) {
        this.total = total;
    }
}
