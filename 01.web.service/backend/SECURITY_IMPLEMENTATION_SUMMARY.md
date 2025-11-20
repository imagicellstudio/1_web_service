# XLCfi Platform - Security Implementation Summary

## 개요

이 문서는 XLCfi 플랫폼의 JWT 기반 인증/인가 시스템 구현 내역을 요약합니다.

**작업 완료 날짜:** 2025-11-20

## 구현 완료 항목

### 1. JWT 인증 필터

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/security/JwtAuthenticationFilter.java`

**주요 기능:**
- HTTP 요청마다 JWT 토큰 검증
- Authorization Header에서 Bearer 토큰 추출
- 토큰 유효성 검증
- SecurityContext에 인증 정보 설정
- Request Attribute에 사용자 정보 저장 (userId, email, role)

**처리 흐름:**
```
1. Request Header에서 "Authorization: Bearer {token}" 추출
2. JWT 토큰 유효성 검증
3. 토큰에서 사용자 정보 추출 (userId, email, role)
4. Authentication 객체 생성
5. SecurityContext에 인증 정보 설정
6. Request Attribute 설정 (Controller에서 사용)
```

### 2. JWT 예외 처리

#### JwtAuthenticationEntryPoint

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/security/JwtAuthenticationEntryPoint.java`

**역할:** 인증 실패 시 처리 (401 Unauthorized)

**응답 예시:**
```json
{
  "success": false,
  "data": null,
  "message": "인증이 필요합니다",
  "errors": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다",
    "path": "/api/..."
  }
}
```

#### JwtAccessDeniedHandler

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/security/JwtAccessDeniedHandler.java`

**역할:** 권한 없음 시 처리 (403 Forbidden)

**응답 예시:**
```json
{
  "success": false,
  "data": null,
  "message": "접근 권한이 없습니다",
  "errors": {
    "code": "FORBIDDEN",
    "message": "접근 권한이 없습니다",
    "path": "/api/..."
  }
}
```

### 3. Security Configuration

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/config/SecurityConfig.java`

**주요 설정:**

#### 세션 관리
- **Stateless**: 세션을 사용하지 않음 (JWT 사용)
- SessionCreationPolicy.STATELESS

#### CSRF 보호
- **비활성화**: JWT 사용 시 CSRF 보호 불필요

#### 권한 설정

| 엔드포인트 | 접근 권한 |
|-----------|----------|
| `/api/auth/register` | 공개 |
| `/api/auth/login` | 공개 |
| `/api/auth/refresh` | 공개 |
| `/api/products/**` (GET) | 공개 |
| `/api/categories/**` (GET) | 공개 |
| `/api/reviews/product/**` (GET) | 공개 |
| `/api/reviews/latest` (GET) | 공개 |
| `/swagger-ui/**` | 공개 (개발 환경) |
| 그 외 모든 엔드포인트 | 인증 필요 |

#### 필터 체인
```
JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ...
```

### 4. 커스텀 어노테이션

#### @CurrentUser

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/annotation/CurrentUser.java`

**용도:** 현재 로그인한 사용자 ID 주입

**사용 예시:**
```java
@GetMapping("/profile")
public ResponseEntity<ApiResponse<UserResponse>> getProfile(
        @CurrentUser Long userId) {
    // userId는 JWT에서 자동으로 추출됨
    UserResponse user = userService.getProfile(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
}
```

#### @RequireRole

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/annotation/RequireRole.java`

**용도:** Role 기반 접근 제어

**사용 예시:**
```java
@RequireRole({"ADMIN"})
@DeleteMapping("/{userId}")
public ResponseEntity<ApiResponse<Void>> deleteUser(
        @PathVariable Long userId) {
    // ADMIN 권한이 있는 사용자만 접근 가능
    userService.deleteUser(userId);
    return ResponseEntity.ok(ApiResponse.success(null));
}
```

### 5. Role 검사 AOP

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/aspect/RoleCheckAspect.java`

**역할:** @RequireRole 어노테이션이 붙은 메서드의 권한 검사

**처리 흐름:**
```
1. @RequireRole 어노테이션 감지
2. Request Attribute에서 사용자 role 추출
3. 허용된 role 목록과 비교
4. 권한이 없으면 BusinessException 발생
```

## JWT 토큰 구조

### Access Token

**유효 기간:** 1시간 (3600초)

**Claims:**
```json
{
  "sub": "123",              // 사용자 ID
  "email": "user@example.com",
  "role": "BUYER",
  "type": "access",
  "iat": 1234567890,         // 발급 시간
  "exp": 1234571490          // 만료 시간
}
```

### Refresh Token

**유효 기간:** 30일 (2592000초)

**Claims:**
```json
{
  "sub": "123",              // 사용자 ID
  "type": "refresh",
  "iat": 1234567890,         // 발급 시간
  "exp": 1237159890          // 만료 시간
}
```

## 사용 방법

### 1. 회원가입

**Request:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123!@#",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "BUYER",
  "language": "KO"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "BUYER",
    ...
  },
  "message": "회원가입이 완료되었습니다"
}
```

### 2. 로그인

**Request:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123!@#"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "BUYER"
    }
  },
  "message": "로그인에 성공했습니다"
}
```

### 3. 인증이 필요한 API 호출

**Request:**
```http
GET /api/auth/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "role": "BUYER",
    ...
  },
  "message": "프로필 조회 성공"
}
```

### 4. 토큰 갱신

**Request:**
```http
POST /api/auth/refresh
Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // 새 토큰
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // 새 토큰
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "BUYER"
    }
  },
  "message": "토큰이 갱신되었습니다"
}
```

## 환경 변수 설정

### application.yml

```yaml
jwt:
  secret: ${JWT_SECRET:xlcfi-secret-key...}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION:3600000}  # 1시간
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:2592000000}  # 30일
```

### 환경 변수 (권장)

```bash
# .env 파일 또는 시스템 환경 변수
export JWT_SECRET="your-256-bit-secret-key-here"
export JWT_ACCESS_EXPIRATION=3600000
export JWT_REFRESH_EXPIRATION=2592000000
```

**중요:** 프로덕션 환경에서는 반드시 강력한 시크릿 키를 사용하세요!

## 보안 권장사항

### 1. JWT Secret Key
- ✅ 최소 256비트 (32바이트) 이상
- ✅ 랜덤하고 예측 불가능한 문자열
- ✅ 환경 변수로 관리
- ❌ 코드에 하드코딩 금지

### 2. HTTPS 사용
- ✅ 프로덕션 환경에서 반드시 HTTPS 사용
- ✅ 토큰 탈취 방지

### 3. 토큰 저장
- ✅ 클라이언트: HttpOnly Cookie 또는 메모리
- ❌ LocalStorage 저장 지양 (XSS 공격 위험)

### 4. Refresh Token
- ✅ Refresh Token은 별도로 안전하게 저장
- ✅ Access Token 만료 시에만 사용
- ✅ 의심스러운 활동 감지 시 즉시 무효화

### 5. 토큰 블랙리스트 (선택)
- Redis를 활용한 로그아웃 토큰 블랙리스트
- 토큰 탈취 의심 시 즉시 무효화

## Role 정의

### UserRole Enum

```java
public enum UserRole {
    BUYER,   // 구매자
    SELLER,  // 판매자
    ADMIN    // 관리자
}
```

### 권한 매트릭스

| 기능 | BUYER | SELLER | ADMIN |
|------|-------|--------|-------|
| 상품 조회 | ✓ | ✓ | ✓ |
| 상품 등록 | ✗ | ✓ | ✓ |
| 상품 수정/삭제 | ✗ | ✓ (본인) | ✓ |
| 주문 생성 | ✓ | ✗ | ✓ |
| 주문 관리 | ✓ (본인) | ✓ (판매) | ✓ |
| 리뷰 작성 | ✓ | ✓ | ✓ |
| 사용자 관리 | ✗ | ✗ | ✓ |

## Controller에서 사용 예시

### 기존 방식 (@RequestAttribute)

```java
@GetMapping("/profile")
public ResponseEntity<ApiResponse<UserResponse>> getProfile(
        @RequestAttribute("userId") Long userId) {
    UserResponse user = authService.getProfile(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
}
```

### 새로운 방식 (@CurrentUser)

```java
@GetMapping("/profile")
public ResponseEntity<ApiResponse<UserResponse>> getProfile(
        @CurrentUser Long userId) {
    UserResponse user = authService.getProfile(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
}
```

### Role 기반 접근 제어

```java
@RequireRole({"ADMIN"})
@DeleteMapping("/users/{userId}")
public ResponseEntity<ApiResponse<Void>> deleteUser(
        @PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.ok(ApiResponse.success(null, "사용자가 삭제되었습니다"));
}
```

```java
@RequireRole({"SELLER", "ADMIN"})
@PostMapping("/products")
public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
        @CurrentUser Long sellerId,
        @Valid @RequestBody ProductRequest request) {
    ProductResponse product = productService.createProduct(sellerId, request);
    return ResponseEntity.ok(ApiResponse.success(product));
}
```

## 테스트

### Postman 테스트 순서

1. **회원가입**
   ```
   POST /api/auth/register
   ```

2. **로그인**
   ```
   POST /api/auth/login
   → accessToken 복사
   ```

3. **인증이 필요한 API 테스트**
   ```
   Headers:
   Authorization: Bearer {accessToken}
   
   GET /api/auth/profile
   ```

4. **토큰 갱신**
   ```
   Headers:
   Refresh-Token: {refreshToken}
   
   POST /api/auth/refresh
   ```

## 문제 해결

### 401 Unauthorized 발생 시

1. Authorization Header 확인
   ```
   Authorization: Bearer {token}
   ```

2. 토큰 만료 확인
   - Access Token: 1시간
   - Refresh Token으로 갱신 필요

3. 토큰 형식 확인
   - "Bearer " 접두사 포함 여부

### 403 Forbidden 발생 시

1. 사용자 Role 확인
2. 엔드포인트에 필요한 권한 확인
3. @RequireRole 설정 확인

## 다음 단계

JWT 인증/인가 시스템 구현이 완료되었습니다. 다음 작업으로 진행할 수 있습니다:

1. **Swagger/OpenAPI 문서화** - JWT 인증 포함
2. **Integration Test 작성** - 인증 테스트
3. **Redis 토큰 블랙리스트** - 로그아웃 구현
4. **OAuth2 소셜 로그인** - Google, Kakao 등
5. **2FA (Two-Factor Authentication)** - 추가 보안

## 참고 문서

- [Controller Layer Summary](./CONTROLLER_LAYER_SUMMARY.md): Controller 구현 내역
- [Service Layer Summary](./SERVICE_LAYER_SUMMARY.md): Service Layer 구현 내역
- [Database Schema Summary](./DATABASE_SCHEMA_SUMMARY.md): 데이터베이스 스키마

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

