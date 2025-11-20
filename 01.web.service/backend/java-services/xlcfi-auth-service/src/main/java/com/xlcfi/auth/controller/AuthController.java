package com.xlcfi.auth.controller;

import com.xlcfi.auth.dto.*;
import com.xlcfi.auth.service.AuthService;
import com.xlcfi.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("회원가입 요청: email={}", request.getEmail());
        
        UserResponse user = authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "회원가입이 완료되었습니다"));
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("로그인 요청: email={}", request.getEmail());
        
        LoginResponse response = authService.login(request);
        
        return ResponseEntity.ok(
                ApiResponse.success(response, "로그인에 성공했습니다"));
    }

    /**
     * 토큰 갱신
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @RequestHeader("Refresh-Token") String refreshToken) {
        
        log.info("토큰 갱신 요청");
        
        LoginResponse response = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(
                ApiResponse.success(response, "토큰이 갱신되었습니다"));
    }

    /**
     * 내 프로필 조회
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @RequestAttribute("userId") Long userId) {
        
        log.info("프로필 조회 요청: userId={}", userId);
        
        UserResponse user = authService.getProfile(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(user, "프로필 조회 성공"));
    }

    /**
     * 프로필 수정
     * PUT /api/auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        log.info("프로필 수정 요청: userId={}", userId);
        
        UserResponse user = authService.updateProfile(userId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(user, "프로필이 수정되었습니다"));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     * Note: JWT 방식에서는 클라이언트에서 토큰을 삭제하면 됨
     * 서버에서는 필요시 토큰 블랙리스트 관리
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestAttribute("userId") Long userId) {
        
        log.info("로그아웃 요청: userId={}", userId);
        
        // JWT 방식에서는 클라이언트에서 토큰 삭제
        // 필요시 Redis 등에 토큰 블랙리스트 추가
        
        return ResponseEntity.ok(
                ApiResponse.success(null, "로그아웃되었습니다"));
    }
}

