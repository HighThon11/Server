package com.mc.mc_server.repository;

import com.mc.mc_server.entity.SavedRepository;
import com.mc.mc_server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedRepositoryRepository extends JpaRepository<SavedRepository, Long> {
    
    /**
     * 특정 사용자의 저장된 레포지토리 목록 조회
     */
    List<SavedRepository> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 특정 사용자가 특정 레포지토리를 저장했는지 확인
     */
    Optional<SavedRepository> findByUserAndRepositoryId(User user, Long repositoryId);
    
    /**
     * 특정 사용자가 저장한 레포지토리 개수
     */
    long countByUser(User user);
    
    /**
     * 특정 사용자의 특정 레포지토리 삭제
     */
    void deleteByUserAndRepositoryId(User user, Long repositoryId);
    
    /**
     * 레포지토리 ID로 중복 확인
     */
    boolean existsByUserAndRepositoryId(User user, Long repositoryId);
}
