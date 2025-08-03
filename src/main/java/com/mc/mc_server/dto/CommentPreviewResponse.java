package com.mc.mc_server.dto;

import java.util.List;
import java.util.Map;

public class CommentPreviewResponse {
    private String commitSha;
    private String commitMessage;
    private String branch;
    private Map<String, FileCommentPreview> files;
    private String sessionId; // 임시 저장을 위한 세션 ID
    
    public static class FileCommentPreview {
        private String filename;
        private String originalContent;
        private String commentedContent;
        private List<CommentItem> addedComments;
        private boolean modified;
        
        public static class CommentItem {
            private int lineNumber;
            private String comment;
            private String codeLine;
            
            // Constructors
            public CommentItem() {}
            
            public CommentItem(int lineNumber, String comment, String codeLine) {
                this.lineNumber = lineNumber;
                this.comment = comment;
                this.codeLine = codeLine;
            }
            
            // Getters and Setters
            public int getLineNumber() { return lineNumber; }
            public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
            
            public String getComment() { return comment; }
            public void setComment(String comment) { this.comment = comment; }
            
            public String getCodeLine() { return codeLine; }
            public void setCodeLine(String codeLine) { this.codeLine = codeLine; }
        }
        
        // Constructors
        public FileCommentPreview() {}
        
        public FileCommentPreview(String filename, String originalContent, String commentedContent, 
                                 List<CommentItem> addedComments, boolean modified) {
            this.filename = filename;
            this.originalContent = originalContent;
            this.commentedContent = commentedContent;
            this.addedComments = addedComments;
            this.modified = modified;
        }
        
        // Getters and Setters
        public String getFilename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        
        public String getOriginalContent() { return originalContent; }
        public void setOriginalContent(String originalContent) { this.originalContent = originalContent; }
        
        public String getCommentedContent() { return commentedContent; }
        public void setCommentedContent(String commentedContent) { this.commentedContent = commentedContent; }
        
        public List<CommentItem> getAddedComments() { return addedComments; }
        public void setAddedComments(List<CommentItem> addedComments) { this.addedComments = addedComments; }
        
        public boolean isModified() { return modified; }
        public void setModified(boolean modified) { this.modified = modified; }
    }
    
    // Constructors
    public CommentPreviewResponse() {}
    
    public CommentPreviewResponse(String commitSha, String commitMessage, String branch, 
                                 Map<String, FileCommentPreview> files, String sessionId) {
        this.commitSha = commitSha;
        this.commitMessage = commitMessage;
        this.branch = branch;
        this.files = files;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public String getCommitSha() { return commitSha; }
    public void setCommitSha(String commitSha) { this.commitSha = commitSha; }
    
    public String getCommitMessage() { return commitMessage; }
    public void setCommitMessage(String commitMessage) { this.commitMessage = commitMessage; }
    
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    
    public Map<String, FileCommentPreview> getFiles() { return files; }
    public void setFiles(Map<String, FileCommentPreview> files) { this.files = files; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
