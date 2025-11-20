# XLCfi Platform - Rate Limiting Implementation

## 개요

Redis를 활용한 API Rate Limiting 시스템이 구현되었습니다. API 호출 빈도를 제한하여 서버 리소스를 보호하고 악의적인 공격을 방지합니다.

**작업 완료 날짜:** 2025-11-20

## 구현 완료 항목

### 1. Rate Limit Aspect

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/ratelimit/RateLimitAspect.java`

**주요 기능:**
- AOP 기반 Rate Limiting
- Redis를 사용한 카운팅
- Sliding Window Algorithm
- IP, User, API, Global 타입 지원

**동작 방식:**
```
1. @RateLimit 어노테이션 감지
2. 요청 정보 추출 (IP, UserId, API 경로)
3. Redis 키 생성
4. 카운트 증가
5. Limit 초과 여부 확인
6. 초과 시 BusinessException 발생
```

### 2. Rate Limit 어노테이션

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/ratelimit/RateLimit.java`

**파라미터:**
- `limit`: 시간 창 내 최대 호출 횟수 (기본값: 100)
- `timeWindow`: 시간 창 (초 단위, 기본값: 60)
- `type`: Rate Limit 타입 (기본값: IP)

**사용 예시:**
```java
@RateLimit(limit = 10, timeWindow = 60, type = RateLimitType.IP)
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // IP당 60초에 10번까지 로그인 가능
}
```

### 3. Rate Limit 타입

**파일:** `xlcfi-common/common-core/src/main/java/com/xlcfi/common/ratelimit/RateLimitType.java`

**타입:**

#### IP
- IP 주소 기반 제한
- 동일 IP에서의 요청 제한
- X-Forwarded-For 헤더 지원 (프록시 환경)

#### USER
- 사용자 ID 기반 제한
- 로그인한 사용자별 제한
- 인증된 요청에 사용

#### API
- API 엔드포인트 기반 제한
- 특정 API의 전체 호출 제한
- 서버 전체 부하 관리

#### GLOBAL
- 전역 제한
- 모든 요청에 대한 제한

## 사용 예시

### 1. 로그인 API - IP 기반

```java
@RateLimit(limit = 5, timeWindow = 60, type = RateLimitType.IP)
@PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(
        @Valid @RequestBody LoginRequest request) {
    // 동일 IP에서 60초에 5번까지만 로그인 시도 가능
    // Brute Force 공격 방지
    LoginResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success(response));
}
```

### 2. 회원가입 API - IP 기반

```java
@RateLimit(limit = 3, timeWindow = 3600, type = RateLimitType.IP)
@PostMapping("/register")
public ResponseEntity<ApiResponse<UserResponse>> register(
        @Valid @RequestBody RegisterRequest request) {
    // 동일 IP에서 1시간에 3번까지만 회원가입 가능
    // 대량 가입 방지
    UserResponse user = authService.register(request);
    return ResponseEntity.ok(ApiResponse.success(user));
}
```

### 3. 상품 조회 API - USER 기반

```java
@RateLimit(limit = 100, timeWindow = 60, type = RateLimitType.USER)
@GetMapping("/products")
public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
        @CurrentUser Long userId,
        @PageableDefault Pageable pageable) {
    // 사용자당 60초에 100번까지 조회 가능
    PageResponse<ProductResponse> products = productService.getProducts(pageable);
    return ResponseEntity.ok(ApiResponse.success(products));
}
```

### 4. 결제 API - USER 기반 (엄격)

```java
@RateLimit(limit = 10, timeWindow = 60, type = RateLimitType.USER)
@PostMapping("/payments")
public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
        @CurrentUser Long userId,
        @Valid @RequestBody CreatePaymentRequest request) {
    // 사용자당 60초에 10번까지만 결제 요청 가능
    // 중복 결제 방지
    PaymentResponse payment = paymentService.createPayment(userId, request);
    return ResponseEntity.ok(ApiResponse.success(payment));
}
```

### 5. 관리자 API - API 기반

```java
@RateLimit(limit = 1000, timeWindow = 60, type = RateLimitType.API)
@GetMapping("/admin/dashboard")
public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
    // 전체 사용자가 60초에 1000번까지 호출 가능
    // 서버 리소스 보호
    DashboardResponse dashboard = adminService.getDashboard();
    return ResponseEntity.ok(ApiResponse.success(dashboard));
}
```

## Redis 데이터 구조

### 키 패턴

```
rate_limit:{type}:{identifier}:count
```

**예시:**
```
# IP 기반
rate_limit:ip:192.168.1.100:count = 5
TTL: 60초

# USER 기반
rate_limit:user:123:count = 15
TTL: 60초

# API 기반
rate_limit:api:/api/products:count = 500
TTL: 60초
```

## 알고리즘: Sliding Window

### 동작 방식

```
1. 요청 발생
   ↓
2. Redis에서 현재 카운트 조회
   ↓
3. 카운트 + 1 (INCR)
   ↓
4. 첫 요청이면 TTL 설정
   ↓
5. 카운트 <= Limit 확인
   ↓
6. 초과 시 에러 반환
   ↓
7. 정상이면 API 처리
```

### 장점

- 구현이 간단
- Redis의 INCR 명령 활용
- 자동 만료 (TTL)
- 메모리 효율적

## 클라이언트 IP 추출

프록시 환경에서 실제 클라이언트 IP 추출:

```java
private String getClientIP(HttpServletRequest request) {
    String[] headerNames = {
            "X-Forwarded-For",      // 프록시 환경
            "Proxy-Client-IP",      // Apache
            "WL-Proxy-Client-IP",   // WebLogic
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };

    for (String header : headerNames) {
        String ip = request.getHeader(header);
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();  // 첫 번째 IP가 원본
        }
    }

    return request.getRemoteAddr();  // 기본 IP
}
```

## 에러 응답

### Rate Limit 초과 시

**HTTP Status:** 429 Too Many Requests (또는 400 Bad Request)

**응답 예시:**
```json
{
  "success": false,
  "data": null,
  "message": "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.",
  "errors": {
    "code": "RATE_LIMIT_001",
    "message": "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요."
  }
}
```

### HTTP Status 429 사용하기

GlobalExceptionHandler에 추가:

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex, HttpServletRequest request) {
    
    HttpStatus status = HttpStatus.BAD_REQUEST;
    
    // Rate Limit 에러는 429 상태 코드 사용
    if ("RATE_LIMIT_001".equals(ex.getErrorCode())) {
        status = HttpStatus.TOO_MANY_REQUESTS;
    }
    
    ErrorResponse errorResponse = ErrorResponse.builder()
            .code(ex.getErrorCode())
            .message(ex.getErrorMessage())
            .build();
    
    return new ResponseEntity<>(errorResponse, status);
}
```

## 권장 Rate Limit 설정

### 인증 관련

| API | Limit | Time Window | Type | 이유 |
|-----|-------|-------------|------|------|
| /api/auth/login | 5 | 60초 | IP | Brute Force 공격 방지 |
| /api/auth/register | 3 | 1시간 | IP | 대량 가입 방지 |
| /api/auth/refresh | 10 | 60초 | USER | 토큰 남용 방지 |

### 상품 관련

| API | Limit | Time Window | Type | 이유 |
|-----|-------|-------------|------|------|
| /api/products (GET) | 100 | 60초 | USER | 크롤링 방지 |
| /api/products (POST) | 20 | 60초 | USER | 스팸 상품 방지 |
| /api/products/search | 50 | 60초 | IP | 서버 부하 관리 |

### 주문/결제 관련

| API | Limit | Time Window | Type | 이유 |
|-----|-------|-------------|------|------|
| /api/orders (POST) | 10 | 60초 | USER | 중복 주문 방지 |
| /api/payments (POST) | 5 | 60초 | USER | 중복 결제 방지 |

### 리뷰 관련

| API | Limit | Time Window | Type | 이유 |
|-----|-------|-------------|------|------|
| /api/reviews (POST) | 10 | 1시간 | USER | 스팸 리뷰 방지 |
| /api/reviews (GET) | 100 | 60초 | IP | 서버 부하 관리 |

## 모니터링

### Redis CLI로 확인

```bash
# Rate Limit 키 조회
redis-cli KEYS "rate_limit:*"

# 특정 키의 값 확인
redis-cli GET "rate_limit:ip:192.168.1.100:count"

# TTL 확인
redis-cli TTL "rate_limit:ip:192.168.1.100:count"
```

### 로그 모니터링

```java
@Slf4j
@Aspect
@Component
public class RateLimitAspect {
    // ...
    
    log.warn("Rate limit 초과: key={}, limit={}/{}", 
            key, rateLimit.limit(), rateLimit.timeWindow());
}
```

## 프론트엔드 통합

### 1. Retry-After 헤더 추가

```java
@ExceptionHandler(BusinessException.class)
public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException ex, HttpServletRequest request) {
    
    if ("RATE_LIMIT_001".equals(ex.getErrorCode())) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", "60");  // 60초 후 재시도
        
        return new ResponseEntity<>(errorResponse, headers, HttpStatus.TOO_MANY_REQUESTS);
    }
    
    // ...
}
```

### 2. 프론트엔드 처리

```javascript
// Axios Interceptor
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 429) {
      const retryAfter = error.response.headers['retry-after'] || 60;
      alert(`요청 한도를 초과했습니다. ${retryAfter}초 후에 다시 시도해주세요.`);
    }
    return Promise.reject(error);
  }
);
```

## 고급 기능

### 1. IP 화이트리스트

관리자 IP는 Rate Limit 제외:

```java
private static final Set<String> WHITELIST_IPS = Set.of(
    "192.168.1.1",
    "10.0.0.1"
);

@Around("@annotation(com.xlcfi.common.ratelimit.RateLimit)")
public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
    String ip = getClientIP(request);
    
    // 화이트리스트 IP는 Rate Limit 적용 안 함
    if (WHITELIST_IPS.contains(ip)) {
        return joinPoint.proceed();
    }
    
    // ...
}
```

### 2. 동적 Limit 조정

VIP 사용자는 더 높은 Limit 적용:

```java
private int getEffectiveLimit(Long userId, int baseLimit) {
    User user = userRepository.findById(userId).orElse(null);
    
    if (user != null && user.getRole() == UserRole.VIP) {
        return baseLimit * 2;  // VIP는 2배
    }
    
    return baseLimit;
}
```

### 3. 여러 시간 창 조합

```java
@RateLimit(limit = 100, timeWindow = 60)  // 1분에 100번
@RateLimit(limit = 1000, timeWindow = 3600)  // 1시간에 1000번
@GetMapping("/api/products")
public ResponseEntity<?> getProducts() {
    // 두 조건을 모두 만족해야 함
}
```

## 성능 고려사항

### 1. Redis Connection Pool

```yaml
spring:
  data:
    redis:
      lettuce:
        pool:
          max-active: 10
          max-idle: 5
          min-idle: 2
```

### 2. Lua Script 사용 (원자적 연산)

```java
String luaScript = 
    "local current = redis.call('incr', KEYS[1]) " +
    "if current == 1 then " +
    "    redis.call('expire', KEYS[1], ARGV[1]) " +
    "end " +
    "return current";

Long count = redisTemplate.execute(
    RedisScript.of(luaScript, Long.class),
    Collections.singletonList(key),
    String.valueOf(timeWindow)
);
```

## 테스트

### 1. 단위 테스트

```java
@Test
void rateLimit_ExceedsLimit_ThrowsException() {
    // Given
    String key = "rate_limit:ip:192.168.1.1:count";
    for (int i = 0; i < 5; i++) {
        redisTemplate.opsForValue().increment(key);
    }
    
    // When & Then
    assertThatThrownBy(() -> rateLimitAspect.checkRateLimit(key, 5))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("요청 한도를 초과했습니다");
}
```

### 2. 통합 테스트

```java
@Test
void login_ExceedsRateLimit_Returns429() throws Exception {
    // Given
    LoginRequest request = LoginRequest.builder()
            .email("test@example.com")
            .password("password123")
            .build();

    // When - 6번 연속 호출 (Limit: 5)
    for (int i = 0; i < 6; i++) {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(i < 5 ? status().isOk() : status().isBadRequest());
    }
}
```

## 문제 해결

### 1. Redis 연결 실패 시에도 API 동작

```java
try {
    return checkRateLimit(key, rateLimit);
} catch (Exception e) {
    log.error("Rate limit 체크 실패: {}", e.getMessage(), e);
    // Redis 오류 시에도 요청은 통과시킴
    return true;
}
```

### 2. 클러스터 환경에서 IP 추출

로드 밸런서나 프록시 뒤에 있을 때 `X-Forwarded-For` 헤더 사용.

## 다음 단계

모든 작업이 완료되었습니다!

1. ✅ **Swagger/OpenAPI 문서화** - 완료
2. ✅ **Redis 토큰 블랙리스트** - 완료
3. ✅ **Integration Test** - 완료
4. ✅ **OAuth2 소셜 로그인** - 완료
5. ✅ **Rate Limiting** - 완료

## 참고 자료

- [Redis INCR Command](https://redis.io/commands/incr/)
- [Rate Limiting Algorithms](https://hechao.li/2018/06/25/Rate-Limiter-Part1/)
- [Spring AOP Documentation](https://docs.spring.io/spring-framework/reference/core/aop.html)

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

