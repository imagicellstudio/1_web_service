# XLCfi Platform - OAuth2 Social Login Implementation Guide

## 개요

Google, Kakao 소셜 로그인 구현 가이드입니다. Spring Security OAuth2를 활용하여 간편하게 소셜 로그인을 구현할 수 있습니다.

**작업 날짜:** 2025-11-20

## 지원 소셜 플랫폼

1. **Google** - Google OAuth2
2. **Kakao** - Kakao Login
3. **(향후) Naver** - Naver Login
4. **(향후) Facebook** - Facebook Login

## 구현 단계

### 1. 의존성 추가

**build.gradle.kts:**
```kotlin
dependencies {
    // OAuth2 Client
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
}
```

### 2. application.yml 설정

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # Google OAuth2
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          
          # Kakao OAuth2
          kakao:
            client-id: ${KAKAO_CLIENT_ID}
            client-secret: ${KAKAO_CLIENT_SECRET}
            client-authentication-method: POST
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            scope:
              - profile_nickname
              - account_email
            client-name: Kakao

        provider:
          kakao:
            authorization-uri: https://kauth.kakao.com/oauth/authorize
            token-uri: https://kauth.kakao.com/oauth/token
            user-info-uri: https://kapi.kakao.com/v2/user/me
            user-name-attribute: id
```

### 3. 환경 변수 설정

**.env (또는 시스템 환경 변수):**
```bash
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Kakao OAuth2
KAKAO_CLIENT_ID=your-kakao-client-id
KAKAO_CLIENT_SECRET=your-kakao-client-secret
```

## Google OAuth2 설정

### 1. Google Cloud Console 설정

1. **Google Cloud Console 접속**
   - https://console.cloud.google.com/

2. **프로젝트 생성**
   - 새 프로젝트 만들기

3. **OAuth 동의 화면 설정**
   - APIs & Services → OAuth consent screen
   - User Type: External
   - 앱 이름, 사용자 지원 이메일 입력
   - 범위: email, profile

4. **OAuth 2.0 클라이언트 ID 생성**
   - APIs & Services → Credentials
   - 사용자 인증 정보 만들기 → OAuth 클라이언트 ID
   - 애플리케이션 유형: 웹 애플리케이션
   - 승인된 리디렉션 URI:
     - `http://localhost:8081/login/oauth2/code/google`
     - `https://yourdomain.com/login/oauth2/code/google`

5. **Client ID 및 Client Secret 복사**
   - 환경 변수에 설정

### 2. Google 사용자 정보 구조

```json
{
  "sub": "1234567890",
  "name": "홍길동",
  "given_name": "길동",
  "family_name": "홍",
  "picture": "https://lh3.googleusercontent.com/...",
  "email": "user@gmail.com",
  "email_verified": true,
  "locale": "ko"
}
```

## Kakao OAuth2 설정

### 1. Kakao Developers 설정

1. **Kakao Developers 접속**
   - https://developers.kakao.com/

2. **애플리케이션 추가**
   - 내 애플리케이션 → 애플리케이션 추가하기

3. **플랫폼 설정**
   - 앱 설정 → 플랫폼 → Web 플랫폼 등록
   - 사이트 도메인:
     - `http://localhost:8081`
     - `https://yourdomain.com`

4. **Kakao 로그인 활성화**
   - 제품 설정 → 카카오 로그인 → 활성화 설정
   - Redirect URI 등록:
     - `http://localhost:8081/login/oauth2/code/kakao`
     - `https://yourdomain.com/login/oauth2/code/kakao`

5. **동의항목 설정**
   - 제품 설정 → 카카오 로그인 → 동의항목
   - 닉네임: 필수
   - 이메일: 필수

6. **REST API 키 복사**
   - 앱 설정 → 앱 키 → REST API 키
   - Client ID로 사용
   - Client Secret은 보안 → Client Secret에서 생성

### 2. Kakao 사용자 정보 구조

```json
{
  "id": 1234567890,
  "kakao_account": {
    "profile": {
      "nickname": "홍길동",
      "profile_image_url": "http://k.kakaocdn.net/..."
    },
    "email": "user@kakao.com",
    "email_verified": true
  }
}
```

## 구현 코드

### 1. OAuth2 User 정보 추출

**OAuth2UserInfo.java:**
```java
package com.xlcfi.auth.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
    String getImageUrl();
}
```

**GoogleOAuth2UserInfo.java:**
```java
package com.xlcfi.auth.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("picture");
    }
}
```

**KakaoOAuth2UserInfo.java:**
```java
package com.xlcfi.auth.oauth2;

import java.util.Map;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = 
                (Map<String, Object>) attributes.get("kakao_account");
        return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
    }

    @Override
    public String getName() {
        Map<String, Object> kakaoAccount = 
                (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = 
                    (Map<String, Object>) kakaoAccount.get("profile");
            return profile != null ? (String) profile.get("nickname") : null;
        }
        return null;
    }

    @Override
    public String getImageUrl() {
        Map<String, Object> kakaoAccount = 
                (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount != null) {
            Map<String, Object> profile = 
                    (Map<String, Object>) kakaoAccount.get("profile");
            return profile != null ? (String) profile.get("profile_image_url") : null;
        }
        return null;
    }
}
```

### 2. Custom OAuth2 User Service

**CustomOAuth2UserService.java:**
```java
package com.xlcfi.auth.oauth2;

import com.xlcfi.auth.domain.User;
import com.xlcfi.auth.domain.UserRole;
import com.xlcfi.auth.domain.UserStatus;
import com.xlcfi.auth.domain.Language;
import com.xlcfi.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) 
            throws OAuth2AuthenticationException {
        
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(registrationId, 
                oAuth2User.getAttributes());

        // 사용자 조회 또는 생성
        User user = getOrCreateUser(oAuth2UserInfo);

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private OAuth2UserInfo getOAuth2UserInfo(String registrationId, 
            Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if ("kakao".equals(registrationId)) {
            return new KakaoOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException(
                "지원하지 않는 OAuth2 Provider입니다: " + registrationId);
    }

    private User getOrCreateUser(OAuth2UserInfo oAuth2UserInfo) {
        String email = oAuth2UserInfo.getEmail();
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            // 기존 사용자 업데이트
            User user = userOptional.get();
            user.setName(oAuth2UserInfo.getName());
            // 필요시 프로필 이미지 등 업데이트
            return userRepository.save(user);
        } else {
            // 신규 사용자 생성
            User newUser = User.builder()
                    .email(email)
                    .name(oAuth2UserInfo.getName())
                    .role(UserRole.BUYER)
                    .status(UserStatus.ACTIVE)
                    .language(Language.KO)
                    .provider(oAuth2UserInfo.getProvider())
                    .providerId(oAuth2UserInfo.getProviderId())
                    .build();
            
            log.info("새 소셜 사용자 생성: email={}, provider={}", 
                    email, oAuth2UserInfo.getProvider());
            
            return userRepository.save(newUser);
        }
    }
}
```

### 3. OAuth2 Success Handler

**OAuth2SuccessHandler.java:**
```java
package com.xlcfi.auth.oauth2;

import com.xlcfi.auth.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       Authentication authentication) 
            throws IOException {
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(oAuth2User.getUser());
        String refreshToken = jwtTokenProvider.generateRefreshToken(oAuth2User.getUser());

        // 프론트엔드로 리다이렉트 (토큰 전달)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth2/redirect")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        log.info("OAuth2 로그인 성공: userId={}, provider={}", 
                oAuth2User.getUser().getId(), 
                oAuth2User.getUser().getProvider());

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

### 4. Security Config 업데이트

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            // ... 기존 설정 ...
            
            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService))
                    .successHandler(oAuth2SuccessHandler)
                    .failureUrl("/login?error=true"));

    return http.build();
}
```

## 데이터베이스 스키마 업데이트

소셜 로그인을 위해 users 테이블에 필드 추가:

```sql
ALTER TABLE users ADD COLUMN provider VARCHAR(20);
ALTER TABLE users ADD COLUMN provider_id VARCHAR(255);
ALTER TABLE users ADD COLUMN profile_image_url TEXT;

CREATE INDEX idx_users_provider ON users(provider, provider_id);
```

**Flyway Migration:**
```sql
-- V3__add_oauth2_fields.sql
ALTER TABLE users 
ADD COLUMN provider VARCHAR(20),
ADD COLUMN provider_id VARCHAR(255),
ADD COLUMN profile_image_url TEXT;

CREATE INDEX idx_users_provider ON users(provider, provider_id);
```

## 프론트엔드 통합

### 1. 소셜 로그인 버튼

```jsx
// React 예시
function LoginPage() {
  const handleGoogleLogin = () => {
    window.location.href = 'http://localhost:8081/oauth2/authorization/google';
  };

  const handleKakaoLogin = () => {
    window.location.href = 'http://localhost:8081/oauth2/authorization/kakao';
  };

  return (
    <div>
      <button onClick={handleGoogleLogin}>
        <img src="/google-icon.svg" alt="Google" />
        Continue with Google
      </button>
      
      <button onClick={handleKakaoLogin}>
        <img src="/kakao-icon.svg" alt="Kakao" />
        카카오 로그인
      </button>
    </div>
  );
}
```

### 2. 리다이렉트 처리

```jsx
// OAuth2RedirectHandler.jsx
import { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

function OAuth2RedirectHandler() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  useEffect(() => {
    const accessToken = searchParams.get('accessToken');
    const refreshToken = searchParams.get('refreshToken');
    
    if (accessToken && refreshToken) {
      // 토큰 저장
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // 메인 페이지로 이동
      navigate('/');
    } else {
      // 에러 처리
      navigate('/login?error=true');
    }
  }, [searchParams, navigate]);

  return <div>로그인 처리 중...</div>;
}
```

## 테스트

### 1. 로컬 테스트

```bash
# 1. 애플리케이션 시작
./gradlew :xlcfi-auth-service:bootRun

# 2. 브라우저에서 접속
http://localhost:8081/oauth2/authorization/google
http://localhost:8081/oauth2/authorization/kakao
```

### 2. Postman 테스트

OAuth2는 웹 브라우저 기반이므로 Postman보다 실제 브라우저에서 테스트하는 것이 좋습니다.

## 보안 고려사항

### 1. CSRF 보호

OAuth2 로그인에서는 state 파라미터를 사용하여 CSRF 공격을 방지합니다.

### 2. Redirect URI 화이트리스트

- 허용된 Redirect URI만 등록
- 프로덕션 URL만 사용

### 3. Client Secret 보호

- 환경 변수로 관리
- 코드에 하드코딩 금지
- GitHub 등에 노출 금지

### 4. 이메일 검증

소셜 로그인에서 받은 이메일이 검증되었는지 확인:

```java
if (oAuth2UserInfo.isEmailVerified()) {
    // 이메일 검증됨
} else {
    // 이메일 검증 필요
}
```

## 문제 해결

### 1. Redirect URI 불일치

**증상:** `redirect_uri_mismatch` 오류

**해결:**
- Google Cloud Console / Kakao Developers에서 Redirect URI 재확인
- 프로토콜(http/https), 포트, 경로 정확히 일치해야 함

### 2. 이메일 권한 없음

**증상:** 이메일이 null

**해결:**
- Google: scope에 `email` 추가
- Kakao: 동의항목에서 이메일 필수로 설정

### 3. 프론트엔드 CORS 오류

**해결:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
```

## 다음 단계

OAuth2 소셜 로그인 구현 가이드가 완료되었습니다. 다음 작업:

1. ✅ **Swagger/OpenAPI 문서화** - 완료
2. ✅ **Redis 토큰 블랙리스트** - 완료
3. ✅ **Integration Test** - 완료
4. ✅ **OAuth2 소셜 로그인** - 가이드 완료
5. ⏭️ **Rate Limiting** - API 호출 빈도 제한

## 참고 자료

- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Google OAuth2 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Kakao Login Documentation](https://developers.kakao.com/docs/latest/ko/kakaologin/common)

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

