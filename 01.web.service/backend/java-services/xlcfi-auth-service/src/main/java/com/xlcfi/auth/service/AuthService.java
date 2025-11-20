package com.xlcfi.auth.service;

import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.domain.UserStatus;
import com.xlcfi.auth.dto.*;
import com.xlcfi.auth.repository.UserRepository;
import com.xlcfi.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 회원가입
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("회원가입 시도: email={}", request.getEmail());

        // 이메일 중복 확인
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("AUTH001", "이미 존재하는 이메일입니다");
        }

        // 사용자 생성
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .language(request.getLanguage())
                .build();

        User savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmail());

        return UserResponse.from(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("로그인 시도: email={}", request.getEmail());

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("AUTH002", "이메일 또는 비밀번호가 올바르지 않습니다"));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("AUTH002", "이메일 또는 비밀번호가 올바르지 않습니다");
        }

        // 계정 상태 확인
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException("AUTH003", "정지된 계정입니다");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BusinessException("AUTH004", "비활성화된 계정입니다");
        }

        // 마지막 로그인 시간 업데이트
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Refresh Token을 Redis에 저장 (선택적)
        tokenBlacklistService.saveRefreshToken(user.getId(), refreshToken, 2592000000L); // 30일

        log.info("로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime())
                .user(UserResponse.from(user))
                .build();
    }

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Long userId, String accessToken, String refreshToken) {
        // 토큰을 블랙리스트에 추가
        tokenBlacklistService.addToBlacklist(accessToken, refreshToken);
        
        // Redis에서 Refresh Token 삭제
        tokenBlacklistService.deleteRefreshToken(userId);
        
        log.info("로그아웃 완료: userId={}", userId);
    }

    /**
     * 프로필 조회
     */
    public UserResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH005", "사용자를 찾을 수 없습니다"));

        return UserResponse.from(user);
    }

    /**
     * 프로필 수정
     */
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("프로필 수정: userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH005", "사용자를 찾을 수 없습니다"));

        // 수정 가능한 필드만 업데이트
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }

        User updatedUser = userRepository.save(user);
        log.info("프로필 수정 완료: userId={}", updatedUser.getId());

        return UserResponse.from(updatedUser);
    }

    /**
     * 토큰 갱신
     */
    @Transactional
    public LoginResponse refreshToken(String refreshToken) {
        log.info("토큰 갱신 시도");

        // 리프레시 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("AUTH006", "유효하지 않은 리프레시 토큰입니다");
        }

        // 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("AUTH005", "사용자를 찾을 수 없습니다"));

        // 계정 상태 확인
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException("AUTH007", "활성화된 계정이 아닙니다");
        }

        // 새 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        log.info("토큰 갱신 완료: userId={}", userId);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationTime())
                .user(UserResponse.from(user))
                .build();
    }
}

