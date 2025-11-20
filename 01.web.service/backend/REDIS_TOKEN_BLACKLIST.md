# XLCfi Platform - Redis Token Blacklist Implementation

## 개요

Redis를 사용한 JWT 토큰 블랙리스트 시스템이 구현되었습니다. 로그아웃 시 토큰을 무효화하고, 탈취된 토큰의 재사용을 방지합니다.

**작업 완료 날짜:** 2025-11-20

## 구현 완료 항목

### 1. Redis 설정

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/config/RedisConfig.java`

**주요 설정:**
- Redis Connection Factory (Lettuce)
- RedisTemplate (String-Object)
- Key Serializer: StringRedisSerializer
- Value Serializer: GenericJackson2JsonRedisSerializer

**application-dev.yml:**
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 3000ms
```

### 2. TokenBlacklistService

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/service/TokenBlacklistService.java`

**주요 기능:**

#### addToBlacklist(accessToken, refreshToken)
- 로그아웃 시 토큰을 블랙리스트에 추가
- 토큰의 남은 유효 시간만큼만 Redis에 저장 (자동 만료)
- Key 패턴: `blacklist:token:{token}`

#### isBlacklisted(token)
- 토큰이 블랙리스트에 있는지 확인
- JWT 필터에서 매 요청마다 검증

#### saveRefreshToken(userId, refreshToken, expirationMs)
- Refresh Token을 Redis에 저장
- 사용자별로 하나의 Refresh Token만 유지 가능
- Key 패턴: `refresh:token:{userId}`

#### getRefreshToken(userId)
- 사용자의 Refresh Token 조회

#### deleteRefreshToken(userId)
- 로그아웃 시 Refresh Token 삭제

#### invalidateAllSessions(userId)
- 특정 사용자의 모든 세션 무효화
- 강제 로그아웃, 계정 정지 시 사용

### 3. JwtAuthenticationFilter 업데이트

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/security/JwtAuthenticationFilter.java`

**추가된 로직:**
```java
// 토큰이 블랙리스트에 있는지 확인
if (StringUtils.hasText(jwt) && tokenBlacklistService.isBlacklisted(jwt)) {
    log.warn("블랙리스트에 등록된 토큰입니다");
    SecurityContextHolder.clearContext();
    filterChain.doFilter(request, response);
    return;
}
```

### 4. AuthService 업데이트

**로그인 시:**
```java
// Refresh Token을 Redis에 저장
tokenBlacklistService.saveRefreshToken(user.getId(), refreshToken, 2592000000L);
```

**로그아웃 메서드 추가:**
```java
public void logout(Long userId, String accessToken, String refreshToken) {
    // 토큰을 블랙리스트에 추가
    tokenBlacklistService.addToBlacklist(accessToken, refreshToken);
    
    // Redis에서 Refresh Token 삭제
    tokenBlacklistService.deleteRefreshToken(userId);
    
    log.info("로그아웃 완료: userId={}", userId);
}
```

### 5. AuthController 로그아웃 API 업데이트

**파일:** `xlcfi-auth-service/src/main/java/com/xlcfi/auth/controller/AuthController.java`

**엔드포인트:**
```java
@PostMapping("/logout")
public ResponseEntity<ApiResponse<Void>> logout(
        @RequestAttribute("userId") Long userId,
        @RequestHeader("Authorization") String authHeader,
        @RequestHeader("Refresh-Token") String refreshToken) {
    
    String accessToken = authHeader.replace("Bearer ", "");
    authService.logout(userId, accessToken, refreshToken);
    
    return ResponseEntity.ok(ApiResponse.success(null, "로그아웃되었습니다"));
}
```

## Redis 데이터 구조

### 블랙리스트 키 (Access Token)
```
Key: blacklist:token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Value: "true"
TTL: 토큰의 남은 유효 시간 (예: 3600초)
```

### 블랙리스트 키 (Refresh Token)
```
Key: blacklist:token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Value: "true"
TTL: 토큰의 남은 유효 시간 (예: 2592000초)
```

### Refresh Token 저장
```
Key: refresh:token:123
Value: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
TTL: 30일 (2592000초)
```

## 사용 예시

### 1. 로그인

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
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

**Redis 상태:**
```
refresh:token:123 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." (TTL: 2592000초)
```

### 2. 로그아웃

```http
POST /api/auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답:**
```json
{
  "success": true,
  "data": null,
  "message": "로그아웃되었습니다"
}
```

**Redis 상태:**
```
blacklist:token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... = "true" (TTL: 남은 시간)
blacklist:token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... = "true" (TTL: 남은 시간)
refresh:token:123 = (삭제됨)
```

### 3. 로그아웃된 토큰으로 API 호출 시도

```http
GET /api/auth/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**응답:**
```json
{
  "success": false,
  "message": "인증이 필요합니다",
  "errors": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다"
  }
}
```

## 동작 흐름

### 로그인 흐름

```
1. 사용자 로그인 요청
   ↓
2. 인증 성공
   ↓
3. Access Token + Refresh Token 생성
   ↓
4. Refresh Token을 Redis에 저장
   - Key: refresh:token:{userId}
   - Value: refreshToken
   - TTL: 30일
   ↓
5. 클라이언트에 토큰 반환
```

### 로그아웃 흐름

```
1. 로그아웃 요청 (Access Token + Refresh Token 포함)
   ↓
2. Access Token을 블랙리스트에 추가
   - Key: blacklist:token:{accessToken}
   - Value: "true"
   - TTL: 토큰의 남은 유효 시간
   ↓
3. Refresh Token을 블랙리스트에 추가
   - Key: blacklist:token:{refreshToken}
   - Value: "true"
   - TTL: 토큰의 남은 유효 시간
   ↓
4. Redis에서 Refresh Token 삭제
   - Key: refresh:token:{userId}
   ↓
5. 로그아웃 완료
```

### API 요청 검증 흐름

```
1. API 요청 (Authorization Header에 Bearer Token)
   ↓
2. JwtAuthenticationFilter
   ↓
3. 토큰 추출
   ↓
4. 블랙리스트 확인 (Redis)
   - 블랙리스트에 있으면 → 401 Unauthorized
   ↓
5. 토큰 유효성 검증
   - 유효하지 않으면 → 401 Unauthorized
   ↓
6. 인증 정보 설정
   ↓
7. API 처리
```

## Redis CLI 명령어

### 1. 모든 블랙리스트 토큰 조회

```bash
redis-cli KEYS "blacklist:token:*"
```

### 2. 특정 사용자의 Refresh Token 조회

```bash
redis-cli GET "refresh:token:123"
```

### 3. 블랙리스트에 있는지 확인

```bash
redis-cli EXISTS "blacklist:token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 4. TTL 확인

```bash
redis-cli TTL "blacklist:token:eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 5. 모든 블랙리스트 삭제 (테스트용)

```bash
redis-cli KEYS "blacklist:token:*" | xargs redis-cli DEL
```

## 보안 고려사항

### 1. TTL 설정

토큰의 남은 유효 시간만큼만 블랙리스트에 보관하여 Redis 메모리 절약:

```java
long accessTokenExpiration = jwtTokenProvider.getExpirationTime(accessToken);
redisTemplate.opsForValue().set(key, "true", accessTokenExpiration, TimeUnit.MILLISECONDS);
```

### 2. 강제 로그아웃

관리자가 특정 사용자를 강제 로그아웃시킬 수 있습니다:

```java
tokenBlacklistService.invalidateAllSessions(userId);
```

### 3. 계정 정지/탈퇴

계정 정지 또는 탈퇴 시 모든 토큰을 무효화:

```java
// 사용자 정지 시
user.setStatus(UserStatus.SUSPENDED);
userRepository.save(user);
tokenBlacklistService.invalidateAllSessions(userId);
```

### 4. Refresh Token Rotation

보안을 강화하려면 Refresh Token을 사용할 때마다 새로운 토큰을 발급:

```java
// refreshToken 메서드에서
// 기존 Refresh Token을 블랙리스트에 추가
tokenBlacklistService.addToBlacklist("", refreshToken);

// 새 토큰 생성 및 저장
String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);
tokenBlacklistService.saveRefreshToken(userId, newRefreshToken, 2592000000L);
```

## 성능 최적화

### 1. Redis Connection Pooling

Lettuce는 기본적으로 connection pooling을 지원합니다.

### 2. Pipeline

여러 Redis 명령을 한 번에 실행:

```java
redisTemplate.executePipelined(new RedisCallback<Object>() {
    @Override
    public Object doInRedis(RedisConnection connection) {
        connection.set(...);
        connection.set(...);
        return null;
    }
});
```

### 3. Redis Cluster

프로덕션 환경에서는 Redis Cluster를 사용하여 고가용성 확보.

## 모니터링

### 1. Redis 메모리 사용량 확인

```bash
redis-cli INFO memory
```

### 2. 블랙리스트 토큰 개수 확인

```bash
redis-cli DBSIZE
```

### 3. Slow Log 확인

```bash
redis-cli SLOWLOG GET 10
```

## 문제 해결

### 1. Redis 연결 실패

**증상:** 로그아웃 후에도 토큰이 여전히 유효

**원인:** Redis 연결 실패

**해결:**
```bash
# Redis 실행 확인
redis-cli ping
# 응답: PONG

# Docker Compose로 Redis 시작
docker-compose up -d redis
```

### 2. 토큰이 블랙리스트에 추가되지 않음

**증상:** 로그아웃 후 로그에 "Redis 저장 실패" 메시지

**원인:** Redis 설정 오류

**해결:**
```yaml
# application-dev.yml 확인
spring:
  data:
    redis:
      host: localhost  # Redis 호스트
      port: 6379       # Redis 포트
```

### 3. 메모리 부족

**증상:** Redis OOM (Out of Memory)

**원인:** TTL 설정 없이 토큰 저장

**해결:** TokenBlacklistService에서 TTL 설정 확인

## 다음 단계

Redis 토큰 블랙리스트 구현이 완료되었습니다. 다음 작업:

1. ✅ **Swagger/OpenAPI 문서화** - 완료
2. ✅ **Redis 토큰 블랙리스트** - 완료
3. ⏭️ **Integration Test** - 인증/인가 테스트
4. ⏭️ **OAuth2 소셜 로그인** - Google, Kakao
5. ⏭️ **Rate Limiting** - API 호출 빈도 제한

## 참고 자료

- [Spring Data Redis Documentation](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Redis Documentation](https://redis.io/documentation)
- [Lettuce Documentation](https://lettuce.io/)

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

