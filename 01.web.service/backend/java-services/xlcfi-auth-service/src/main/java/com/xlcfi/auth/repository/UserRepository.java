package com.xlcfi.auth.repository;

import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 이메일 중복 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 이메일과 상태로 사용자 조회
     */
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
}

