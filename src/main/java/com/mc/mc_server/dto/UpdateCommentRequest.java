package com.mc.mc_server.dto;

import java.util.Map;

public class UpdateCommentRequest {
    private String sessionId;
    private Map<String, String> updatedFiles; // filename -> updated content
    
    // Constructors
    public UpdateCommentRequest() {}
    
    public UpdateCommentRequest(String sessionId, Map<String, String> updatedFiles) {
        this.sessionId = sessionId;
        this.updatedFiles = updatedFiles;
    }
    
    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public Map<String, String> getUpdatedFiles() { return updatedFiles; }
    public void setUpdatedFiles(Map<String, String> updatedFiles) { this.updatedFiles = updatedFiles; }
}
