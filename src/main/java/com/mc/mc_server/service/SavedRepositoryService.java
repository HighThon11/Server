package com.mc.mc_server.service;

import com.mc.mc_server.dto.SaveRepositoryRequest;
import com.mc.mc_server.dto.SavedRepositoryResponse;
import com.mc.mc_server.entity.SavedRepository;
import com.mc.mc_server.entity.User;
import com.mc.mc_server.repository.SavedRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SavedRepositoryService {
    
    @Autowired
    private SavedRepositoryRepository savedRepositoryRepository;
    
    /**
     * 레포지토리 저장
     */
    public SavedRepositoryResponse saveRepository(User user, SaveRepositoryRequest request) {
        // 이미 저장된 레포지토리인지 확인
        if (savedRepositoryRepository.existsByUserAndRepositoryId(user, request.getRepositoryId())) {
            throw new RuntimeException("이미 저장된 레포지토리입니다.");
        }
        
        SavedRepository savedRepository = new SavedRepository(
            user,
            request.getRepositoryId(),
            request.getRepositoryName(),
            request.getRepositoryFullName(),
            request.getRepositoryDescription(),
            request.getRepositoryUrl(),
            request.getDefaultBranch(),
            request.getIsPrivate(),
            request.getRepositoryCreatedAt(),
            request.getRepositoryUpdatedAt()
        );
        
        SavedRepository saved = savedRepositoryRepository.save(savedRepository);
        return new SavedRepositoryResponse(saved);
    }
    
    /**
     * 사용자의 저장된 레포지토리 목록 조회
     */
    @Transactional(readOnly = true)
    public List<SavedRepositoryResponse> getSavedRepositories(User user) {
        List<SavedRepository> savedRepositories = savedRepositoryRepository.findByUserOrderByCreatedAtDesc(user);
        return savedRepositories.stream()
                .map(SavedRepositoryResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 레포지토리 저장 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isRepositorySaved(User user, Long repositoryId) {
        return savedRepositoryRepository.existsByUserAndRepositoryId(user, repositoryId);
    }
    
    /**
     * 저장된 레포지토리 삭제
     */
    public void removeSavedRepository(User user, Long repositoryId) {
        Optional<SavedRepository> savedRepository = savedRepositoryRepository.findByUserAndRepositoryId(user, repositoryId);
        if (savedRepository.isEmpty()) {
            throw new RuntimeException("저장된 레포지토리를 찾을 수 없습니다.");
        }
        
        savedRepositoryRepository.delete(savedRepository.get());
    }
    
    /**
     * 사용자의 저장된 레포지토리 개수 조회
     */
    @Transactional(readOnly = true)
    public long getSavedRepositoryCount(User user) {
        return savedRepositoryRepository.countByUser(user);
    }
    
    /**
     * 특정 저장된 레포지토리 상세 조회
     */
    @Transactional(readOnly = true)
    public SavedRepositoryResponse getSavedRepositoryById(User user, Long repositoryId) {
        Optional<SavedRepository> savedRepository = savedRepositoryRepository.findByUserAndRepositoryId(user, repositoryId);
        if (savedRepository.isEmpty()) {
            throw new RuntimeException("저장된 레포지토리를 찾을 수 없습니다.");
        }
        
        return new SavedRepositoryResponse(savedRepository.get());
    }
}
