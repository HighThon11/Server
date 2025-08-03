package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "GitHub 파일 변경 정보")
public class GitHubFile {
    
    @Schema(description = "파일명", example = "src/main/java/Main.java")
    private String filename;
    
    @Schema(description = "변경 상태", example = "modified")
    private String status;
    
    @Schema(description = "추가된 라인 수", example = "10")
    private Integer additions;
    
    @Schema(description = "삭제된 라인 수", example = "3")
    private Integer deletions;
    
    @Schema(description = "총 변경 라인 수", example = "13")
    private Integer changes;
    
    @Schema(description = "파일 변경 내용 (diff)")
    private String patch;
    
    public GitHubFile() {}
    
    // Getters and Setters
    public String getFilename() {
        return filename;
    }
    
    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public Integer getChanges() {
        return changes;
    }
    
    public void setChanges(Integer changes) {
        this.changes = changes;
    }
    
    public String getPatch() {
        return patch;
    }
    
    public void setPatch(String patch) {
        this.patch = patch;
    }
}
