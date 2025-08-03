package com.mc.mc_server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public class SignupRequest {
    
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    @Schema(description = "사용자 이메일", example = "user@example.com", required = true)
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 6, message = "비밀번호는 최소 6자 이상이어야 합니다.")
    @Schema(description = "사용자 비밀번호", example = "password123", required = true, minLength = 6)
    private String password;
    
    @Schema(description = "GitHub 토큰", example = "ghp_xxxxxxxxxxxxxxxxxxxx", required = false)
    private String githubToken;
    
    public SignupRequest() {}
    
    public SignupRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    public SignupRequest(String email, String password, String githubToken) {
        this.email = email;
        this.password = password;
        this.githubToken = githubToken;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getGithubToken() {
        return githubToken;
    }
    
    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
}
