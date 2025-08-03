package com.mc.mc_server.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
@Tag(name = "테스트", description = "API 테스트 관련 엔드포인트")
public class TestController {
    
    @GetMapping("/public")
    @Operation(summary = "공개 엔드포인트", description = "인증 없이 접근 가능한 테스트 엔드포인트입니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "성공")
    })
    public ResponseEntity<?> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "이것은 공개 엔드포인트입니다. 인증이 필요하지 않습니다.");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/protected")
    @Operation(summary = "보호된 엔드포인트", description = "JWT 인증이 필요한 테스트 엔드포인트입니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "인증 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    public ResponseEntity<?> protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "이것은 보호된 엔드포인트입니다. 인증이 필요합니다.");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }
}
