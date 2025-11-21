package com.xlcfi.auth.controller;

import com.xlcfi.auth.dto.*;
import com.xlcfi.auth.service.AuthService;
import com.xlcfi.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증 (Authentication)", description = "회원가입, 로그인, 프로필 관리 API")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "회원가입",
            description = "새로운 사용자를 등록합니다. 이메일 중복 확인이 자동으로 수행됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (이메일 중복, Validation 실패)"
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("회원가입 요청: email={}", request.getEmail());
        
        UserResponse user = authService.register(request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "회원가입이 완료되었습니다"));
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "로그인 실패 (이메일 또는 비밀번호 오류)"
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("로그인 요청: email={}", request.getEmail());
        
        LoginResponse response = authService.login(request);
        
        return ResponseEntity.ok(
                ApiResponse.success(response, "로그인에 성공했습니다"));
    }

    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Refresh Token"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Parameter(description = "Refresh Token", required = true)
            @RequestHeader("Refresh-Token") String refreshToken) {
        
        log.info("토큰 갱신 요청");
        
        LoginResponse response = authService.refreshToken(refreshToken);
        
        return ResponseEntity.ok(
                ApiResponse.success(response, "토큰이 갱신되었습니다"));
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 조회 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요"
            )
    })
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        
        log.info("프로필 조회 요청: userId={}", userId);
        
        UserResponse user = authService.getProfile(userId);
        
        return ResponseEntity.ok(
                ApiResponse.success(user, "프로필 조회 성공"));
    }

    @Operation(
            summary = "프로필 수정",
            description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "프로필 수정 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요"
            )
    })
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        
        log.info("프로필 수정 요청: userId={}", userId);
        
        UserResponse user = authService.updateProfile(userId, request);
        
        return ResponseEntity.ok(
                ApiResponse.success(user, "프로필이 수정되었습니다"));
    }

    @Operation(
            summary = "로그아웃",
            description = "로그아웃합니다. Access Token과 Refresh Token을 블랙리스트에 추가하여 무효화합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 필요"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId,
            @Parameter(description = "Access Token", required = true) @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "Refresh Token", required = true) @RequestHeader("Refresh-Token") String refreshToken) {
        
        log.info("로그아웃 요청: userId={}", userId);
        
        // Bearer 접두사 제거
        String accessToken = authHeader.replace("Bearer ", "");
        
        authService.logout(userId, accessToken, refreshToken);
        
        return ResponseEntity.ok(
                ApiResponse.success(null, "로그아웃되었습니다"));
    }
}
