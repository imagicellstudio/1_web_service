# XLCfi Platform - Integration Test Implementation

## 개요

Spring Boot의 MockMvc와 Mockito를 활용한 통합 테스트 및 단위 테스트가 구현되었습니다.

**작업 완료 날짜:** 2025-11-20

## 구현 완료 항목

### 1. Integration Test (통합 테스트)

**파일:** `xlcfi-auth-service/src/test/java/com/xlcfi/auth/controller/AuthControllerIntegrationTest.java`

**테스트 시나리오:**

#### 회원가입
- ✅ 회원가입 성공
- ✅ 회원가입 실패 - 이메일 중복

#### 로그인
- ✅ 로그인 성공
- ✅ 로그인 실패 - 잘못된 비밀번호

#### 프로필
- ✅ 프로필 조회 성공
- ✅ 프로필 조회 실패 - 토큰 없음
- ✅ 프로필 수정 성공

#### 로그아웃
- ✅ 로그아웃 성공
- ✅ 로그아웃 후 토큰 무효화 확인

**주요 특징:**
- `@SpringBootTest`: 전체 Spring Context 로드
- `@AutoConfigureMockMvc`: MockMvc 자동 설정
- `@ActiveProfiles("test")`: 테스트 프로파일 활성화
- 실제 데이터베이스(H2) 사용
- JWT 토큰 발급 및 검증
- Redis 블랙리스트 동작 확인

### 2. Unit Test (단위 테스트)

**파일:** `xlcfi-auth-service/src/test/java/com/xlcfi/auth/service/AuthServiceTest.java`

**테스트 시나리오:**

#### 회원가입
- ✅ 회원가입 성공
- ✅ 회원가입 실패 - 이메일 중복

#### 로그인
- ✅ 로그인 성공
- ✅ 로그인 실패 - 잘못된 비밀번호
- ✅ 로그인 실패 - 정지된 계정

#### 프로필
- ✅ 프로필 조회 성공

#### 로그아웃
- ✅ 로그아웃 성공

**주요 특징:**
- `@ExtendWith(MockitoExtension.class)`: Mockito 활용
- `@Mock`: 의존성 Mocking
- `@InjectMocks`: 테스트 대상 주입
- 빠른 실행 속도
- 외부 의존성 없이 독립적인 테스트

### 3. 테스트 설정

**파일:** `xlcfi-auth-service/src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  
  jpa:
    hibernate:
      ddl-auto: create-drop
  
  flyway:
    enabled: false  # 테스트에서는 Flyway 비활성화

jwt:
  secret: test-secret-key...
```

**H2 Database:**
- 인메모리 데이터베이스
- 테스트마다 초기화
- 빠른 테스트 실행

**Redis:**
- Embedded Redis 사용
- 로컬 Redis 의존성 제거

### 4. 빌드 설정

**파일:** `xlcfi-auth-service/build.gradle.kts`

```kotlin
dependencies {
    // Test
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testImplementation("it.ozimov:embedded-redis:0.7.3")
}
```

## 테스트 실행 방법

### 1. 전체 테스트 실행

```bash
# Gradle로 실행
./gradlew test

# Windows
.\gradlew.bat test
```

### 2. 특정 서비스 테스트

```bash
./gradlew :xlcfi-auth-service:test
```

### 3. 특정 테스트 클래스 실행

```bash
./gradlew :xlcfi-auth-service:test --tests AuthControllerIntegrationTest
```

### 4. 특정 테스트 메서드 실행

```bash
./gradlew :xlcfi-auth-service:test --tests AuthServiceTest.login_Success
```

### 5. 테스트 리포트 생성

```bash
./gradlew test
# 리포트 위치: build/reports/tests/test/index.html
```

## 테스트 커버리지

### JaCoCo 설정 (선택사항)

**build.gradle.kts에 추가:**

```kotlin
plugins {
    jacoco
}

jacoco {
    toolVersion = "0.8.10"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()  // 80% 커버리지 요구
            }
        }
    }
}
```

**실행:**
```bash
./gradlew test jacocoTestReport
# 리포트 위치: build/reports/jacoco/test/html/index.html
```

## 테스트 예시

### Integration Test 예시

```java
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
            .andReturn();

    // AccessToken 추출
    String response = result.getResponse().getContentAsString();
    String accessToken = objectMapper.readTree(response)
            .get("data")
            .get("accessToken")
            .asText();

    assertThat(accessToken).isNotNull();
}
```

### Unit Test 예시

```java
@Test
@DisplayName("회원가입 성공")
void register_Success() {
    // Given
    RegisterRequest request = RegisterRequest.builder()
            .email("newuser@example.com")
            .password("password123")
            .name("신규유저")
            .build();

    given(userRepository.existsByEmail(anyString())).willReturn(false);
    given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
    given(userRepository.save(any(User.class))).willReturn(testUser);

    // When
    UserResponse response = authService.register(request);

    // Then
    assertThat(response).isNotNull();
    assertThat(response.getEmail()).isEqualTo(testUser.getEmail());
    verify(userRepository, times(1)).save(any(User.class));
}
```

## CI/CD 통합

### GitHub Actions 예시

**`.github/workflows/test.yml`:**

```yaml
name: Java CI with Gradle

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_USER: xlcfi_test
          POSTGRES_PASSWORD: test_password
          POSTGRES_DB: xlcfi_test_db
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Test with Gradle
        run: ./gradlew test

      - name: Publish Test Report
        uses: mikepenz/action-junit-report@v3
        if: always()
        with:
          report_paths: '**/build/test-results/test/TEST-*.xml'

      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-results
          path: '**/build/reports/tests/test/'
```

## 테스트 Best Practices

### 1. 테스트 명명 규칙

```java
@Test
@DisplayName("메서드명_테스트조건_예상결과")
void login_WithValidCredentials_ReturnsToken() {
    // ...
}
```

### 2. Given-When-Then 패턴

```java
@Test
void test() {
    // Given: 테스트 준비
    LoginRequest request = LoginRequest.builder()...

    // When: 테스트 실행
    LoginResponse response = authService.login(request);

    // Then: 결과 검증
    assertThat(response).isNotNull();
}
```

### 3. 테스트 격리

```java
@BeforeEach
void setUp() {
    // 각 테스트 전에 실행
}

@AfterEach
void tearDown() {
    // 각 테스트 후에 실행 (데이터 정리)
    userRepository.deleteAll();
}
```

### 4. 의미있는 Assertion

```java
// 나쁜 예
assertThat(response).isNotNull();

// 좋은 예
assertThat(response.getEmail())
    .isNotNull()
    .isEqualTo("expected@example.com");
```

### 5. Mock 사용 최소화

- Integration Test에서는 실제 Bean 사용
- Unit Test에서만 Mock 사용
- 필요한 부분만 Mocking

## 테스트 데이터 관리

### 1. Test Fixture

```java
public class UserFixture {
    public static User createTestUser() {
        return User.builder()
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .name("테스트유저")
                .build();
    }

    public static RegisterRequest createRegisterRequest() {
        return RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .build();
    }
}
```

### 2. @Sql 어노테이션

```java
@Test
@Sql("/test-data.sql")  // 테스트 전에 SQL 실행
void testWithPreloadedData() {
    // ...
}
```

## 성능 테스트

### 1. JMeter

```xml
<!-- JMeter Test Plan 예시 -->
<ThreadGroup>
  <stringProp name="ThreadGroup.num_threads">100</stringProp>
  <stringProp name="ThreadGroup.ramp_time">10</stringProp>
  <HTTPSamplerProxy>
    <stringProp name="HTTPSampler.path">/api/auth/login</stringProp>
    <stringProp name="HTTPSampler.method">POST</stringProp>
  </HTTPSamplerProxy>
</ThreadGroup>
```

### 2. Gatling

```scala
class AuthSimulation extends Simulation {
  val httpProtocol = http
    .baseUrl("http://localhost:8081")

  val scn = scenario("Login")
    .exec(http("login")
      .post("/api/auth/login")
      .body(StringBody("""{"email":"test@example.com","password":"password123"}"""))
      .check(status.is(200)))

  setUp(scn.inject(atOnceUsers(100)))
    .protocols(httpProtocol)
}
```

## 문제 해결

### 1. H2 Database 오류

**증상:** `Table not found` 오류

**해결:**
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # 테스트마다 스키마 재생성
```

### 2. Redis 연결 오류

**증상:** `Connection refused` 오류

**해결:** Embedded Redis 사용
```kotlin
testImplementation("it.ozimov:embedded-redis:0.7.3")
```

### 3. 테스트 간 데이터 간섭

**증상:** 테스트 순서에 따라 결과가 달라짐

**해결:**
```java
@AfterEach
void tearDown() {
    userRepository.deleteAll();  // 각 테스트 후 데이터 정리
}
```

## 다음 단계

Integration Test 구현이 완료되었습니다. 다음 작업:

1. ✅ **Swagger/OpenAPI 문서화** - 완료
2. ✅ **Redis 토큰 블랙리스트** - 완료
3. ✅ **Integration Test** - 완료
4. ⏭️ **OAuth2 소셜 로그인** - Google, Kakao
5. ⏭️ **Rate Limiting** - API 호출 빈도 제한

## 참고 자료

- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

