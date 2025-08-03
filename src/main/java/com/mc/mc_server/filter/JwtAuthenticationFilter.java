package com.mc.mc_server.filter;

import com.mc.mc_server.entity.User;
import com.mc.mc_server.service.UserService;
import com.mc.mc_server.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authorizationHeader);
        
        String email = null;
        String jwt = null;
        
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("JWT Token: " + jwt.substring(0, Math.min(jwt.length(), 20)) + "...");
            try {
                email = jwtUtil.extractUsername(jwt);
                System.out.println("Extracted email: " + email);
            } catch (Exception e) {
                logger.error("JWT 토큰에서 사용자명을 추출할 수 없습니다", e);
            }
        }
        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                User user = userService.findByEmail(email);
                System.out.println("User loaded: " + user.getEmail());
                
                if (jwtUtil.validateToken(jwt, user)) {
                    System.out.println("Token is valid, setting authentication");
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("Token validation failed");
                }
            } catch (Exception e) {
                logger.error("사용자를 찾을 수 없습니다: " + email, e);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
