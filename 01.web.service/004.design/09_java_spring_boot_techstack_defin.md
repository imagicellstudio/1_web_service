# Java Spring Boot 기술 스택 정의서

**문서 버전**: v1.0  
**작성일**: 2025-11-20  
**프로젝트**: 식품 거래 플랫폼 (XLCfi)

---

## 1. 개요

플랫폼 구축 전환에 따라 안정적이고 확장 가능한 엔터프라이즈급 백엔드를 위해 Java Spring Boot 기반 아키텍처로 재설계합니다.

### 1.1 기술 선택 배경

- **거래/결제 안정성**: 대규모 트랜잭션 처리 검증됨
- **블록체인 통합**: Web3j 등 성숙한 라이브러리 지원
- **글로벌 확장성**: 다국어, 다통화, 높은 동시성 처리
- **보안/규정 준수**: Spring Security 기반 강력한 보안 체계
- **장기 유지보수**: 엔터프라이즈급 안정성 및 커뮤니티 지원

---

## 2. 핵심 기술 스택

### 2.1 Backend Framework

```yaml
Core Framework:
  - Java: 17 LTS (장기 지원)
  - Spring Boot: 3.2.x
  - Spring Framework: 6.1.x
  
Build Tool:
  - Gradle: 8.5+ (Kotlin DSL)
  - 이유: Maven 대비 빠른 빌드, 유연한 설정
```

### 2.2 Spring 생태계

```yaml
Spring Modules:
  - Spring Web: RESTful API 개발
  - Spring Data JPA: ORM 및 데이터 접근
  - Spring Security: 인증/권한/보안
  - Spring Cloud Gateway: API Gateway
  - Spring Cloud Config: 중앙 설정 관리
  - Spring Cloud OpenFeign: 마이크로서비스 통신
  - Spring Batch: 배치 처리 (정산, 통계)
  - Spring Cache: 캐싱 추상화
  - Spring Validation: 입력 검증
  - Spring AOP: 횡단 관심사 처리
```

### 2.3 데이터베이스

```yaml
Primary Database:
  - PostgreSQL: 15.x
  - 이유: ACID 보장, JSON 지원, 확장성, 오픈소스
  - 용도: 트랜잭션 데이터, 회원정보, 상품, 주문

Cache:
  - Redis: 7.2.x
  - 용도: 세션, 캐시, 실시간 데이터, Rate Limiting

Search Engine:
  - Elasticsearch: 8.x
  - 용도: 상품 검색, 로그 분석, 전문 검색

Document Store (선택적):
  - MongoDB: 7.x
  - 용도: 비정형 데이터, 로그, 평가 이미지 메타데이터
```

### 2.4 ORM 및 데이터 접근

```yaml
ORM:
  - Spring Data JPA: 3.2.x
  - Hibernate: 6.4.x (JPA 구현체)
  - QueryDSL: 5.1.x (타입 안전 쿼리)

Connection Pool:
  - HikariCP: 5.1.x (Spring Boot 기본)
  - 설정: max-pool-size=20, connection-timeout=30000

Migration:
  - Flyway: 10.x
  - 이유: Liquibase 대비 간결, Git 친화적
```

### 2.5 보안

```yaml
Authentication & Authorization:
  - Spring Security: 6.2.x
  - JWT: jjwt 0.12.x
  - OAuth2: Spring Security OAuth2 Client

Encryption:
  - BCrypt: 비밀번호 해싱
  - AES-256: 민감 데이터 암호화
  - TLS 1.3: 전송 계층 보안

API Security:
  - CORS: 출처 기반 접근 제어
  - CSRF: 토큰 기반 방어
  - Rate Limiting: Bucket4j + Redis
```

### 2.6 블록체인 통합

```yaml
Blockchain Library:
  - Web3j: 4.10.x
  - 이유: Java 생태계 표준, Ethereum 호환

Smart Contract:
  - Solidity: 0.8.x
  - 용도: 거래 이력, 원산지 추적, 리워드 토큰

Network:
  - Phase 1: Ethereum Testnet (Sepolia)
  - Phase 2: Polygon (낮은 가스비)
  - Phase 3: Private Blockchain (Hyperledger Besu)
```

### 2.7 결제 통합

```yaml
PG Integration:
  - 국내: 토스페이먼츠, 나이스페이
  - 해외: Stripe API (글로벌 확장)

Library:
  - Spring RestTemplate / WebClient
  - 결제 SDK: 각 PG사 Java SDK

Transaction Management:
  - Spring @Transactional
  - 2PC (Two-Phase Commit) for 분산 트랜잭션
```

### 2.8 API 문서화

```yaml
API Documentation:
  - SpringDoc OpenAPI: 2.3.x
  - Swagger UI: 자동 생성
  - 접근: /swagger-ui.html

API Specification:
  - OpenAPI 3.0
  - 자동 생성: Controller 어노테이션 기반
```

### 2.9 테스트

```yaml
Unit Testing:
  - JUnit 5: 5.10.x
  - Mockito: 5.x
  - AssertJ: 3.25.x

Integration Testing:
  - Spring Boot Test
  - Testcontainers: 1.19.x (DB, Redis 컨테이너)
  - REST Assured: API 테스트

Performance Testing:
  - JMeter: 부하 테스트
  - Gatling: 시나리오 기반 테스트
```

### 2.10 모니터링 및 로깅

```yaml
Monitoring:
  - Spring Boot Actuator: 헬스체크, 메트릭
  - Prometheus: 메트릭 수집
  - Grafana: 시각화 대시보드

Logging:
  - SLF4J + Logback: 로깅 프레임워크
  - ELK Stack:
    - Elasticsearch: 로그 저장
    - Logstash: 로그 수집/변환
    - Kibana: 로그 분석 UI

Tracing:
  - Spring Cloud Sleuth: 분산 추적
  - Zipkin: 트레이싱 시각화
```

### 2.11 메시징 (Phase 2+)

```yaml
Message Queue:
  - Apache Kafka: 3.6.x
  - 용도: 이벤트 스트리밍, 비동기 처리

Alternative:
  - RabbitMQ: 4.x
  - 용도: 작업 큐, 알림 발송
```

### 2.12 파일 저장소

```yaml
Object Storage:
  - AWS S3
  - 용도: 상품 이미지, 평가 사진, 문서

CDN:
  - AWS CloudFront
  - 용도: 정적 파일 배포, 이미지 캐싱

Image Processing:
  - Thumbnailator: 2.x
  - 용도: 썸네일 생성, 리사이징
```

---

## 3. 프로젝트 구조

### 3.1 멀티 모듈 구조

```
xlcfi-platform/
├── xlcfi-api-gateway/           # API Gateway (Spring Cloud Gateway)
├── xlcfi-auth-service/          # 인증/권한 서비스
├── xlcfi-member-service/        # 회원 관리 서비스
├── xlcfi-product-service/       # 상품 관리 서비스
├── xlcfi-order-service/         # 주문 관리 서비스
├── xlcfi-payment-service/       # 결제 서비스
├── xlcfi-blockchain-service/    # 블록체인 연동 서비스
├── xlcfi-review-service/        # 평가/리뷰 서비스
├── xlcfi-admin-service/         # 관리자 서비스
├── xlcfi-common/                # 공통 라이브러리
│   ├── common-core/             # 공통 유틸, 예외, 응답
│   ├── common-security/         # 보안 공통 모듈
│   └── common-data/             # 데이터 공통 모듈
├── xlcfi-batch/                 # 배치 작업 (정산, 통계)
└── build.gradle.kts             # 루트 빌드 설정
```

### 3.2 단일 서비스 내부 구조 (Layered Architecture)

```
xlcfi-product-service/
├── src/main/java/com/xlcfi/product/
│   ├── controller/              # REST API 엔드포인트
│   │   ├── ProductController.java
│   │   └── CategoryController.java
│   ├── service/                 # 비즈니스 로직
│   │   ├── ProductService.java
│   │   └── ProductServiceImpl.java
│   ├── repository/              # 데이터 접근 계층
│   │   ├── ProductRepository.java
│   │   └── ProductQueryRepository.java (QueryDSL)
│   ├── domain/                  # 도메인 엔티티
│   │   ├── Product.java
│   │   └── Category.java
│   ├── dto/                     # 데이터 전송 객체
│   │   ├── request/
│   │   │   └── ProductCreateRequest.java
│   │   └── response/
│   │       └── ProductResponse.java
│   ├── mapper/                  # Entity <-> DTO 변환
│   │   └── ProductMapper.java
│   ├── exception/               # 예외 처리
│   │   └── ProductNotFoundException.java
│   ├── config/                  # 설정 클래스
│   │   ├── JpaConfig.java
│   │   └── SecurityConfig.java
│   └── ProductServiceApplication.java
├── src/main/resources/
│   ├── application.yml          # 설정 파일
│   ├── application-dev.yml      # 개발 환경
│   ├── application-prod.yml     # 운영 환경
│   └── db/migration/            # Flyway 마이그레이션
│       ├── V1__init_product_schema.sql
│       └── V2__add_product_indexes.sql
└── src/test/java/               # 테스트 코드
    ├── controller/
    ├── service/
    └── repository/
```

---

## 4. 의존성 관리 (build.gradle.kts)

### 4.1 루트 build.gradle.kts

```kotlin
plugins {
    java
    id("org.springframework.boot") version "3.2.1" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.xlcfi"
    version = "1.0.0-SNAPSHOT"
    
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    
    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    dependencies {
        // Lombok
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        
        // Test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.mockito:mockito-core")
        testImplementation("org.assertj:assertj-core")
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
```

### 4.2 서비스별 의존성 예시 (product-service)

```kotlin
dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.zaxxer:HikariCP")
    
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    
    // Migration
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    
    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Elasticsearch
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    
    // AWS SDK
    implementation("software.amazon.awssdk:s3:2.21.0")
    
    // Image Processing
    implementation("net.coobird:thumbnailator:0.4.20")
    
    // Common Module
    implementation(project(":xlcfi-common:common-core"))
    implementation(project(":xlcfi-common:common-security"))
    
    // Test
    testImplementation("org.testcontainers:postgresql:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
}
```

---

## 5. 설정 파일 구조

### 5.1 application.yml (공통 설정)

```yaml
spring:
  application:
    name: xlcfi-product-service
  
  profiles:
    active: dev
  
  datasource:
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 100
  
  data:
    redis:
      timeout: 3000ms
  
  cache:
    type: redis
    redis:
      time-to-live: 3600000
  
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 20MB

server:
  port: 8081
  compression:
    enabled: true
  http2:
    enabled: true

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    root: INFO
    com.xlcfi: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 5.2 application-dev.yml (개발 환경)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/xlcfi_dev
    username: xlcfi_dev
    password: dev_password
  
  data:
    redis:
      host: localhost
      port: 6379
  
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 5.3 application-prod.yml (운영 환경)

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}
  
  jpa:
    show-sql: false

logging:
  level:
    root: WARN
    com.xlcfi: INFO
```

---

## 6. 개발 환경 설정

### 6.1 로컬 개발 환경 (Docker Compose)

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: xlcfi-postgres
    environment:
      POSTGRES_DB: xlcfi_dev
      POSTGRES_USER: xlcfi_dev
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
  
  redis:
    image: redis:7-alpine
    container_name: xlcfi-redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
  
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: xlcfi-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - es_data:/usr/share/elasticsearch/data

volumes:
  postgres_data:
  redis_data:
  es_data:
```

### 6.2 IDE 설정 (IntelliJ IDEA)

```
필수 플러그인:
- Lombok
- Spring Boot
- Database Tools
- Docker
- Gradle

권장 설정:
- Enable annotation processing (Lombok, QueryDSL)
- Code style: Google Java Style Guide
- File encoding: UTF-8
```

---

## 7. 코딩 컨벤션

### 7.1 네이밍 규칙

```java
// 클래스: PascalCase
public class ProductService {}

// 메서드/변수: camelCase
private String productName;
public void createProduct() {}

// 상수: UPPER_SNAKE_CASE
public static final int MAX_PRODUCT_COUNT = 100;

// 패키지: lowercase
package com.xlcfi.product.service;

// 테스트: ~Test suffix
public class ProductServiceTest {}
```

### 7.2 어노테이션 순서

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    
    @GetMapping("/{id}")
    @Operation(summary = "상품 조회")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ProductResponse> getProduct(
        @PathVariable Long id
    ) {
        // ...
    }
}
```

### 7.3 예외 처리

```java
// 커스텀 예외
public class ProductNotFoundException extends BusinessException {
    public ProductNotFoundException(Long productId) {
        super(ErrorCode.PRODUCT_NOT_FOUND, 
              String.format("상품을 찾을 수 없습니다. ID: %d", productId));
    }
}

// 글로벌 예외 핸들러
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
        ProductNotFoundException e
    ) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(e));
    }
}
```

---

## 8. 보안 설정

### 8.1 Spring Security 기본 설정

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter(), 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### 8.2 JWT 설정

```java
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity; // 1시간
    
    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity; // 14일
    
    public String generateAccessToken(Authentication authentication) {
        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim("authorities", getAuthorities(authentication))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
            .signWith(getSigningKey(), SignatureAlgorithm.HS512)
            .compact();
    }
}
```

---

## 9. 성능 최적화

### 9.1 JPA N+1 문제 해결

```java
// Fetch Join 사용
@Query("SELECT p FROM Product p " +
       "LEFT JOIN FETCH p.category " +
       "LEFT JOIN FETCH p.images " +
       "WHERE p.id = :id")
Optional<Product> findByIdWithDetails(@Param("id") Long id);

// EntityGraph 사용
@EntityGraph(attributePaths = {"category", "images"})
List<Product> findAll();

// Batch Size 설정 (application.yml)
spring.jpa.properties.hibernate.default_batch_fetch_size: 100
```

### 9.2 캐싱 전략

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProduct(Long id) {
        // DB 조회
    }
    
    @CacheEvict(value = "products", key = "#id")
    public void updateProduct(Long id, ProductUpdateRequest request) {
        // 업데이트 후 캐시 제거
    }
    
    @Caching(evict = {
        @CacheEvict(value = "products", allEntries = true),
        @CacheEvict(value = "productList", allEntries = true)
    })
    public void deleteProduct(Long id) {
        // 삭제 후 관련 캐시 모두 제거
    }
}
```

### 9.3 데이터베이스 인덱스

```sql
-- V3__add_performance_indexes.sql
CREATE INDEX idx_product_category_id ON products(category_id);
CREATE INDEX idx_product_status ON products(status);
CREATE INDEX idx_product_created_at ON products(created_at DESC);
CREATE INDEX idx_order_member_id ON orders(member_id);
CREATE INDEX idx_order_status_created ON orders(status, created_at DESC);

-- 복합 인덱스
CREATE INDEX idx_product_category_status ON products(category_id, status);

-- 전문 검색 인덱스
CREATE INDEX idx_product_name_gin ON products USING gin(to_tsvector('korean', name));
```

---

## 10. 배포 및 운영

### 10.1 Dockerfile

```dockerfile
# Multi-stage build
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 10.2 Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: xlcfi-product-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
      - name: product-service
        image: xlcfi/product-service:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## 11. 다음 단계

이 기술 스택 정의서를 기반으로:

1. **하이브리드 아키텍처 상세 설계** 진행
2. **각 서비스별 API 명세** 작성
3. **데이터베이스 스키마** Java 엔티티로 재설계
4. **단계별 구현 계획** 수정

---

**문서 작성**: AI Assistant  
**검토 필요**: 기술 리드, 아키텍트

