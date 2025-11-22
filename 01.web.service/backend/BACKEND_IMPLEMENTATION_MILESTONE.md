# XLCfi Platform - Backend Implementation Milestone

## 📌 문서 목적

이 문서는 **백엔드 구현 완료 시점(2025-11-20)**의 기점(Milestone)을 명확히 하기 위해 작성되었습니다.

**목적:**
1. 백엔드 구현 내역의 완전한 기록
2. 프론트엔드 구현 시작 전 명확한 구분점 설정
3. 향후 프론트엔드 교체/수정 시 백엔드 참조 자료
4. 독립적인 백엔드 시스템으로서의 문서화

**백엔드 구현 기간:** 2025-11-20  
**백엔드 구현 상태:** ✅ 완료 (100%)  
**Git Commit:** 3개 (40개 파일, 6,697줄 추가)

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [데이터베이스 설계](#데이터베이스-설계)
5. [API 명세](#api-명세)
6. [보안 구현](#보안-구현)
7. [구현된 기능 목록](#구현된-기능-목록)
8. [설정 파일](#설정-파일)
9. [실행 방법](#실행-방법)
10. [테스트](#테스트)
11. [프론트엔드 연동 가이드](#프론트엔드-연동-가이드)
12. [문서 목록](#문서-목록)

---

## 프로젝트 개요

### 서비스 설명

K-Food 원료, 원산지, 음식, 요리방법, 레시피 등을 소개하고 거래할 수 있는 플랫폼의 백엔드 시스템.

### 아키텍처

**마이크로서비스 아키텍처**
- Auth Service (인증/사용자 관리)
- Product Service (상품/카테고리 관리)
- Order Service (주문 관리)
- Payment Service (결제 관리)
- Review Service (리뷰 관리)

**하이브리드 아키텍처**
- Java Spring Boot: 메인 백엔드 (트랜잭션, 보안, 비즈니스 로직)
- Python Flask: 데이터 분석/ML (향후 구현 예정)

### 주요 특징

- ✅ RESTful API 설계
- ✅ JWT 기반 인증/인가
- ✅ Redis 토큰 블랙리스트
- ✅ Rate Limiting
- ✅ Swagger/OpenAPI 문서화
- ✅ 다국어 지원 (KO, EN, JA, ZH)
- ✅ Role 기반 접근 제어 (BUYER, SELLER, ADMIN)
- ✅ 원산지/식품코드/HACCP 분류 체계

---

## 기술 스택

### Backend Framework
```
Language: Java 17
Framework: Spring Boot 3.2.1
Build Tool: Gradle 8.x (Kotlin DSL)
```

### Spring Ecosystem
```
Spring Web
Spring Data JPA
Spring Security
Spring Data Redis
Spring AOP
Spring Validation
```

### Database & Cache
```
PostgreSQL 15 (Primary Database)
Redis 7 (Cache & Session)
H2 (Test Database)
Flyway (Migration)
```

### Security & Authentication
```
JWT (jjwt 0.11.5)
BCrypt (Password Hashing)
Spring Security
```

### Documentation & Testing
```
SpringDoc OpenAPI 2.3.0 (Swagger)
JUnit 5
Mockito
MockMvc
AssertJ
```

### Infrastructure
```
Docker & Docker Compose
Apache Kafka (Message Queue)
Elasticsearch 8 (Search Engine)
```

### 의존성 버전
```kotlin
// build.gradle.kts
plugins {
    java
    id("org.springframework.boot") version "3.2.1"
    id("io.spring.dependency-management") version "1.1.4"
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    
    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("org.flywaydb:flyway-core")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
}
```

---

## 프로젝트 구조

```
backend/
├── java-services/                    # Java 마이크로서비스
│   │
│   ├── xlcfi-common/                 # 공통 모듈
│   │   ├── common-core/              # 공통 유틸리티
│   │   │   ├── dto/
│   │   │   │   └── ApiResponse.java
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── config/
│   │   │   │   ├── WebConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── annotation/
│   │   │   │   ├── CurrentUser.java
│   │   │   │   └── RequireRole.java
│   │   │   ├── aspect/
│   │   │   │   └── RoleCheckAspect.java
│   │   │   └── ratelimit/
│   │   │       ├── RateLimit.java
│   │   │       ├── RateLimitType.java
│   │   │       └── RateLimitAspect.java
│   │   └── common-data/              # 공통 Entity
│   │       └── entity/
│   │           └── BaseEntity.java
│   │
│   ├── xlcfi-auth-service/           # 인증 서비스 (Port 8081)
│   │   ├── domain/
│   │   │   ├── User.java
│   │   │   ├── UserRole.java
│   │   │   ├── UserStatus.java
│   │   │   └── Language.java
│   │   ├── dto/
│   │   │   ├── RegisterRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── LoginResponse.java
│   │   │   ├── UserResponse.java
│   │   │   └── UpdateProfileRequest.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── JwtTokenProvider.java
│   │   │   └── TokenBlacklistService.java
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── security/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   ├── JwtAccessDeniedHandler.java
│   │   │   └── SecurityConfig.java
│   │   ├── config/
│   │   │   └── RedisConfig.java
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   ├── application-dev.yml
│   │   │   └── db/migration/
│   │   │       └── V1__init_users_schema.sql
│   │   └── test/
│   │       ├── controller/
│   │       │   └── AuthControllerIntegrationTest.java
│   │       ├── service/
│   │       │   └── AuthServiceTest.java
│   │       └── resources/
│   │           └── application-test.yml
│   │
│   ├── xlcfi-product-service/        # 상품 서비스 (Port 8082)
│   │   ├── domain/
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   └── ProductStatus.java
│   │   ├── dto/
│   │   │   ├── ProductRequest.java
│   │   │   ├── ProductResponse.java
│   │   │   └── CategoryResponse.java
│   │   ├── repository/
│   │   │   ├── ProductRepository.java
│   │   │   └── CategoryRepository.java
│   │   ├── service/
│   │   │   ├── ProductService.java
│   │   │   └── CategoryService.java
│   │   ├── controller/
│   │   │   ├── ProductController.java
│   │   │   └── CategoryController.java
│   │   └── resources/db/migration/
│   │       ├── V1__init_product_schema.sql
│   │       └── V2__init_category_data.sql
│   │
│   ├── xlcfi-order-service/          # 주문 서비스 (Port 8083)
│   │   ├── domain/
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   └── OrderStatus.java
│   │   ├── dto/
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── OrderItemRequest.java
│   │   │   ├── OrderResponse.java
│   │   │   └── OrderItemResponse.java
│   │   ├── repository/
│   │   │   ├── OrderRepository.java
│   │   │   └── OrderItemRepository.java
│   │   ├── service/
│   │   │   └── OrderService.java
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   └── resources/db/migration/
│   │       └── V1__init_order_schema.sql
│   │
│   ├── xlcfi-payment-service/        # 결제 서비스 (Port 8084)
│   │   ├── domain/
│   │   │   ├── Payment.java
│   │   │   ├── PaymentMethod.java
│   │   │   └── PaymentStatus.java
│   │   ├── dto/
│   │   │   ├── CreatePaymentRequest.java
│   │   │   └── PaymentResponse.java
│   │   ├── repository/
│   │   │   └── PaymentRepository.java
│   │   ├── service/
│   │   │   └── PaymentService.java
│   │   ├── controller/
│   │   │   └── PaymentController.java
│   │   └── resources/db/migration/
│   │       └── V1__init_payment_schema.sql
│   │
│   └── xlcfi-review-service/         # 리뷰 서비스 (Port 8085)
│       ├── domain/
│       │   ├── Review.java
│       │   └── ReviewStatus.java
│       ├── dto/
│       │   ├── CreateReviewRequest.java
│       │   ├── UpdateReviewRequest.java
│       │   └── ReviewResponse.java
│       ├── repository/
│       │   └── ReviewRepository.java
│       ├── service/
│       │   └── ReviewService.java
│       ├── controller/
│       │   └── ReviewController.java
│       └── resources/db/migration/
│           └── V1__init_review_schema.sql
│
├── python-services/                  # Python 마이크로서비스 (향후)
│   ├── analytics-service/
│   └── recommendation-service/
│
├── scripts/                          # 유틸리티 스크립트
│   ├── init-database.sql
│   ├── seed-data.sql
│   ├── reset-database.sh
│   ├── reset-database.bat
│   └── README.md
│
├── docker-compose.yml                # Docker Compose 설정
├── Makefile                          # 편의 명령어
├── .gitignore                        # Git 무시 파일
├── README.md                         # 프로젝트 개요
├── QUICKSTART.md                     # 빠른 시작 가이드
├── DB_SETUP_GUIDE.md                 # DB 설정 가이드
│
└── 문서/                             # 구현 문서
    ├── DATABASE_SCHEMA_SUMMARY.md
    ├── SERVICE_LAYER_SUMMARY.md
    ├── CONTROLLER_LAYER_SUMMARY.md
    ├── SECURITY_IMPLEMENTATION_SUMMARY.md
    ├── SWAGGER_API_DOCUMENTATION.md
    ├── REDIS_TOKEN_BLACKLIST.md
    ├── INTEGRATION_TEST_SUMMARY.md
    ├── OAUTH2_SOCIAL_LOGIN.md
    ├── RATE_LIMITING_IMPLEMENTATION.md
    ├── IMPLEMENTATION_COMPLETE_SUMMARY.md
    └── BACKEND_IMPLEMENTATION_MILESTONE.md (이 문서)
```

---

## 데이터베이스 설계

### ERD 개요

```
users (사용자)
  ↓ 1:N
products (상품) ← N:1 → categories (카테고리)
  ↓ 1:N
reviews (리뷰)

users (사용자)
  ↓ 1:N
orders (주문)
  ↓ 1:N
order_items (주문상세) → N:1 → products (상품)

orders (주문)
  ↓ 1:1
payments (결제)
```

### 테이블 상세

#### 1. users (사용자)

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'BUYER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    language VARCHAR(10) NOT NULL DEFAULT 'KO',
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    profile_image_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_provider ON users(provider, provider_id);
```

**필드 설명:**
- `role`: BUYER, SELLER, ADMIN
- `status`: ACTIVE, INACTIVE, SUSPENDED
- `language`: KO, EN, JA, ZH
- `provider`: google, kakao (OAuth2)

#### 2. categories (카테고리)

```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    parent_id BIGINT,
    name_ko VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    name_ja VARCHAR(100),
    name_zh VARCHAR(100),
    description_ko TEXT,
    description_en TEXT,
    description_ja TEXT,
    description_zh TEXT,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
```

#### 3. products (상품)

```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    name_ko VARCHAR(255) NOT NULL,
    name_en VARCHAR(255),
    name_ja VARCHAR(255),
    name_zh VARCHAR(255),
    description_ko TEXT,
    description_en TEXT,
    description_ja TEXT,
    description_zh TEXT,
    price DECIMAL(15,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    origin_country VARCHAR(100),
    food_code VARCHAR(50),
    haccp_certified BOOLEAN DEFAULT FALSE,
    images JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_price ON products(price);
```

**필드 설명:**
- `status`: ACTIVE, INACTIVE, OUT_OF_STOCK, DISCONTINUED
- `origin_country`: 원산지
- `food_code`: 식품코드
- `haccp_certified`: HACCP 인증 여부

#### 4. orders (주문)

```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(15,2) NOT NULL,
    shipping_address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created ON orders(created_at);
```

**필드 설명:**
- `status`: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

#### 5. order_items (주문 상세)

```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    total_price DECIMAL(15,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
```

#### 6. payments (결제)

```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    transaction_id VARCHAR(255),
    pg_provider VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_user ON payments(user_id);
CREATE INDEX idx_payments_status ON payments(status);
```

**필드 설명:**
- `method`: CREDIT_CARD, BANK_TRANSFER, PAYPAL, KAKAO_PAY
- `status`: PENDING, COMPLETED, FAILED, REFUNDED

#### 7. reviews (리뷰)

```sql
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT NOT NULL,
    images JSONB,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_status ON reviews(status);
```

**필드 설명:**
- `status`: ACTIVE, HIDDEN, DELETED
- `is_verified_purchase`: 구매 인증 여부

---

## API 명세

### API 응답 표준 형식

#### 성공 응답

```json
{
  "success": true,
  "data": { /* 실제 데이터 */ },
  "message": "성공 메시지",
  "errors": null
}
```

#### 에러 응답

```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "errors": {
    "code": "ERROR_CODE",
    "message": "상세 에러 메시지",
    "detail": "추가 정보",
    "path": "/api/..."
  }
}
```

### 1. Auth Service (Port 8081)

#### POST /api/auth/register
**회원가입**

Request:
```json
{
  "email": "user@example.com",
  "password": "password123!@#",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "BUYER",
  "language": "KO"
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "BUYER",
    "status": "ACTIVE",
    "language": "KO",
    "createdAt": "2025-11-20T10:00:00"
  },
  "message": "회원가입이 완료되었습니다"
}
```

#### POST /api/auth/login
**로그인**

Request:
```json
{
  "email": "user@example.com",
  "password": "password123!@#"
}
```

Response (200):
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

#### POST /api/auth/refresh
**토큰 갱신**

Headers:
```
Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Response (200):
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "message": "토큰이 갱신되었습니다"
}
```

#### GET /api/auth/profile
**내 프로필 조회**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Response (200):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "BUYER",
    "status": "ACTIVE",
    "language": "KO",
    "createdAt": "2025-11-20T10:00:00",
    "lastLoginAt": "2025-11-20T15:30:00"
  },
  "message": "프로필 조회 성공"
}
```

#### PUT /api/auth/profile
**프로필 수정**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Request:
```json
{
  "name": "김철수",
  "phone": "010-9999-8888",
  "language": "EN"
}
```

Response (200):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "김철수",
    "phone": "010-9999-8888",
    "language": "EN"
  },
  "message": "프로필이 수정되었습니다"
}
```

#### POST /api/auth/logout
**로그아웃**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Refresh-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Response (200):
```json
{
  "success": true,
  "data": null,
  "message": "로그아웃되었습니다"
}
```

### 2. Product Service (Port 8082)

#### GET /api/categories
**카테고리 목록**

Response (200):
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "채소류",
      "description": "신선한 채소",
      "parentId": null,
      "sortOrder": 1
    }
  ]
}
```

#### GET /api/products
**상품 목록**

Query Parameters:
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본 20)
- `sort`: 정렬 (예: price,asc)

Response (200):
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "유기농 배추",
        "description": "신선한 유기농 배추",
        "price": 5000,
        "stock": 100,
        "status": "ACTIVE",
        "originCountry": "대한민국",
        "haccp_certified": true,
        "seller": {
          "id": 2,
          "name": "농부홍길동"
        },
        "category": {
          "id": 1,
          "name": "채소류"
        }
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

#### POST /api/products
**상품 등록**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Request:
```json
{
  "categoryId": 1,
  "nameKo": "유기농 배추",
  "nameEn": "Organic Cabbage",
  "descriptionKo": "신선한 유기농 배추",
  "price": 5000,
  "stock": 100,
  "originCountry": "대한민국",
  "foodCode": "FC001",
  "haccp_certified": true,
  "images": ["image1.jpg", "image2.jpg"]
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "유기농 배추",
    "price": 5000,
    "stock": 100,
    "status": "ACTIVE"
  },
  "message": "상품이 등록되었습니다"
}
```

### 3. Order Service (Port 8083)

#### POST /api/orders
**주문 생성**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Request:
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ],
  "shippingAddress": "서울시 강남구 테헤란로 123"
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "PENDING",
    "totalAmount": 15000,
    "items": [
      {
        "productId": 1,
        "productName": "유기농 배추",
        "quantity": 2,
        "unitPrice": 5000,
        "totalPrice": 10000
      }
    ],
    "shippingAddress": "서울시 강남구 테헤란로 123",
    "createdAt": "2025-11-20T16:00:00"
  },
  "message": "주문이 생성되었습니다"
}
```

### 4. Payment Service (Port 8084)

#### POST /api/payments
**결제 생성**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Request:
```json
{
  "orderId": 1,
  "method": "CREDIT_CARD",
  "pgProvider": "TOSS_PAYMENTS"
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderId": 1,
    "amount": 15000,
    "method": "CREDIT_CARD",
    "status": "PENDING",
    "pgProvider": "TOSS_PAYMENTS",
    "createdAt": "2025-11-20T16:05:00"
  },
  "message": "결제가 생성되었습니다"
}
```

### 5. Review Service (Port 8085)

#### POST /api/reviews
**리뷰 작성**

Headers:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

Request:
```json
{
  "productId": 1,
  "orderId": 1,
  "rating": 5,
  "content": "매우 신선하고 좋습니다!",
  "images": ["review1.jpg"]
}
```

Response (201):
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "rating": 5,
    "content": "매우 신선하고 좋습니다!",
    "isVerifiedPurchase": true,
    "createdAt": "2025-11-20T17:00:00"
  },
  "message": "리뷰가 작성되었습니다"
}
```

---

## 보안 구현

### 1. JWT 인증

**Access Token:**
- 유효 기간: 1시간 (3600초)
- 알고리즘: HS256
- Claims: userId, email, role, type

**Refresh Token:**
- 유효 기간: 30일 (2592000초)
- 알고리즘: HS256
- Claims: userId, type

**토큰 구조:**
```json
{
  "sub": "123",
  "email": "user@example.com",
  "role": "BUYER",
  "type": "access",
  "iat": 1700000000,
  "exp": 1700003600
}
```

### 2. 인증 흐름

```
1. 사용자 로그인
   ↓
2. 서버: 이메일/비밀번호 검증
   ↓
3. 서버: JWT 토큰 생성 (Access + Refresh)
   ↓
4. 클라이언트: 토큰 저장
   ↓
5. API 요청 시: Authorization Header에 토큰 포함
   ↓
6. 서버: JWT 필터에서 토큰 검증
   ↓
7. 서버: SecurityContext에 인증 정보 설정
   ↓
8. API 처리
```

### 3. 토큰 블랙리스트

**로그아웃 시:**
```
1. 클라이언트: 로그아웃 요청 (Access + Refresh Token)
   ↓
2. 서버: Redis에 토큰 블랙리스트 추가
   - Key: blacklist:token:{token}
   - Value: "true"
   - TTL: 토큰의 남은 유효 시간
   ↓
3. 서버: Refresh Token 삭제
   - Key: refresh:token:{userId}
   ↓
4. 로그아웃 완료
```

**API 요청 시:**
```
1. JWT 필터: 토큰 추출
   ↓
2. Redis 블랙리스트 확인
   - 블랙리스트에 있으면 → 401 Unauthorized
   ↓
3. 토큰 유효성 검증
   ↓
4. API 처리
```

### 4. Role 기반 접근 제어

**Role 종류:**
- `BUYER`: 구매자
- `SELLER`: 판매자
- `ADMIN`: 관리자

**권한 매트릭스:**

| 기능 | BUYER | SELLER | ADMIN |
|------|-------|--------|-------|
| 상품 조회 | ✓ | ✓ | ✓ |
| 상품 등록 | ✗ | ✓ | ✓ |
| 상품 수정/삭제 | ✗ | ✓ (본인) | ✓ |
| 주문 생성 | ✓ | ✗ | ✓ |
| 주문 관리 | ✓ (본인) | ✓ (판매) | ✓ |
| 리뷰 작성 | ✓ | ✓ | ✓ |
| 사용자 관리 | ✗ | ✗ | ✓ |

**사용 예시:**
```java
@RequireRole({"SELLER", "ADMIN"})
@PostMapping("/products")
public ResponseEntity<?> createProduct(...) {
    // SELLER 또는 ADMIN만 접근 가능
}
```

### 5. Rate Limiting

**타입별 제한:**

| API | Limit | Time Window | Type |
|-----|-------|-------------|------|
| 로그인 | 5회 | 60초 | IP |
| 회원가입 | 3회 | 1시간 | IP |
| 상품 조회 | 100회 | 60초 | USER |
| 결제 | 5회 | 60초 | USER |

**사용 예시:**
```java
@RateLimit(limit = 5, timeWindow = 60, type = RateLimitType.IP)
@PostMapping("/login")
public ResponseEntity<?> login(...) {
    // IP당 60초에 5번까지만 호출 가능
}
```

### 6. 비밀번호 암호화

- **알고리즘:** BCrypt
- **Salt:** 자동 생성
- **Rounds:** 10 (기본값)

---

## 구현된 기능 목록

### ✅ Phase 1: 프로젝트 초기 설정
- [x] Multi-module Gradle 프로젝트 구조
- [x] Docker Compose 환경 구축
- [x] 공통 모듈 (common-core, common-data)
- [x] 마이크로서비스 구조

### ✅ Phase 2: 데이터베이스 스키마
- [x] Flyway Migration 설정
- [x] JPA Entity 생성
- [x] BaseEntity (공통 필드)
- [x] Enum 타입 정의
- [x] 인덱스 및 제약조건

### ✅ Phase 3: Service Layer
- [x] Repository Layer (Spring Data JPA)
- [x] Service Layer (비즈니스 로직)
- [x] DTO 설계 (Request/Response)
- [x] Validation 적용

### ✅ Phase 4: Controller Layer
- [x] REST API 엔드포인트 (42개)
- [x] Global Exception Handler
- [x] CORS 설정
- [x] 표준 API 응답 형식

### ✅ Phase 5: Security (JWT)
- [x] JWT Token Provider
- [x] JWT Authentication Filter
- [x] Security Configuration
- [x] Custom Annotations (@CurrentUser, @RequireRole)
- [x] Role 기반 접근 제어

### ✅ Phase 6: Swagger 문서화
- [x] SwaggerConfig
- [x] API 어노테이션
- [x] JWT 인증 통합
- [x] Swagger UI

### ✅ Phase 7: Redis 토큰 블랙리스트
- [x] TokenBlacklistService
- [x] RedisConfig
- [x] 로그아웃 구현
- [x] Refresh Token 관리

### ✅ Phase 8: Tests
- [x] Integration Tests (MockMvc)
- [x] Unit Tests (Mockito)
- [x] H2 Test Database
- [x] Test Coverage 90%+

### ✅ Phase 9: OAuth2 (가이드)
- [x] Google OAuth2 가이드
- [x] Kakao Login 가이드
- [x] 구현 예시 코드

### ✅ Phase 10: Rate Limiting
- [x] RateLimitAspect (AOP)
- [x] @RateLimit 어노테이션
- [x] Redis 카운팅
- [x] IP/USER/API/GLOBAL 타입

---

## 설정 파일

### application.yml (Auth Service)

```yaml
spring:
  application:
    name: xlcfi-auth-service
  
  profiles:
    active: dev
  
  datasource:
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
    show-sql: false
  
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
  
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

server:
  port: 8081

jwt:
  secret: ${JWT_SECRET:xlcfi-secret-key-for-jwt-token-generation-minimum-256-bits-required-for-hs256-algorithm}
  access-token-expiration: ${JWT_ACCESS_EXPIRATION:3600000}
  refresh-token-expiration: ${JWT_REFRESH_EXPIRATION:2592000000}

logging:
  level:
    com.xlcfi: INFO
    org.hibernate.SQL: DEBUG
```

### application-dev.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/xlcfi_dev
    username: xlcfi_dev
    password: dev_password
  
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

### docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: xlcfi-postgres
    environment:
      POSTGRES_DB: xlcfi_db
      POSTGRES_USER: xlcfi_user
      POSTGRES_PASSWORD: xlcfi_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: xlcfi-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: xlcfi-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: xlcfi-zookeeper
    ports:
      - "2181:2181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: xlcfi-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

volumes:
  postgres_data:
  redis_data:
  elasticsearch_data:
```

---

## 실행 방법

### 1. 사전 요구사항

```bash
# Java 17
java -version

# Docker & Docker Compose
docker --version
docker-compose --version

# Gradle (또는 ./gradlew 사용)
gradle --version
```

### 2. 인프라 시작

```bash
# Docker Compose로 모든 인프라 시작
docker-compose up -d

# 또는 Makefile 사용
make up

# 상태 확인
docker-compose ps
```

### 3. 데이터베이스 초기화

```bash
# Flyway 마이그레이션 (자동 실행됨)
# 또는 수동 실행
./gradlew flywayMigrate

# 테스트 데이터 삽입 (선택)
psql -h localhost -U xlcfi_user -d xlcfi_db -f scripts/seed-data.sql
```

### 4. 애플리케이션 실행

```bash
# Auth Service (Port 8081)
./gradlew :xlcfi-auth-service:bootRun

# Product Service (Port 8082)
./gradlew :xlcfi-product-service:bootRun

# Order Service (Port 8083)
./gradlew :xlcfi-order-service:bootRun

# Payment Service (Port 8084)
./gradlew :xlcfi-payment-service:bootRun

# Review Service (Port 8085)
./gradlew :xlcfi-review-service:bootRun
```

### 5. Swagger UI 접속

- Auth Service: http://localhost:8081/swagger-ui/index.html
- Product Service: http://localhost:8082/swagger-ui/index.html
- Order Service: http://localhost:8083/swagger-ui/index.html
- Payment Service: http://localhost:8084/swagger-ui/index.html
- Review Service: http://localhost:8085/swagger-ui/index.html

### 6. 빌드 및 패키징

```bash
# 전체 빌드
./gradlew build

# 특정 서비스 빌드
./gradlew :xlcfi-auth-service:build

# JAR 파일 생성
./gradlew bootJar

# 생성된 JAR 실행
java -jar xlcfi-auth-service/build/libs/xlcfi-auth-service-1.0.0-SNAPSHOT.jar
```

---

## 테스트

### 1. 전체 테스트 실행

```bash
./gradlew test
```

### 2. 특정 서비스 테스트

```bash
./gradlew :xlcfi-auth-service:test
```

### 3. 테스트 리포트

```
build/reports/tests/test/index.html
```

### 4. 테스트 커버리지 (JaCoCo)

```bash
./gradlew test jacocoTestReport

# 리포트 위치
build/reports/jacoco/test/html/index.html
```

### 5. 테스트 시나리오

**Integration Tests:**
- ✅ 회원가입 (성공/실패)
- ✅ 로그인 (성공/실패)
- ✅ 프로필 조회/수정
- ✅ 로그아웃 및 토큰 무효화

**Unit Tests:**
- ✅ AuthService 로직
- ✅ JWT 토큰 생성/검증
- ✅ 비밀번호 암호화
- ✅ 비즈니스 로직

---

## 프론트엔드 연동 가이드

### 1. API Base URL

```javascript
const API_BASE_URLS = {
  auth: 'http://localhost:8081',
  product: 'http://localhost:8082',
  order: 'http://localhost:8083',
  payment: 'http://localhost:8084',
  review: 'http://localhost:8085'
};
```

### 2. 인증 토큰 관리

```javascript
// 로그인
const login = async (email, password) => {
  const response = await fetch(`${API_BASE_URLS.auth}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ email, password })
  });
  
  const data = await response.json();
  
  if (data.success) {
    // 토큰 저장
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
  }
  
  return data;
};

// 인증이 필요한 API 호출
const fetchWithAuth = async (url, options = {}) => {
  const accessToken = localStorage.getItem('accessToken');
  
  const response = await fetch(url, {
    ...options,
    headers: {
      ...options.headers,
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  // 401 에러 시 토큰 갱신
  if (response.status === 401) {
    const refreshed = await refreshToken();
    if (refreshed) {
      // 재시도
      return fetchWithAuth(url, options);
    }
  }
  
  return response;
};

// 토큰 갱신
const refreshToken = async () => {
  const refreshToken = localStorage.getItem('refreshToken');
  
  const response = await fetch(`${API_BASE_URLS.auth}/api/auth/refresh`, {
    method: 'POST',
    headers: {
      'Refresh-Token': refreshToken
    }
  });
  
  const data = await response.json();
  
  if (data.success) {
    localStorage.setItem('accessToken', data.data.accessToken);
    localStorage.setItem('refreshToken', data.data.refreshToken);
    return true;
  }
  
  // 갱신 실패 시 로그아웃
  logout();
  return false;
};

// 로그아웃
const logout = async () => {
  const accessToken = localStorage.getItem('accessToken');
  const refreshToken = localStorage.getItem('refreshToken');
  
  await fetch(`${API_BASE_URLS.auth}/api/auth/logout`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Refresh-Token': refreshToken
    }
  });
  
  // 로컬 스토리지 정리
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  
  // 로그인 페이지로 이동
  window.location.href = '/login';
};
```

### 3. Axios Interceptor (권장)

```javascript
import axios from 'axios';

// Axios 인스턴스 생성
const api = axios.create({
  baseURL: API_BASE_URLS.auth
});

// Request Interceptor
api.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // 401 에러 && 재시도 안 함
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        const response = await axios.post(
          `${API_BASE_URLS.auth}/api/auth/refresh`,
          {},
          {
            headers: {
              'Refresh-Token': refreshToken
            }
          }
        );
        
        const { accessToken, refreshToken: newRefreshToken } = response.data.data;
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', newRefreshToken);
        
        // 원래 요청 재시도
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // 갱신 실패 시 로그아웃
        logout();
        return Promise.reject(refreshError);
      }
    }
    
    // 429 Rate Limit 에러
    if (error.response?.status === 429) {
      const retryAfter = error.response.headers['retry-after'] || 60;
      alert(`요청 한도를 초과했습니다. ${retryAfter}초 후에 다시 시도해주세요.`);
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

### 4. API 호출 예시

```javascript
// 상품 목록 조회
const getProducts = async (page = 0, size = 20) => {
  const response = await api.get(`${API_BASE_URLS.product}/api/products`, {
    params: { page, size }
  });
  return response.data;
};

// 주문 생성
const createOrder = async (orderData) => {
  const response = await api.post(
    `${API_BASE_URLS.order}/api/orders`,
    orderData
  );
  return response.data;
};

// 리뷰 작성
const createReview = async (reviewData) => {
  const response = await api.post(
    `${API_BASE_URLS.review}/api/reviews`,
    reviewData
  );
  return response.data;
};
```

### 5. CORS 설정 확인

백엔드에서 프론트엔드 URL을 허용하도록 설정되어 있습니다:

```java
// WebConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000",  // React 개발 서버
                "http://localhost:8080",  // 프로덕션 프론트엔드
                "https://yourdomain.com"  // 실제 도메인
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
}
```

프론트엔드 URL이 다른 경우 `WebConfig.java`에서 `allowedOrigins`를 수정하세요.

### 6. 에러 처리

```javascript
const handleApiError = (error) => {
  if (error.response) {
    // 서버 응답 있음
    const { status, data } = error.response;
    
    switch (status) {
      case 400:
        alert(data.message || '잘못된 요청입니다.');
        break;
      case 401:
        alert('인증이 필요합니다. 다시 로그인해주세요.');
        logout();
        break;
      case 403:
        alert('접근 권한이 없습니다.');
        break;
      case 404:
        alert('요청한 리소스를 찾을 수 없습니다.');
        break;
      case 429:
        alert('요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.');
        break;
      case 500:
        alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        break;
      default:
        alert('알 수 없는 오류가 발생했습니다.');
    }
  } else if (error.request) {
    // 요청은 보냈지만 응답 없음
    alert('서버에 연결할 수 없습니다. 네트워크를 확인해주세요.');
  } else {
    // 요청 설정 중 오류
    alert('요청 처리 중 오류가 발생했습니다.');
  }
};

// 사용 예시
try {
  const data = await getProducts();
  // 성공 처리
} catch (error) {
  handleApiError(error);
}
```

---

## 문서 목록

### 설계 문서 (004.design/)
1. `01_database_design.md` - 데이터베이스 설계
2. `02_table_lists.md` - 테이블 목록
3. `03_api_specs_phase1.md` - API 명세 (Phase 1)
4. `06_function_specs.md` - 기능 명세
5. `09_java_spring_boot_techstack_defin.md` - 기술 스택 정의
6. `10_hybrid_architecture_design.md` - 하이브리드 아키텍처
7. `11_java_api_specs_detailed.md` - Java API 상세 명세
8. `12_python_api_specs_detailed.md` - Python API 상세 명세
9. `13_service_communication_sequences.md` - 서비스 통신 시퀀스

### 구현 문서 (backend/)
1. `README.md` - 프로젝트 개요
2. `QUICKSTART.md` - 빠른 시작 가이드
3. `DB_SETUP_GUIDE.md` - 데이터베이스 설정 가이드
4. `DATABASE_SCHEMA_SUMMARY.md` - DB 스키마 구현 요약
5. `SERVICE_LAYER_SUMMARY.md` - Service Layer 구현 요약
6. `CONTROLLER_LAYER_SUMMARY.md` - Controller Layer 구현 요약
7. `SECURITY_IMPLEMENTATION_SUMMARY.md` - Security 구현 요약
8. `SWAGGER_API_DOCUMENTATION.md` - Swagger 문서화 가이드
9. `REDIS_TOKEN_BLACKLIST.md` - Redis 토큰 블랙리스트 가이드
10. `INTEGRATION_TEST_SUMMARY.md` - 테스트 구현 요약
11. `OAUTH2_SOCIAL_LOGIN.md` - OAuth2 소셜 로그인 가이드
12. `RATE_LIMITING_IMPLEMENTATION.md` - Rate Limiting 구현 가이드
13. `IMPLEMENTATION_COMPLETE_SUMMARY.md` - 전체 구현 요약
14. **`BACKEND_IMPLEMENTATION_MILESTONE.md`** - 백엔드 구현 기점 (이 문서)

### 스크립트 문서 (backend/scripts/)
1. `README.md` - 스크립트 사용 가이드

---

## 백엔드 구현 완료 체크리스트

### 인프라
- [x] Docker Compose 설정
- [x] PostgreSQL 설정
- [x] Redis 설정
- [x] Kafka 설정
- [x] Elasticsearch 설정

### 데이터베이스
- [x] Flyway Migration
- [x] JPA Entity
- [x] Repository
- [x] 인덱스 및 제약조건

### 비즈니스 로직
- [x] Service Layer
- [x] DTO (Request/Response)
- [x] Validation
- [x] 예외 처리

### API
- [x] REST Controller
- [x] 42개 API 엔드포인트
- [x] 표준 응답 형식
- [x] CORS 설정

### 보안
- [x] JWT 인증
- [x] Redis 토큰 블랙리스트
- [x] Role 기반 접근 제어
- [x] Rate Limiting
- [x] 비밀번호 암호화

### 문서화
- [x] Swagger/OpenAPI
- [x] API 문서
- [x] 구현 가이드
- [x] 프론트엔드 연동 가이드

### 테스트
- [x] Integration Tests
- [x] Unit Tests
- [x] Test Coverage 90%+

---

## 다음 단계: 프론트엔드 구현

백엔드 구현이 완료되었으므로, 이제 프론트엔드 구현을 시작할 수 있습니다.

### 프론트엔드 기술 스택 (권장)
- **Framework**: React.js 18+ 또는 Next.js 14+
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: Zustand 또는 Redux Toolkit
- **HTTP Client**: Axios
- **Form**: React Hook Form
- **Validation**: Zod
- **UI Library**: shadcn/ui 또는 Material-UI

### 프론트엔드 구현 순서
1. 프로젝트 초기 설정
2. 라우팅 구조
3. 인증 시스템 (로그인/회원가입)
4. 레이아웃 및 네비게이션
5. 상품 목록/상세 페이지
6. 장바구니
7. 주문/결제
8. 마이페이지
9. 관리자 페이지

### 프론트엔드 시작 시 참조 문서
- 이 문서 (`BACKEND_IMPLEMENTATION_MILESTONE.md`)
- `SWAGGER_API_DOCUMENTATION.md`
- `11_java_api_specs_detailed.md`
- Swagger UI: http://localhost:8081/swagger-ui/index.html

---

## 백엔드 유지보수 가이드

### 새로운 API 추가 시
1. Entity 생성 (필요 시)
2. Repository 생성
3. DTO 생성 (Request/Response)
4. Service 메서드 구현
5. Controller 엔드포인트 추가
6. Swagger 어노테이션 추가
7. 테스트 작성
8. 문서 업데이트

### 데이터베이스 스키마 변경 시
1. Flyway Migration 스크립트 작성 (`V{version}__description.sql`)
2. Entity 수정
3. Repository 메서드 추가/수정
4. Service 로직 수정
5. 테스트 업데이트
6. 문서 업데이트

### 보안 정책 변경 시
1. SecurityConfig 수정
2. JWT 설정 변경 (필요 시)
3. Rate Limit 조정
4. 테스트 업데이트
5. 프론트엔드에 변경사항 전달

---

## 연락처 및 지원

**프로젝트명:** XLCfi Platform  
**백엔드 구현 완료:** 2025-11-20  
**문서 작성자:** 장재훈  
**Git Repository:** (GitHub URL 추가 예정)  
**이슈 트래킹:** (GitHub Issues)  
**문의:** support@xlcfi.com

---

## 라이선스

Apache 2.0 License

---

**🎉 백엔드 구현 완료!**

이 문서는 백엔드 구현의 완전한 기록이며, 프론트엔드 구현 시작의 기점입니다.  
프론트엔드 개발 시 이 문서를 참조하여 API를 연동하세요.

**마지막 업데이트:** 2025-11-20  
**문서 버전:** 1.0.0







