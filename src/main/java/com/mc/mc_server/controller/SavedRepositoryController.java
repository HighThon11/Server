package com.mc.mc_server.controller;

import com.mc.mc_server.dto.SaveRepositoryRequest;
import com.mc.mc_server.dto.SavedRepositoryResponse;
import com.mc.mc_server.entity.User;
import com.mc.mc_server.service.SavedRepositoryService;
import com.mc.mc_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved-repositories")
@Tag(name = "저장된 레포지토리", description = "사용자가 저장한 GitHub 레포지토리 관리 API")
public class SavedRepositoryController {
    
    @Autowired
    private SavedRepositoryService savedRepositoryService;
    
    @Autowired
    @Lazy
    private UserService userService;
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getName())) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        
        // authentication.getPrincipal()이 User 객체인 경우
        if (authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        
        // authentication.getName()으로 이메일을 가져와서 사용자 조회
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        
        return user;
    }
    
    @GetMapping("/debug/auth")
    @Operation(summary = "인증 상태 디버그", description = "현재 인증 상태를 확인합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> debugAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("authentication", auth != null ? auth.toString() : "null");
        debug.put("isAuthenticated", auth != null ? auth.isAuthenticated() : false);
        debug.put("principal", auth != null ? auth.getPrincipal().toString() : "null");
        debug.put("principalClass", auth != null ? auth.getPrincipal().getClass().getSimpleName() : "null");
        debug.put("name", auth != null ? auth.getName() : "null");
        debug.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");
        
        return ResponseEntity.ok(debug);
    }
    
    @Operation(summary = "레포지토리 저장", description = "GitHub 레포지토리를 사용자 계정에 저장합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PostMapping
    public ResponseEntity<?> saveRepository(
            @Valid @RequestBody SaveRepositoryRequest request) {
        try {
            User user = getCurrentUser();
            SavedRepositoryResponse response = savedRepositoryService.saveRepository(user, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @Operation(summary = "저장된 레포지토리 목록 조회", description = "사용자가 저장한 모든 GitHub 레포지토리 목록을 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping
    public ResponseEntity<List<SavedRepositoryResponse>> getSavedRepositories() {
        try {
            User user = getCurrentUser();
            List<SavedRepositoryResponse> savedRepositories = savedRepositoryService.getSavedRepositories(user);
            return ResponseEntity.ok(savedRepositories);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "레포지토리 저장 여부 확인", description = "특정 레포지토리가 이미 저장되어 있는지 확인합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/check/{repositoryId}")
    public ResponseEntity<Map<String, Boolean>> checkRepositorySaved(
            @Parameter(description = "GitHub 레포지토리 ID", required = true)
            @PathVariable Long repositoryId) {
        try {
            User user = getCurrentUser();
            boolean isSaved = savedRepositoryService.isRepositorySaved(user, repositoryId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isSaved", isSaved);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Boolean> errorResponse = new HashMap<>();
            errorResponse.put("isSaved", false);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @Operation(summary = "저장된 레포지토리 삭제", description = "저장된 GitHub 레포지토리를 삭제합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @DeleteMapping("/{repositoryId}")
    public ResponseEntity<?> removeSavedRepository(
            @Parameter(description = "GitHub 레포지토리 ID", required = true)
            @PathVariable Long repositoryId) {
        try {
            User user = getCurrentUser();
            savedRepositoryService.removeSavedRepository(user, repositoryId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "저장된 레포지토리가 성공적으로 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @Operation(summary = "저장된 레포지토리 개수 조회", description = "사용자가 저장한 레포지토리 총 개수를 조회합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/count")
    public ResponseEntity<?> getSavedRepositoryCount() {
        try {
            // 디버깅 정보 추가
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("SavedRepo - Authentication: " + auth);
            System.out.println("SavedRepo - Authenticated: " + (auth != null ? auth.isAuthenticated() : "null"));
            System.out.println("SavedRepo - Principal: " + (auth != null ? auth.getPrincipal() : "null"));
            System.out.println("SavedRepo - Principal Class: " + (auth != null ? auth.getPrincipal().getClass().getSimpleName() : "null"));
            System.out.println("SavedRepo - Name: " + (auth != null ? auth.getName() : "null"));
            
            User user = getCurrentUser();
            long count = savedRepositoryService.getSavedRepositoryCount(user);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("debug", Map.of(
                "authenticated", auth != null ? auth.isAuthenticated() : false,
                "principalClass", auth != null ? auth.getPrincipal().getClass().getSimpleName() : "null",
                "userName", auth != null ? auth.getName() : "null"
            ));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.out.println("SavedRepo - Error: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("count", 0L);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
