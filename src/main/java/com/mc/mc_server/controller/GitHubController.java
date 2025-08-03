package com.mc.mc_server.controller;

import com.mc.mc_server.dto.*;
import com.mc.mc_server.entity.User;
import com.mc.mc_server.service.CodeCommentPushService;
import com.mc.mc_server.service.GitHubService;
import com.mc.mc_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/github")
@Tag(name = "GitHub", description = "GitHub API 관련 기능")
public class GitHubController {
    
    private static final Logger logger = LoggerFactory.getLogger(GitHubController.class);
    
    private final GitHubService gitHubService;
    private final UserService userService;
    private final CodeCommentPushService codeCommentPushService;
    
    @Autowired
    public GitHubController(GitHubService gitHubService, UserService userService, CodeCommentPushService codeCommentPushService) {
        this.gitHubService = gitHubService;
        this.userService = userService;
        this.codeCommentPushService = codeCommentPushService;
    }
    
    private String getCurrentUserGitHubToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        
        // authentication.getPrincipal()이 User 객체인 경우
        if (authentication.getPrincipal() instanceof User user) {
            if (user.getGithubToken() == null || user.getGithubToken().isEmpty()) {
                throw new RuntimeException("GitHub 토큰이 설정되지 않았습니다.");
            }
            return user.getGithubToken();
        }
        
        // authentication.getName()으로 이메일을 가져와서 사용자 조회
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        
        if (user.getGithubToken() == null || user.getGithubToken().isEmpty()) {
            throw new RuntimeException("GitHub 토큰이 설정되지 않았습니다.");
        }
        
        return user.getGithubToken();
    }
    
    // ========== 인증 및 사용자 정보 API ==========
    
    @GetMapping("/debug/auth")
    @Operation(summary = "인증 상태 디버그", description = "현재 인증 상태를 확인합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("authentication", auth != null ? auth.toString() : "null");
        debug.put("isAuthenticated", auth != null && auth.isAuthenticated());
        debug.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
        debug.put("principalClass", auth != null ? auth.getPrincipal().getClass().getSimpleName() : "null");
        debug.put("name", auth != null ? auth.getName() : "null");
        debug.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        
        return ResponseEntity.ok(debug);
    }
    
    @GetMapping("/username")
    @Operation(summary = "GitHub 사용자명 조회", description = "GitHub 토큰을 사용하여 사용자명(login)만 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자명 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> getUsername() {
        try {
            String githubToken = getCurrentUserGitHubToken();
            String username = gitHubService.getUsername(githubToken);
            
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Runtime exception: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("General exception: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "사용자명을 가져올 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/user")
    @Operation(summary = "GitHub 사용자 정보 조회", description = "GitHub 토큰을 사용하여 사용자 정보를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> getUserInfo() {
        try {
            String githubToken = getCurrentUserGitHubToken();
            GitHubUser user = gitHubService.getUserInfo(githubToken);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            logger.error("GitHub getUserInfo error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            logger.error("GitHub getUserInfo general exception: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "사용자 정보를 가져올 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/get-username")
    @Operation(summary = "토큰으로 GitHub 사용자명 조회", description = "제공된 GitHub 토큰으로 사용자명을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자명 조회 성공"),
        @ApiResponse(responseCode = "400", description = "토큰이 누락되거나 잘못됨"),
        @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> getUsernameByToken(
            @Parameter(description = "GitHub Personal Access Token", required = true)
            @RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "GitHub 토큰이 필요합니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            String username = gitHubService.getUsername(token.trim());
            
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "사용자명 조회 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/validate-token")
    @Operation(summary = "GitHub 토큰 검증 및 사용자 정보 조회", description = "제공된 GitHub 토큰이 유효한지 검증하고 사용자 정보를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 검증 성공 및 사용자 정보 반환"),
        @ApiResponse(responseCode = "400", description = "잘못된 토큰 또는 토큰이 누락됨"),
        @ApiResponse(responseCode = "401", description = "토큰이 유효하지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> validateTokenAndGetUser(
            @Parameter(description = "GitHub Personal Access Token", required = true)
            @RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "GitHub 토큰이 필요합니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            GitHubUser user = gitHubService.getUserInfo(token.trim());
            
            // 응답에 토큰 유효성 정보 추가
            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", "토큰 검증 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ========== 레포지토리 관리 API ==========
    
    @GetMapping("/repositories")
    @Operation(summary = "레포지토리 목록 조회", description = "사용자의 GitHub 레포지토리 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "레포지토리 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> getRepositories() {
        try {
            String githubToken = getCurrentUserGitHubToken();
            List<GitHubRepository> repositories = gitHubService.getUserRepositories(githubToken);
            return ResponseEntity.ok(repositories);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "레포지토리 목록을 가져올 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/repositories/{owner}/{repo}/commits")
    @Operation(summary = "레포지토리 커밋 목록 조회", description = "특정 레포지토리의 커밋 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "커밋 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> getRepositoryCommits(
            @Parameter(description = "레포지토리 소유자", required = true, example = "username")
            @PathVariable String owner,
            @Parameter(description = "레포지토리 이름", required = true, example = "my-repo")
            @PathVariable String repo,
            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "30")
            @RequestParam(defaultValue = "30") int perPage) {
        try {
            String githubToken = getCurrentUserGitHubToken();
            List<GitHubCommit> commits = gitHubService.getRepositoryCommits(githubToken, owner, repo, page, perPage);
            return ResponseEntity.ok(commits);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "커밋 목록을 가져올 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/repositories/{owner}/{repo}/commits/{sha}")
    @Operation(summary = "커밋 상세 정보 조회", description = "특정 커밋의 상세 정보와 변경 내용을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "커밋 상세 정보 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "GitHub API 호출 실패")
    })
    public ResponseEntity<?> getCommitDetail(
            @Parameter(description = "레포지토리 소유자", required = true, example = "username")
            @PathVariable String owner,
            @Parameter(description = "레포지토리 이름", required = true, example = "my-repo")
            @PathVariable String repo,
            @Parameter(description = "커밋 SHA", required = true, example = "abc123def456")
            @PathVariable String sha) {
        try {
            String githubToken = getCurrentUserGitHubToken();
            GitHubCommitDetail commitDetail = gitHubService.getCommitDetail(githubToken, owner, repo, sha);
            return ResponseEntity.ok(commitDetail);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "커밋 상세 정보를 가져올 수 없습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // ========== AI 주석 시스템 API ==========
    
    @PostMapping("/repositories/{owner}/{repo}/commits/{sha}/preview-comments")
    @Operation(summary = "커밋에 AI 주석 미리보기 생성", description = "특정 커밋의 변경사항에 AI가 생성한 주석 미리보기를 제공합니다. 실제로 푸시하지 않습니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주석 미리보기 생성 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "주석 미리보기 생성 실패")
    })
    public ResponseEntity<?> previewComments(
            @Parameter(description = "레포지토리 소유자", required = true, example = "username")
            @PathVariable String owner,
            @Parameter(description = "레포지토리 이름", required = true, example = "my-repo")
            @PathVariable String repo,
            @Parameter(description = "커밋 SHA", required = true, example = "abc123def456")
            @PathVariable String sha,
            @Parameter(description = "대상 브랜치", example = "main")
            @RequestParam(defaultValue = "main") String branch) {
        try {
            String githubToken = getCurrentUserGitHubToken();
            CommentPreviewResponse preview = codeCommentPushService.generateCommentsPreview(githubToken, owner, repo, sha, branch);
            
            return ResponseEntity.ok(preview);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "주석 미리보기 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/comments/session/{sessionId}")
    @Operation(summary = "세션의 주석 내용 수정", description = "미리보기 세션에 저장된 주석 내용을 수정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주석 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 세션 ID 또는 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<?> updateSessionComments(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable String sessionId,
            @Parameter(description = "수정된 파일 내용", required = true)
            @RequestBody UpdateCommentRequest request) {
        try {
            if (request.getSessionId() == null || !request.getSessionId().equals(sessionId)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "세션 ID가 일치하지 않습니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (request.getUpdatedFiles() == null || request.getUpdatedFiles().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "수정할 파일 내용이 없습니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            codeCommentPushService.updateSessionComments(sessionId, request.getUpdatedFiles());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "주석이 성공적으로 수정되었습니다.");
            response.put("sessionId", sessionId);
            response.put("updatedFileCount", request.getUpdatedFiles().size());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            
            if (e.getMessage().contains("찾을 수 없습니다") || e.getMessage().contains("만료되었습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "주석 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/comments/session/{sessionId}/push")
    @Operation(summary = "세션의 주석을 GitHub에 푸시", description = "미리보기 세션에 저장된 (수정된) 주석들을 실제로 GitHub에 푸시합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주석 푸시 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 세션 ID"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "푸시 실패")
    })
    public ResponseEntity<?> pushSessionComments(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable String sessionId) {
        try {
            String result = codeCommentPushService.pushSessionComments(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("sessionId", sessionId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            
            if (e.getMessage().contains("찾을 수 없습니다") || e.getMessage().contains("만료되었습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "주석 푸시 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @DeleteMapping("/comments/session/{sessionId}")
    @Operation(summary = "주석 미리보기 세션 삭제", description = "미리보기 세션을 수동으로 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "세션 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    public ResponseEntity<?> deleteSession(
            @Parameter(description = "세션 ID", required = true)
            @PathVariable String sessionId) {
        try {
            // 세션 존재 여부 확인을 위해 먼저 조회 시도
            codeCommentPushService.getSessionData(sessionId);
            
            // 세션 삭제
            codeCommentPushService.deleteSession(sessionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "세션이 성공적으로 삭제되었습니다.");
            response.put("sessionId", sessionId);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            
            if (e.getMessage().contains("찾을 수 없습니다") || e.getMessage().contains("만료되었습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "세션 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/repositories/{owner}/{repo}/commits/{sha}/apply-comments")
    @Operation(summary = "커밋에 AI 주석 적용 및 푸시", description = "특정 커밋의 변경사항에 AI가 생성한 주석을 적용하고 GitHub에 푸시합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "주석 적용 및 푸시 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "400", description = "GitHub 토큰이 설정되지 않음"),
        @ApiResponse(responseCode = "500", description = "주석 적용 또는 푸시 실패")
    })
    public ResponseEntity<?> applyCommentsAndPush(
            @Parameter(description = "레포지토리 소유자", required = true, example = "username")
            @PathVariable String owner,
            @Parameter(description = "레포지토리 이름", required = true, example = "my-repo")
            @PathVariable String repo,
            @Parameter(description = "커밋 SHA", required = true, example = "abc123def456")
            @PathVariable String sha,
            @Parameter(description = "대상 브랜치", example = "main")
            @RequestParam(defaultValue = "main") String branch) {
        try {
            String githubToken = getCurrentUserGitHubToken();
            String result = codeCommentPushService.applyCommentsAndPush(githubToken, owner, repo, sha, branch);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            response.put("originalCommit", sha);
            response.put("branch", branch);
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "주석 적용 및 푸시 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
