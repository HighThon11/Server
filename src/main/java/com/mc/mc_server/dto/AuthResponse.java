package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 응답")
public class AuthResponse {
    
    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIn0...")
    private String token;
    
    @Schema(description = "응답 메시지", example = "로그인 성공")
    private String message;
    
    @Schema(description = "GitHub 토큰", example = "ghp_xxxxxxxxxxxxxxxxxxxx")
    private String githubToken;
    
    public AuthResponse() {}
    
    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }
    
    public AuthResponse(String token, String message, String githubToken) {
        this.token = token;
        this.message = message;
        this.githubToken = githubToken;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getGithubToken() {
        return githubToken;
    }
    
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
}
