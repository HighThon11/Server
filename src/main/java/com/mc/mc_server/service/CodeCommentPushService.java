package com.mc.mc_server.service;

import com.mc.mc_server.dto.CommentPreviewResponse;
import com.mc.mc_server.dto.CommentPreviewResponse.FileCommentPreview;
import com.mc.mc_server.dto.CommentPreviewResponse.FileCommentPreview.CommentItem;
import com.mc.mc_server.dto.GitHubCommitDetail;
import com.mc.mc_server.dto.GitHubFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CodeCommentPushService {
    
    private static final Logger logger = LoggerFactory.getLogger(CodeCommentPushService.class);
    
    private final GitHubService gitHubService;
    private final CodeCommentService codeCommentService;
    private final CommentSessionService commentSessionService;
    
    @Autowired
    public CodeCommentPushService(GitHubService gitHubService, CodeCommentService codeCommentService, CommentSessionService commentSessionService) {
        this.gitHubService = gitHubService;
        this.codeCommentService = codeCommentService;
        this.commentSessionService = commentSessionService;
    }
    
    /**
     * 세션 데이터를 조회합니다 (컨트롤러용).
     */
    public CommentSessionService.SessionData getSessionData(String sessionId) {
        return commentSessionService.getSession(sessionId);
    }
    
    /**
     * 세션을 삭제합니다 (컨트롤러용).
     */
    public void deleteSession(String sessionId) {
        commentSessionService.deleteSession(sessionId);
    }
    
    /**
     * 커밋의 변경사항에 주석을 생성하고 미리보기를 제공합니다 (푸시하지 않음).
     */
    public CommentPreviewResponse generateCommentsPreview(String token, String owner, String repo, String sha, String branch) {
        try {
            logger.info("Starting comment preview generation...");
            
            // 1. 커밋 상세 정보 가져오기
            GitHubCommitDetail commitDetail = gitHubService.getCommitDetail(token, owner, repo, sha);
            logger.info("Retrieved commit detail: {}", commitDetail.getMessage());
            
            // 2. 각 파일에 대해 주석 생성 (푸시하지 않음)
            Map<String, FileCommentPreview> filePreviews = new HashMap<>();
            Map<String, String> updatedFiles = new HashMap<>();
            
            for (GitHubFile file : commitDetail.getFiles()) {
                if (shouldProcessFile(file)) {
                    logger.info("Processing file: {}", file.getFilename());
                    
                    try {
                        // 현재 파일 내용 가져오기
                        String currentContent = gitHubService.getFileContent(token, owner, repo, file.getFilename(), branch);
                        
                        // 주석 적용
                        FileCommentResult result = applyCommentsToFileWithDetails(currentContent, file, commitDetail);
                        
                        if (result.commentedContent != null && !result.commentedContent.equals(currentContent)) {
                            FileCommentPreview preview = new FileCommentPreview(
                                file.getFilename(),
                                currentContent,
                                result.commentedContent,
                                result.addedComments,
                                true
                            );
                            filePreviews.put(file.getFilename(), preview);
                            updatedFiles.put(file.getFilename(), result.commentedContent);
                            logger.info("Generated comments for: {}", file.getFilename());
                        } else {
                            FileCommentPreview preview = new FileCommentPreview(
                                file.getFilename(),
                                currentContent,
                                currentContent,
                                new ArrayList<>(),
                                false
                            );
                            filePreviews.put(file.getFilename(), preview);
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process file {}: {}", file.getFilename(), e.getMessage());
                        // 개별 파일 실패는 전체 프로세스를 중단하지 않음
                    }
                }
            }
            
            // 3. 세션 생성 및 임시 저장
            String sessionId = commentSessionService.createSession(token, owner, repo, sha, branch, updatedFiles);
            
            // 4. 미리보기 응답 생성
            CommentPreviewResponse response = new CommentPreviewResponse(
                sha,
                commitDetail.getMessage(),
                branch,
                filePreviews,
                sessionId
            );
            
            logger.info("Successfully generated preview with session: {}", sessionId);
            return response;
                
        } catch (Exception e) {
            logger.error("Error in generateCommentsPreview: {}", e.getMessage(), e);
            throw new RuntimeException("주석 미리보기 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 세션에 저장된 내용을 업데이트합니다.
     */
    public void updateSessionComments(String sessionId, Map<String, String> updatedFiles) {
        try {
            commentSessionService.updateSessionFiles(sessionId, updatedFiles);
            logger.info("Updated session files: {}", sessionId);
        } catch (Exception e) {
            logger.error("Error updating session: {}", e.getMessage());
            throw new RuntimeException("세션 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 세션에 저장된 내용을 실제로 GitHub에 푸시합니다.
     */
    public String pushSessionComments(String sessionId) {
        try {
            logger.info("Starting session push: {}", sessionId);
            
            // 1. 세션 데이터 가져오기
            CommentSessionService.SessionData sessionData = commentSessionService.getSession(sessionId);
            
            // 2. 업데이트된 파일들이 있는지 확인
            Map<String, String> updatedFiles = sessionData.getUpdatedFiles();
            if (updatedFiles.isEmpty()) {
                throw new RuntimeException("푸시할 변경사항이 없습니다.");
            }
            
            // 3. 커밋 메시지 생성
            String commitMessage = String.format("docs: Add AI-generated comments for commit %s", 
                sessionData.getCommitSha().substring(0, 8));
            
            // 4. GitHub에 푸시
            String newCommitSha = gitHubService.createCommitWithMultipleFiles(
                sessionData.getToken(), 
                sessionData.getOwner(), 
                sessionData.getRepo(), 
                updatedFiles, 
                commitMessage, 
                sessionData.getBranch()
            );
            
            // 5. 세션 삭제
            commentSessionService.deleteSession(sessionId);
            
            logger.info("Successfully pushed session comments: {}", newCommitSha);
            
            return String.format("성공적으로 %d개 파일에 주석을 적용하고 커밋했습니다. 커밋 SHA: %s", 
                updatedFiles.size(), newCommitSha.substring(0, 8));
                
        } catch (Exception e) {
            logger.error("Error in pushSessionComments: {}", e.getMessage(), e);
            throw new RuntimeException("주석 푸시 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 기존 방식 유지 (바로 푸시)
     */
    public String applyCommentsAndPush(String token, String owner, String repo, String sha, String branch) {
        try {
            logger.info("Starting direct comment application and push...");
            
            // 1. 커밋 상세 정보 가져오기
            GitHubCommitDetail commitDetail = gitHubService.getCommitDetail(token, owner, repo, sha);
            logger.info("Retrieved commit detail: {}", commitDetail.getMessage());
            
            // 2. 각 파일에 대해 주석 적용
            Map<String, String> updatedFiles = new HashMap<>();
            
            for (GitHubFile file : commitDetail.getFiles()) {
                if (shouldProcessFile(file)) {
                    logger.info("Processing file: {}", file.getFilename());
                    
                    try {
                        // 현재 파일 내용 가져오기
                        String currentContent = gitHubService.getFileContent(token, owner, repo, file.getFilename(), branch);
                        
                        // 주석 적용
                        String commentedContent = applyCommentsToFile(currentContent, file, commitDetail);
                        
                        if (commentedContent != null && !commentedContent.equals(currentContent)) {
                            updatedFiles.put(file.getFilename(), commentedContent);
                            logger.info("Added comments to: {}", file.getFilename());
                        }
                    } catch (Exception e) {
                        logger.warn("Failed to process file {}: {}", file.getFilename(), e.getMessage());
                        // 개별 파일 실패는 전체 프로세스를 중단하지 않음
                    }
                }
            }
            
            if (updatedFiles.isEmpty()) {
                return "주석을 추가할 파일이 없습니다.";
            }
            
            // 3. 모든 변경사항을 한 번에 커밋
            String commitMessage = String.format("docs: Add AI-generated comments for commit %s\n\n%s", 
                sha.substring(0, 8), commitDetail.getMessage());
            
            String newCommitSha = gitHubService.createCommitWithMultipleFiles(
                token, owner, repo, updatedFiles, commitMessage, branch);
            
            logger.info("Successfully created commit: {}", newCommitSha);
            
            return String.format("성공적으로 %d개 파일에 주석을 적용하고 커밋했습니다. 커밋 SHA: %s", 
                updatedFiles.size(), newCommitSha.substring(0, 8));
                
        } catch (Exception e) {
            logger.error("Error in applyCommentsAndPush: {}", e.getMessage(), e);
            throw new RuntimeException("주석 적용 및 푸시 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    // ========== Private Helper Methods ==========
    
    /**
     * 파일을 처리해야 하는지 판단합니다.
     */
    private boolean shouldProcessFile(GitHubFile file) {
        String filename = file.getFilename().toLowerCase();
        
        // 지원하는 파일 형식만 처리
        return filename.endsWith(".java") || 
               filename.endsWith(".js") || 
               filename.endsWith(".ts") || 
               filename.endsWith(".jsx") || 
               filename.endsWith(".tsx") || 
               filename.endsWith(".py") || 
               filename.endsWith(".css") || 
               filename.endsWith(".scss") ||
               filename.endsWith(".html") ||
               filename.endsWith(".vue") ||
               filename.endsWith(".php") ||
               filename.endsWith(".go") ||
               filename.endsWith(".rs") ||
               filename.endsWith(".cpp") ||
               filename.endsWith(".c") ||
               filename.endsWith(".h");
    }
    
    /**
     * 파일에 주석을 적용하고 상세 정보를 반환합니다.
     */
    private FileCommentResult applyCommentsToFileWithDetails(String currentContent, GitHubFile file, GitHubCommitDetail commitDetail) {
        if (file.getPatch() == null || file.getPatch().isEmpty()) {
            return new FileCommentResult(currentContent, new ArrayList<>());
        }
        
        String[] contentLines = currentContent.split("\n");
        StringBuilder commentedContent = new StringBuilder();
        List<CommentItem> addedComments = new ArrayList<>();
        
        boolean isJavaFile = file.getFilename().endsWith(".java");
        String commentPrefix = isJavaFile ? "//" : getCommentPrefix(file.getFilename());
        
        for (int i = 0; i < contentLines.length; i++) {
            String line = contentLines[i];
            commentedContent.append(line).append("\n");
            
            // 새로 추가된 메서드나 클래스에 주석 추가
            if (shouldAddComment(line, file.getFilename())) {
                String comment = generateContextualComment(line, file.getFilename(), commitDetail);
                if (comment != null && !comment.isEmpty()) {
                    String fullComment = getIndentation(line) + commentPrefix + " " + comment;
                    commentedContent.append(fullComment).append("\n");
                    
                    // 추가된 주석 정보 저장
                    addedComments.add(new CommentItem(i + 1, comment, line.trim()));
                }
            }
        }
        
        return new FileCommentResult(commentedContent.toString(), addedComments);
    }
    
    /**
     * 파일에 주석을 적용합니다 (기존 방식).
     */
    private String applyCommentsToFile(String currentContent, GitHubFile file, GitHubCommitDetail commitDetail) {
        FileCommentResult result = applyCommentsToFileWithDetails(currentContent, file, commitDetail);
        return result.commentedContent;
    }
    
    /**
     * 파일 형식에 따른 주석 접두사를 반환합니다.
     */
    private String getCommentPrefix(String filename) {
        String ext = filename.toLowerCase();
        if (ext.endsWith(".py")) {
            return "#";
        } else if (ext.endsWith(".css") || ext.endsWith(".scss")) {
            return "/*";
        } else if (ext.endsWith(".html") || ext.endsWith(".vue")) {
            return "<!--";
        }
        return "//"; // 기본값 (Java, JS, TS, C++, Go 등)
    }
    
    /**
     * 라인에 주석을 추가해야 하는지 판단합니다.
     */
    private boolean shouldAddComment(String line, String filename) {
        String trimmed = line.trim();
        
        if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*") || trimmed.startsWith("#")) {
            return false;
        }
        
        // Java 파일
        if (filename.endsWith(".java")) {
            return trimmed.contains("public ") || 
                   trimmed.contains("private ") || 
                   trimmed.contains("protected ") ||
                   trimmed.contains("class ") ||
                   trimmed.contains("interface ") ||
                   trimmed.contains("@Override") ||
                   (trimmed.contains("extends") && trimmed.contains("Repository"));
        }
        
        // JavaScript/TypeScript
        if (filename.endsWith(".js") || filename.endsWith(".ts") || filename.endsWith(".jsx") || filename.endsWith(".tsx")) {
            return trimmed.startsWith("function ") ||
                   trimmed.startsWith("const ") ||
                   trimmed.startsWith("let ") ||
                   trimmed.contains("=>") ||
                   trimmed.contains("useState") ||
                   trimmed.contains("useEffect");
        }
        
        return false;
    }
    
    /**
     * 컨텍스트를 고려한 주석을 생성합니다.
     */
    private String generateContextualComment(String line, String filename, GitHubCommitDetail commitDetail) {
        String trimmed = line.trim();
        
        // 커밋 메시지에서 힌트 추출
        String commitMsg = commitDetail.getMessage().toLowerCase();
        
        if (commitMsg.contains("refactor")) {
            return "리팩토링: " + generateBasicComment(trimmed, filename);
        } else if (commitMsg.contains("feat") || commitMsg.contains("add")) {
            return "새 기능: " + generateBasicComment(trimmed, filename);
        } else if (commitMsg.contains("fix")) {
            return "버그 수정: " + generateBasicComment(trimmed, filename);
        } else if (commitMsg.contains("docs")) {
            return "문서화: " + generateBasicComment(trimmed, filename);
        }
        
        return generateBasicComment(trimmed, filename);
    }
    
    /**
     * 기본적인 주석을 생성합니다.
     */
    private String generateBasicComment(String line, String filename) {
        return codeCommentService.generateCommentForLine(line, filename);
    }
    
    /**
     * 라인의 들여쓰기를 가져옵니다.
     */
    private String getIndentation(String line) {
        StringBuilder indentation = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == ' ' || c == '\t') {
                indentation.append(c);
            } else {
                break;
            }
        }
        return indentation.toString();
    }
    
    // ========== Inner Classes ==========
    
    /**
     * 내부 클래스로 결과 데이터 구조 정의
     */
    private static class FileCommentResult {
        public final String commentedContent;
        public final List<CommentItem> addedComments;
        
        public FileCommentResult(String commentedContent, List<CommentItem> addedComments) {
            this.commentedContent = commentedContent;
            this.addedComments = addedComments;
        }
    }
}
