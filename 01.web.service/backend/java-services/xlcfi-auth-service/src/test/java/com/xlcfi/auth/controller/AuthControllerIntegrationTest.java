package com.xlcfi.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlcfi.auth.domain.Language;
import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.domain.UserRole;
import com.xlcfi.auth.domain.UserStatus;
import com.xlcfi.auth.dto.LoginRequest;
import com.xlcfi.auth.dto.RegisterRequest;
import com.xlcfi.auth.dto.UpdateProfileRequest;
import com.xlcfi.auth.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController Integration Test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("인증 API 통합 테스트")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .name("테스트유저")
                .phone("010-1234-5678")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .language(Language.KO)
                .build();
        testUser = userRepository.save(testUser);
    }

    @AfterEach
    void tearDown() {
        // 테스트 데이터 정리
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password123!@#")
                .name("신규유저")
                .phone("010-9999-8888")
                .role(UserRole.BUYER)
                .language(Language.KO)
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.name").value("신규유저"))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다"));

        // 데이터베이스 확인
        User savedUser = userRepository.findByEmail("newuser@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("신규유저");
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_DuplicateEmail() throws Exception {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")  // 이미 존재하는 이메일
                .password("password123!@#")
                .name("중복유저")
                .phone("010-8888-9999")
                .role(UserRole.BUYER)
                .language(Language.KO)
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        // When & Then
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andReturn();

        // AccessToken 추출
        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response)
                .get("data")
                .get("accessToken")
                .asText();

        assertThat(accessToken).isNotNull();
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_WrongPassword() throws Exception {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_Success() throws Exception {
        // Given - 먼저 로그인
        String token = loginAndGetToken();

        // When & Then
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.name").value("테스트유저"));
    }

    @Test
    @DisplayName("프로필 조회 실패 - 토큰 없음")
    void getProfile_NoToken() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_Success() throws Exception {
        // Given
        String token = loginAndGetToken();
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .name("수정된이름")
                .phone("010-7777-6666")
                .language(Language.EN)
                .build();

        // When & Then
        mockMvc.perform(put("/api/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("수정된이름"))
                .andExpect(jsonPath("$.data.phone").value("010-7777-6666"));

        // 데이터베이스 확인
        User updatedUser = userRepository.findById(testUser.getId()).orElse(null);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getName()).isEqualTo("수정된이름");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() throws Exception {
        // Given
        String[] tokens = loginAndGetTokens();
        String accessToken = tokens[0];
        String refreshToken = tokens[1];

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Refresh-Token", refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그아웃되었습니다"));

        // 로그아웃 후 같은 토큰으로 API 호출 시도
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    // Helper Methods

    private String loginAndGetToken() throws Exception {
        return loginAndGetTokens()[0];
    }

    private String[] loginAndGetTokens() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(response)
                .get("data")
                .get("accessToken")
                .asText();
        String refreshToken = objectMapper.readTree(response)
                .get("data")
                .get("refreshToken")
                .asText();

        return new String[]{accessToken, refreshToken};
    }
}

