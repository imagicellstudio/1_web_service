# XLCfi Platform - Swagger API Documentation Guide

## 개요

Swagger/OpenAPI를 사용한 API 문서화가 완료되었습니다.

**작업 완료 날짜:** 2025-11-20

## Swagger UI 접근

### 개발 환경

각 서비스별로 Swagger UI에 접근할 수 있습니다:

| 서비스 | URL |
|--------|-----|
| Auth Service | http://localhost:8081/swagger-ui/index.html |
| Product Service | http://localhost:8082/swagger-ui/index.html |
| Order Service | http://localhost:8083/swagger-ui/index.html |
| Payment Service | http://localhost:8084/swagger-ui/index.html |
| Review Service | http://localhost:8085/swagger-ui/index.html |

### API Docs (JSON)

| 서비스 | URL |
|--------|-----|
| Auth Service | http://localhost:8081/v3/api-docs |
| Product Service | http://localhost:8082/v3/api-docs |

## JWT 인증 설정

### 1. Swagger UI에서 인증하기

1. **로그인 API 실행**
   - `POST /api/auth/login` 엔드포인트 실행
   - 응답에서 `accessToken` 복사

2. **Authorize 버튼 클릭**
   - Swagger UI 우측 상단의 🔒 **Authorize** 버튼 클릭
   
3. **토큰 입력**
   - Value 필드에 토큰만 입력 (Bearer 접두사는 자동 추가됨)
   - 예: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
   
4. **Authorize 버튼 클릭**
   - 인증이 완료되면 모든 API에서 자동으로 토큰이 포함됨

### 2. 테스트 계정

seed-data.sql을 실행한 경우 다음 계정을 사용할 수 있습니다:

```json
{
  "email": "buyer1@xlcfi.com",
  "password": "password123"
}
```

## 구현된 기능

### 1. Swagger Configuration

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/config/SwaggerConfig.java`

**주요 설정:**
- API 정보 (제목, 설명, 버전, 연락처)
- 서버 목록 (개발/프로덕션)
- JWT 인증 스키마 (Bearer Token)
- Security Requirement

### 2. API 어노테이션

각 Controller에 다음 어노테이션이 추가되었습니다:

- `@Tag`: API 그룹화
- `@Operation`: 엔드포인트 설명
- `@ApiResponses`: 응답 코드 및 설명
- `@Parameter`: 파라미터 설명
- `@SecurityRequirement`: 인증 필요 여부

### 3. 예시: Auth Controller

```java
@Tag(name = "인증 (Authentication)", description = "회원가입, 로그인, 프로필 관리 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "로그인 실패")
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(...) {
        // ...
    }
}
```

## API 그룹

Swagger UI에서 다음과 같이 그룹화되어 표시됩니다:

1. **인증 (Authentication)**
   - 회원가입, 로그인, 프로필 관리

2. **카테고리 (Categories)**
   - 카테고리 조회, 검색

3. **상품 (Products)**
   - 상품 CRUD, 검색, 필터링

4. **주문 (Orders)**
   - 주문 생성, 조회, 관리

5. **결제 (Payments)**
   - 결제 처리, 환불

6. **리뷰 (Reviews)**
   - 리뷰 작성, 조회, 관리

## 사용 예시

### 1. 회원가입

```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123!@#",
  "name": "테스트유저",
  "phone": "010-1234-5678",
  "role": "BUYER",
  "language": "KO"
}
```

### 2. 로그인 및 토큰 획득

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123!@#"
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

### 3. 인증이 필요한 API 테스트

Swagger UI에서:
1. 🔒 Authorize 버튼 클릭
2. accessToken 입력
3. Authorize 클릭
4. 이제 모든 API 테스트 가능

## 추가 설정

### application.yml

```yaml
# Swagger 설정
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method
    tags-sorter: alpha
    display-request-duration: true
    doc-expansion: none
```

### build.gradle.kts

```kotlin
dependencies {
    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
}
```

## 주의사항

### 1. 프로덕션 환경

프로덕션 환경에서는 Swagger UI를 비활성화하거나 접근을 제한해야 합니다:

```yaml
# application-prod.yml
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false
```

또는 Security 설정에서 특정 IP만 허용:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**")
        .hasIpAddress("192.168.1.0/24")  // 내부 IP만 허용
    );
}
```

### 2. @Parameter(hidden = true)

컨트롤러에서 `@RequestAttribute`로 받는 파라미터는 Swagger UI에 표시되지 않도록 설정:

```java
@GetMapping("/profile")
public ResponseEntity<?> getProfile(
        @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
    // userId는 JWT 필터에서 자동 주입
}
```

## Swagger 커스터마이징

### 1. 그룹별로 API 분리

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
            .group("public")
            .pathsToMatch("/api/auth/**", "/api/products/**")
            .build();
}

@Bean
public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
            .group("admin")
            .pathsToMatch("/api/admin/**")
            .build();
}
```

### 2. 예제 값 추가

```java
@Schema(example = "user@example.com")
private String email;

@Schema(example = "password123!@#", minLength = 8)
private String password;
```

## 다음 단계

Swagger 문서화가 완료되었습니다. 이제 다음 작업을 진행할 수 있습니다:

1. ✅ **Swagger/OpenAPI 문서화** - 완료
2. ⏭️ **Redis 토큰 블랙리스트** - 로그아웃 구현
3. ⏭️ **Integration Test** - 인증/인가 테스트
4. ⏭️ **OAuth2 소셜 로그인** - Google, Kakao
5. ⏭️ **Rate Limiting** - API 호출 빈도 제한

## 참고 자료

- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

