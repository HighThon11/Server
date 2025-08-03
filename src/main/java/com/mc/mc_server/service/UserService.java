package com.mc.mc_server.service;

import com.mc.mc_server.dto.AuthResponse;
import com.mc.mc_server.dto.LoginRequest;
import com.mc.mc_server.dto.SignupRequest;
import com.mc.mc_server.entity.User;
import com.mc.mc_server.repository.UserRepository;
import com.mc.mc_server.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }
    
    public AuthResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        // 새 사용자 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setGithubToken(request.getGithubToken());
        
        User savedUser = userRepository.save(user);
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(savedUser);
        
        return new AuthResponse(token, "회원가입이 완료되었습니다.", savedUser.getGithubToken());
    }
    
    public AuthResponse login(LoginRequest request) {
        try {
            // 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            // 인증 성공시 사용자 정보 가져오기
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = findByEmail(userDetails.getUsername());
            
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(userDetails);
            
            return new AuthResponse(token, "로그인 성공", user.getGithubToken());
        } catch (Exception e) {
            throw new RuntimeException("로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.");
        }
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }
    
    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }
    
    public String extractEmailFromToken(String token) {
        return jwtUtil.extractUsername(token);
    }
    
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
    
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
