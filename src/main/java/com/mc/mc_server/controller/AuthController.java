package com.mc.mc_server.controller;

import com.mc.mc_server.dto.AuthResponse;
import com.mc.mc_server.dto.LoginRequest;
import com.mc.mc_server.dto.SignupRequest;
import com.mc.mc_server.entity.User;
import com.mc.mc_server.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "인증", description = "사용자 인증 관련 API")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (이메일 중복 등)")
    })
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            AuthResponse response = userService.signup(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패 (잘못된 이메일 또는 비밀번호)")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = userService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }
    
    @GetMapping("/users")
    @Operation(summary = "모든 사용자 목록 조회", description = "등록된 모든 사용자의 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.findAllUsers();
            
            List<Map<String, Object>> response = new ArrayList<>();
            for (User user : users) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("githubToken", user.getGithubToken());
                userInfo.put("createdAt", user.getCreatedAt());
                userInfo.put("updatedAt", user.getUpdatedAt());
                response.add(userInfo);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "사용자 목록을 가져올 수 없습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/validate-token")
    @Operation(summary = "토큰 유효성 검증", description = "JWT 토큰의 유효성을 검증합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 검증 완료"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰 형식"),
        @ApiResponse(responseCode = "500", description = "토큰 검증 실패")
    })
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Bearer JWT 토큰", required = true, example = "Bearer eyJhbGciOiJIUzI1NiJ9...")
            @RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "유효하지 않은 토큰 형식입니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            String token = authHeader.substring(7);
            boolean isValid = userService.validateToken(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            
            if (isValid) {
                String email = userService.extractEmailFromToken(token);
                response.put("email", email);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "토큰 검증에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/github-token")
    @Operation(summary = "GitHub 토큰 설정", description = "사용자의 GitHub Personal Access Token을 설정합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "GitHub 토큰 설정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 토큰"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    })
    public ResponseEntity<?> setGitHubToken(
            @Parameter(description = "GitHub Personal Access Token", required = true)
            @RequestParam String githubToken) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "인증되지 않은 사용자입니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            
            if (githubToken == null || githubToken.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "GitHub 토큰이 필요합니다.");
                return ResponseEntity.badRequest().body(error);
            }
            
            user.setGithubToken(githubToken.trim());
            userService.saveUser(user);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "GitHub 토큰이 성공적으로 설정되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "토큰 설정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
