package com.mc.mc_server.service;

import com.mc.mc_server.dto.CommentPreviewResponse;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CommentSessionService {
    
    // 임시 세션 저장소 (실제 운영환경에서는 Redis나 데이터베이스 사용 권장)
    private final Map<String, SessionData> sessions = new ConcurrentHashMap<>();
    
    public static class SessionData {
        private final String token;
        private final String owner;
        private final String repo;
        private final String commitSha;
        private final String branch;
        private final long createdAt;
        private Map<String, String> updatedFiles;
        
        public SessionData(String token, String owner, String repo, String commitSha, String branch, 
                          Map<String, String> updatedFiles) {
            this.token = token;
            this.owner = owner;
            this.repo = repo;
            this.commitSha = commitSha;
            this.branch = branch;
            this.updatedFiles = updatedFiles;
            this.createdAt = System.currentTimeMillis();
        }
        
        // Getters
        public String getToken() { return token; }
        public String getOwner() { return owner; }
        public String getRepo() { return repo; }
        public String getCommitSha() { return commitSha; }
        public String getBranch() { return branch; }
        public long getCreatedAt() { return createdAt; }
        public Map<String, String> getUpdatedFiles() { return updatedFiles; }
        
        // Setter for updated files
        public void setUpdatedFiles(Map<String, String> updatedFiles) {
            this.updatedFiles = updatedFiles;
        }
    }
    
    /**
     * 새로운 세션을 생성합니다.
     */
    public String createSession(String token, String owner, String repo, String commitSha, 
                               String branch, Map<String, String> updatedFiles) {
        String sessionId = UUID.randomUUID().toString();
        SessionData sessionData = new SessionData(token, owner, repo, commitSha, branch, updatedFiles);
        sessions.put(sessionId, sessionData);
        
        // 1시간 후 자동 삭제 (메모리 정리)
        scheduleSessionCleanup(sessionId);
        
        return sessionId;
    }
    
    /**
     * 세션 데이터를 조회합니다.
     */
    public SessionData getSession(String sessionId) {
        SessionData sessionData = sessions.get(sessionId);
        if (sessionData == null) {
            throw new RuntimeException("세션을 찾을 수 없습니다. 세션이 만료되었거나 존재하지 않습니다.");
        }
        
        // 1시간 후 만료 체크
        if (System.currentTimeMillis() - sessionData.getCreatedAt() > 3600000) { // 1시간
            sessions.remove(sessionId);
            throw new RuntimeException("세션이 만료되었습니다. 다시 미리보기를 생성해주세요.");
        }
        
        return sessionData;
    }
    
    /**
     * 세션의 파일 내용을 업데이트합니다.
     */
    public void updateSessionFiles(String sessionId, Map<String, String> updatedFiles) {
        SessionData sessionData = getSession(sessionId);
        sessionData.setUpdatedFiles(updatedFiles);
    }
    
    /**
     * 세션을 삭제합니다.
     */
    public void deleteSession(String sessionId) {
        sessions.remove(sessionId);
    }
    
    /**
     * 만료된 세션들을 정리합니다.
     */
    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        sessions.entrySet().removeIf(entry -> 
            currentTime - entry.getValue().getCreatedAt() > 3600000); // 1시간
    }
    
    /**
     * 세션 자동 삭제를 스케줄링합니다.
     */
    private void scheduleSessionCleanup(String sessionId) {
        // 간단한 구현 - 실제로는 스케줄러 사용 권장
        new Thread(() -> {
            try {
                Thread.sleep(3600000); // 1시간 대기
                sessions.remove(sessionId);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 현재 세션 수를 반환합니다 (디버깅용).
     */
    public int getSessionCount() {
        return sessions.size();
    }
}
