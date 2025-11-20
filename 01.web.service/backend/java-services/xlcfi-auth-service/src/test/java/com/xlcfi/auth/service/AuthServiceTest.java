package com.xlcfi.auth.service;

import com.xlcfi.auth.domain.Language;
import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.domain.UserRole;
import com.xlcfi.auth.domain.UserStatus;
import com.xlcfi.auth.dto.LoginRequest;
import com.xlcfi.auth.dto.LoginResponse;
import com.xlcfi.auth.dto.RegisterRequest;
import com.xlcfi.auth.dto.UserResponse;
import com.xlcfi.auth.repository.UserRepository;
import com.xlcfi.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AuthService Unit Test
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("인증 서비스 단위 테스트")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .name("테스트유저")
                .phone("010-1234-5678")
                .role(UserRole.BUYER)
                .status(UserStatus.ACTIVE)
                .language(Language.KO)
                .build();
    }

    @Test
    @DisplayName("회원가입 성공")
    void register_Success() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .name("신규유저")
                .phone("010-9999-8888")
                .role(UserRole.BUYER)
                .language(Language.KO)
                .build();

        given(userRepository.existsByEmail(anyString())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // When
        UserResponse response = authService.register(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        verify(userRepository, times(1)).existsByEmail(request.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void register_DuplicateEmail() {
        // Given
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password123")
                .name("중복유저")
                .phone("010-8888-9999")
                .role(UserRole.BUYER)
                .language(Language.KO)
                .build();

        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이미 존재하는 이메일입니다");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(testUser)).willReturn("accessToken");
        given(jwtTokenProvider.generateRefreshToken(testUser)).willReturn("refreshToken");
        given(jwtTokenProvider.getAccessTokenExpirationTime()).willReturn(3600L);

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(tokenBlacklistService, times(1)).saveRefreshToken(eq(testUser.getId()), eq("refreshToken"), anyLong());
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_WrongPassword() {
        // Given
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("이메일 또는 비밀번호가 올바르지 않습니다");
    }

    @Test
    @DisplayName("로그인 실패 - 정지된 계정")
    void login_SuspendedAccount() {
        // Given
        testUser.setStatus(UserStatus.SUSPENDED);
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(request.getPassword(), testUser.getPasswordHash())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("정지된 계정입니다");
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_Success() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        UserResponse response = authService.getProfile(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(response.getName()).isEqualTo(testUser.getName());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // Given
        Long userId = 1L;
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        // When
        authService.logout(userId, accessToken, refreshToken);

        // Then
        verify(tokenBlacklistService, times(1)).addToBlacklist(accessToken, refreshToken);
        verify(tokenBlacklistService, times(1)).deleteRefreshToken(userId);
    }
}

