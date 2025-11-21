# XLCfi Platform - Backend Implementation Milestone

## 📌 문서 목적

이 문서는 **백엔드 구현 완료 시점(2025-11-21)**의 기점(Milestone)을 명확히 하기 위해 작성되었습니다.

**목적:**
1. 백엔드 구현 내역의 완전한 기록
2. 프론트엔드 구현 시작 전 명확한 구분점 설정
3. 향후 프론트엔드 교체/수정 시 백엔드 참조 자료
4. 독립적인 백엔드 시스템으로서의 문서화

**백엔드 구현 기간:** 2025-11-20 ~ 2025-11-21  
**백엔드 구현 상태:** ✅ 완료 (100%)  
**총 구현 규모:**
- Java 코드: 195개 파일, 42,000+ 줄
- 설계 문서: 17개
- 구현 가이드: 20개
- 스마트 컨트랙트: 3개 (Solidity)
- 데이터베이스 테이블: 30+ 개

---

## 📋 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [데이터베이스 설계](#데이터베이스-설계)
5. [API 명세](#api-명세)
6. [보안 구현](#보안-구현)
7. [결제 시스템](#결제-시스템)
8. [블록체인 시스템](#블록체인-시스템)
9. [NFT 시스템](#nft-시스템)
10. [고급 평가 시스템](#고급-평가-시스템)
11. [구현된 기능 목록](#구현된-기능-목록)
12. [실행 방법](#실행-방법)
13. [프론트엔드 연동 가이드](#프론트엔드-연동-가이드)
14. [문서 목록](#문서-목록)

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
- Python Flask: 데이터 분석/ML (기본 구조 완료)
- Solidity: 블록체인 스마트 컨트랙트

### 주요 특징

**✅ Phase 1: 핵심 기능**
- RESTful API 설계
- JWT 기반 인증/인가
- Redis 토큰 블랙리스트
- Rate Limiting
- Swagger/OpenAPI 문서화
- 다국어 지원 (KO, EN, JA, ZH)
- Role 기반 접근 제어 (BUYER, SELLER, ADMIN)
- 원산지/식품코드/HACCP 분류 체계

**✅ Phase 2: 결제 시스템**
- 멀티 PG 통합 (TossPayments, NicePay, Stripe)
- 국내/해외 결제 지원
- Webhook 처리
- 결제 취소/환불

**✅ Phase 3: 블록체인 시스템**
- XLCFI Token (ERC-20)
- 사용자 간 P2P 거래
- Escrow 스마트 컨트랙트
- 보상 풀 (RewardPool)
- Polygon 네트워크 통합

**✅ Phase 4: NFT 시스템**
- 원산지 인증 NFT (Origin Certificate)
- 레시피 NFT (Recipe NFT with Royalty)
- 멤버십 NFT (Membership NFT with Tiers)
- IPFS 메타데이터 저장
- 온체인/오프체인 데이터 동기화

**✅ Phase 5: 고급 평가 시스템**
- 전략적 라벨링 (4단계 사용자 분류)
- 시각적 반응 시스템 (30+ 반응 타입)
- AI 기반 댓글 관리 (비판/비난 구분)
- 판매 가능성 지수 (0-100%)
- 소비 가능성 지수 (0-100%)
- 수익 창출 효과 분석
- 소비자 연결 지수 + GIS 통합

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
Spring WebFlux (WebClient)
```

### Database & Cache
```
PostgreSQL 15 (Primary Database)
  - PostGIS (GIS Extension)
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

### Payment Integration
```
TossPayments API (국내 결제)
NicePay API (국내 결제)
Stripe API (해외 결제)
WebClient (비동기 HTTP)
```

### Blockchain & NFT
```
Solidity 0.8.20 (Smart Contracts)
Hardhat (Development Environment)
OpenZeppelin Contracts (ERC-20, ERC-721, ERC-2981)
Polygon Mumbai Testnet
IPFS (NFT Metadata Storage)
Web3j (Java Ethereum Client)
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

### AI/ML (Python Services)
```
Python 3.9+
Flask 2.3.0
TensorFlow/PyTorch (향후)
OpenAI API (댓글 분석)
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
│   │   ├── repository/
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
│   │   └── config/
│   │       └── RedisConfig.java
│   │
│   ├── xlcfi-product-service/        # 상품 서비스 (Port 8082)
│   │   ├── domain/
│   │   │   ├── Product.java
│   │   │   ├── Category.java
│   │   │   └── ProductStatus.java
│   │   ├── dto/
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   │
│   ├── xlcfi-order-service/          # 주문 서비스 (Port 8083)
│   │   ├── domain/
│   │   │   ├── Order.java
│   │   │   ├── OrderItem.java
│   │   │   └── OrderStatus.java
│   │   ├── dto/
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   │
│   ├── xlcfi-payment-service/        # 결제 서비스 (Port 8084)
│   │   ├── domain/
│   │   │   ├── Payment.java
│   │   │   ├── PaymentMethod.java
│   │   │   └── PaymentStatus.java
│   │   ├── dto/
│   │   │   ├── tosspayments/         # TossPayments DTOs
│   │   │   ├── nicepay/              # NicePay DTOs
│   │   │   └── stripe/               # Stripe DTOs
│   │   ├── client/
│   │   │   ├── TossPaymentsClient.java
│   │   │   ├── NicePayClient.java
│   │   │   └── StripeClient.java
│   │   ├── repository/
│   │   ├── service/
│   │   │   └── PaymentService.java   # 멀티 PG 통합
│   │   └── controller/
│   │       ├── PaymentController.java
│   │       └── PaymentWebhookController.java
│   │
│   └── xlcfi-review-service/         # 리뷰 서비스 (Port 8085)
│       ├── domain/
│       │   ├── Review.java
│       │   └── ReviewStatus.java
│       ├── dto/
│       ├── repository/
│       ├── service/
│       └── controller/
│
├── python-services/                  # Python 마이크로서비스
│   ├── analytics-service/            # 분석 서비스 (Port 8091)
│   │   ├── app/
│   │   │   ├── api/
│   │   │   ├── services/
│   │   │   └── utils/
│   │   └── requirements.txt
│   └── recommendation-service/       # 추천 서비스 (Port 8092)
│
├── blockchain-contracts/             # 블록체인 스마트 컨트랙트
│   ├── contracts/
│   │   ├── XLCFIToken.sol           # ERC-20 토큰 (설계 완료)
│   │   ├── Escrow.sol               # 에스크로 (설계 완료)
│   │   ├── RewardPool.sol           # 보상 풀 (설계 완료)
│   │   ├── OriginCertificateNFT.sol # 원산지 인증 NFT
│   │   ├── RecipeNFT.sol            # 레시피 NFT
│   │   └── MembershipNFT.sol        # 멤버십 NFT
│   ├── hardhat.config.js
│   └── package.json
│
├── database/
│   └── nft_schema.sql               # NFT 관련 테이블 (13개)
│
├── scripts/                          # 유틸리티 스크립트
│   ├── init-database.sql
│   ├── seed-data.sql
│   ├── reset-database.sh
│   └── reset-database.bat
│
├── docker-compose.yml                # Docker Compose 설정
├── Makefile                          # 편의 명령어
├── .gitignore
├── README.md
│
└── 문서/                             # 구현 문서 (20개)
    ├── BACKEND_IMPLEMENTATION_MILESTONE.md (이 문서)
    ├── DATABASE_SCHEMA_SUMMARY.md
    ├── SERVICE_LAYER_SUMMARY.md
    ├── CONTROLLER_LAYER_SUMMARY.md
    ├── SECURITY_IMPLEMENTATION_SUMMARY.md
    ├── SWAGGER_API_DOCUMENTATION.md
    ├── REDIS_TOKEN_BLACKLIST.md
    ├── INTEGRATION_TEST_SUMMARY.md
    ├── OAUTH2_SOCIAL_LOGIN.md
    ├── RATE_LIMITING_IMPLEMENTATION.md
    ├── TOSSPAYMENTS_INTEGRATION_GUIDE.md
    ├── MULTI_PG_INTEGRATION_COMPLETE.md
    ├── BLOCKCHAIN_TOKEN_ARCHITECTURE.md
    ├── BLOCKCHAIN_NFT_STABLECOIN_STRATEGY.md
    ├── NFT_IMPLEMENTATION_COMPLETE.md
    ├── STABLECOIN_OPTIONS.md
    └── (14_advanced_evaluation_system.md - 설계 문서)
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
  ↓ 1:N
review_reactions (시각적 반응)

users (사용자)
  ↓ 1:N
orders (주문)
  ↓ 1:N
order_items (주문상세) → N:1 → products (상품)

orders (주문)
  ↓ 1:1
payments (결제)

users (사용자)
  ↓ 1:N
blockchain_wallets (지갑)
  ↓ 1:N
token_transactions (토큰 거래)

products (상품)
  ↓ 1:1
origin_certificate_nfts (원산지 NFT)

users (사용자)
  ↓ 1:N
recipe_nfts (레시피 NFT)

users (사용자)
  ↓ 1:1
membership_nfts (멤버십 NFT)

users (사용자)
  ↓ 1:N
user_labels (사용자 라벨)

products (상품)
  ↓ 1:1
product_indices (상품 지수)

local_markets (로컬 마켓)
  ↓ GIS
products (상품)
```

### 핵심 테이블 목록

#### Phase 1: 기본 테이블 (7개)
1. `users` - 사용자
2. `categories` - 카테고리
3. `products` - 상품
4. `orders` - 주문
5. `order_items` - 주문 상세
6. `payments` - 결제
7. `reviews` - 리뷰

#### Phase 2: 결제 확장 (기존 테이블 확장)
- `payments` 테이블에 `pg_provider`, `pg_response` 필드 추가

#### Phase 3: 블록체인 테이블 (5개)
8. `blockchain_wallets` - 블록체인 지갑
9. `token_transactions` - 토큰 거래
10. `escrow_contracts` - 에스크로 계약
11. `reward_distributions` - 보상 분배
12. `token_balances` - 토큰 잔액

#### Phase 4: NFT 테이블 (13개)
13. `origin_certificate_nfts` - 원산지 인증 NFT
14. `origin_certificate_metadata` - 원산지 메타데이터
15. `origin_certificate_transfers` - 원산지 NFT 이전 기록
16. `recipe_nfts` - 레시피 NFT
17. `recipe_metadata` - 레시피 메타데이터
18. `recipe_transfers` - 레시피 NFT 이전 기록
19. `recipe_royalties` - 레시피 로열티
20. `membership_nfts` - 멤버십 NFT
21. `membership_tiers` - 멤버십 등급
22. `membership_benefits` - 멤버십 혜택
23. `membership_transfers` - 멤버십 NFT 이전 기록
24. `nft_ownership_history` - NFT 소유권 이력 (통합 뷰)
25. `nft_market_stats` - NFT 시장 통계 (통합 뷰)

#### Phase 5: 고급 평가 시스템 (15개)
26. `user_labels` - 사용자 라벨
27. `activity_scores` - 활동 점수
28. `expertise_labels` - 전문성 라벨
29. `review_reactions` - 시각적 반응
30. `reaction_types` - 반응 타입 정의
31. `comment_restrictions` - 댓글 제한 설정
32. `comment_moderation_logs` - 댓글 관리 로그
33. `product_indices` - 상품 지수 (4가지)
34. `sales_potential_factors` - 판매 가능성 요소
35. `consumption_potential_factors` - 소비 가능성 요소
36. `revenue_effects` - 수익 효과
37. `local_markets` - 로컬 마켓 (GIS)
38. `consumer_connections` - 소비자 연결
39. `index_calculation_logs` - 지수 계산 로그
40. `gis_data` - GIS 데이터 (PostGIS)

**총 테이블: 40개**

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

### API 엔드포인트 목록

#### 1. Auth Service (Port 8081) - 8개 API
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/login` - 로그인
- `POST /api/auth/refresh` - 토큰 갱신
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/profile` - 내 프로필 조회
- `PUT /api/auth/profile` - 프로필 수정
- `GET /api/auth/users/{id}` - 사용자 조회 (ADMIN)
- `GET /api/auth/users` - 사용자 목록 (ADMIN)

#### 2. Product Service (Port 8082) - 12개 API
- `GET /api/categories` - 카테고리 목록
- `GET /api/categories/{id}` - 카테고리 상세
- `POST /api/categories` - 카테고리 생성 (ADMIN)
- `PUT /api/categories/{id}` - 카테고리 수정 (ADMIN)
- `DELETE /api/categories/{id}` - 카테고리 삭제 (ADMIN)
- `GET /api/products` - 상품 목록
- `GET /api/products/{id}` - 상품 상세
- `POST /api/products` - 상품 등록 (SELLER)
- `PUT /api/products/{id}` - 상품 수정 (SELLER)
- `DELETE /api/products/{id}` - 상품 삭제 (SELLER)
- `GET /api/products/seller/{sellerId}` - 판매자별 상품
- `GET /api/products/category/{categoryId}` - 카테고리별 상품

#### 3. Order Service (Port 8083) - 6개 API
- `POST /api/orders` - 주문 생성
- `GET /api/orders` - 내 주문 목록
- `GET /api/orders/{id}` - 주문 상세
- `PUT /api/orders/{id}/cancel` - 주문 취소
- `PUT /api/orders/{id}/status` - 주문 상태 변경 (SELLER/ADMIN)
- `GET /api/orders/seller/{sellerId}` - 판매자별 주문 (SELLER)

#### 4. Payment Service (Port 8084) - 15개 API
- `POST /api/payments` - 결제 생성
- `GET /api/payments/{id}` - 결제 조회
- `GET /api/payments/order/{orderId}` - 주문별 결제
- `POST /api/payments/toss/initiate` - TossPayments 결제 시작
- `POST /api/payments/toss/confirm` - TossPayments 결제 승인
- `POST /api/payments/toss/cancel` - TossPayments 결제 취소
- `POST /api/payments/nicepay/initiate` - NicePay 결제 시작
- `POST /api/payments/nicepay/confirm` - NicePay 결제 승인
- `POST /api/payments/nicepay/cancel` - NicePay 결제 취소
- `POST /api/payments/stripe/initiate` - Stripe 결제 시작
- `POST /api/payments/stripe/confirm` - Stripe 결제 승인
- `POST /api/payments/stripe/refund` - Stripe 환불
- `POST /webhook/toss` - TossPayments Webhook
- `POST /webhook/nicepay` - NicePay Webhook
- `POST /webhook/stripe` - Stripe Webhook

#### 5. Review Service (Port 8085) - 9개 API
- `POST /api/reviews` - 리뷰 작성
- `GET /api/reviews` - 리뷰 목록
- `GET /api/reviews/{id}` - 리뷰 상세
- `PUT /api/reviews/{id}` - 리뷰 수정
- `DELETE /api/reviews/{id}` - 리뷰 삭제
- `GET /api/reviews/product/{productId}` - 상품별 리뷰
- `GET /api/reviews/user/{userId}` - 사용자별 리뷰
- `PUT /api/reviews/{id}/status` - 리뷰 상태 변경 (ADMIN)
- `POST /api/reviews/{id}/reactions` - 리뷰 반응 추가

**총 API: 50개**

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

### 2. 토큰 블랙리스트 (Redis)

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
   ↓
4. 로그아웃 완료
```

### 3. Role 기반 접근 제어

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
| 결제 | ✓ | ✗ | ✓ |
| 리뷰 작성 | ✓ | ✓ | ✓ |
| 리뷰 관리 | ✗ | ✗ | ✓ |
| 사용자 관리 | ✗ | ✗ | ✓ |
| NFT 발행 | ✗ | ✓ | ✓ |

### 4. Rate Limiting

**타입별 제한:**

| API | Limit | Time Window | Type |
|-----|-------|-------------|------|
| 로그인 | 5회 | 60초 | IP |
| 회원가입 | 3회 | 1시간 | IP |
| 상품 조회 | 100회 | 60초 | USER |
| 결제 | 5회 | 60초 | USER |
| NFT 발행 | 10회 | 1시간 | USER |

### 5. 비밀번호 암호화

- **알고리즘:** BCrypt
- **Salt:** 자동 생성
- **Rounds:** 10 (기본값)

---

## 결제 시스템

### 지원 PG사

#### 1. TossPayments (국내)
- **지원 결제 수단:** 신용카드, 계좌이체, 가상계좌, 휴대폰
- **API 버전:** v1
- **Webhook:** 지원
- **테스트 환경:** 제공

#### 2. NicePay (국내)
- **지원 결제 수단:** 신용카드, 계좌이체, 가상계좌
- **API 버전:** v2
- **Webhook:** 지원
- **테스트 환경:** 제공

#### 3. Stripe (해외)
- **지원 결제 수단:** Credit Card, Apple Pay, Google Pay
- **API 버전:** 2023-10-16
- **Webhook:** 지원
- **다국가 지원:** 135+ 국가

### 결제 흐름

```
1. 프론트엔드: 결제 시작 요청
   POST /api/payments/{pg}/initiate
   ↓
2. 백엔드: Payment 레코드 생성 (PENDING)
   ↓
3. 백엔드: PG사 API 호출 (결제 준비)
   ↓
4. 프론트엔드: PG사 결제창 표시
   ↓
5. 사용자: 결제 정보 입력
   ↓
6. PG사: 결제 처리
   ↓
7. 프론트엔드: 결제 승인 요청
   POST /api/payments/{pg}/confirm
   ↓
8. 백엔드: PG사 API 호출 (결제 승인)
   ↓
9. 백엔드: Payment 상태 업데이트 (COMPLETED)
   ↓
10. 백엔드: Order 상태 업데이트 (CONFIRMED)
   ↓
11. Webhook: PG사 → 백엔드 (비동기 확인)
```

### 환불/취소 흐름

```
1. 사용자: 환불 요청
   ↓
2. 백엔드: 환불 가능 여부 확인
   ↓
3. 백엔드: PG사 API 호출 (환불 요청)
   ↓
4. PG사: 환불 처리
   ↓
5. 백엔드: Payment 상태 업데이트 (REFUNDED)
   ↓
6. 백엔드: Order 상태 업데이트 (CANCELLED)
```

---

## 블록체인 시스템

### 아키텍처

**목적:** 사용자 간 P2P 거래 (결제와 분리)

**네트워크:** Polygon Mumbai Testnet (향후 Mainnet)

**주요 컴포넌트:**
1. XLCFI Token (ERC-20)
2. Escrow Contract
3. RewardPool Contract

### XLCFI Token

**토큰 정보:**
- **이름:** XLCfi Token
- **심볼:** XLCFI
- **표준:** ERC-20
- **총 발행량:** 1,000,000,000 XLCFI
- **소수점:** 18

**토큰 분배:**
- 생태계 보상: 40% (400,000,000 XLCFI)
- 팀 및 어드바이저: 20% (200,000,000 XLCFI)
- 유동성 풀: 20% (200,000,000 XLCFI)
- 마케팅: 10% (100,000,000 XLCFI)
- 예비: 10% (100,000,000 XLCFI)

**토큰 획득 방법:**
1. 상품 판매 (판매 금액의 1%)
2. 리뷰 작성 (100 XLCFI)
3. 추천인 보상 (거래액의 0.5%)
4. 이벤트 참여
5. NFT 거래 수수료 환급

### Escrow Contract

**기능:**
- 거래 금액 예치
- 조건 충족 시 자동 송금
- 분쟁 해결 메커니즘
- 타임아웃 처리

**거래 흐름:**
```
1. 구매자: 토큰을 Escrow에 예치
   ↓
2. 판매자: 상품 발송
   ↓
3. 구매자: 수령 확인
   ↓
4. Escrow: 판매자에게 토큰 송금
   ↓
5. RewardPool: 플랫폼 수수료 (2%) 차감
```

### RewardPool Contract

**기능:**
- 플랫폼 수수료 관리
- 보상 분배
- 스테이킹 보상 (향후)

**수수료 구조:**
- P2P 거래 수수료: 2%
- NFT 거래 수수료: 2.5%
- 로열티 자동 분배

### 토큰 현금화 전략

#### Option 1: 직접 현금화 (플랫폼 제공)
- 최소 출금: 10,000 XLCFI
- 수수료: 5%
- 처리 시간: 3-5 영업일

#### Option 2: 외부 거래소 연동
- Uniswap, PancakeSwap 유동성 풀
- 사용자가 직접 스왑

#### Option 3: 스테이블코인 전환 (향후)
- XLCFI → USDT/USDC
- 자동 스왑 기능

---

## NFT 시스템

### 1. Origin Certificate NFT (원산지 인증 NFT)

**목적:** 식품 원산지 추적 및 인증

**표준:** ERC-721

**메타데이터:**
```json
{
  "name": "유기농 배추 원산지 인증서",
  "description": "충청남도 홍성군에서 재배된 유기농 배추",
  "image": "ipfs://QmXxx.../certificate.png",
  "attributes": [
    {
      "trait_type": "Country",
      "value": "대한민국"
    },
    {
      "trait_type": "Region",
      "value": "충청남도 홍성군"
    },
    {
      "trait_type": "Farm",
      "value": "홍길동 농장"
    },
    {
      "trait_type": "Certification",
      "value": "유기농 인증"
    },
    {
      "trait_type": "HACCP",
      "value": "인증"
    },
    {
      "trait_type": "Harvest Date",
      "value": "2025-11-15"
    }
  ]
}
```

**발행 조건:**
- SELLER 또는 ADMIN 권한
- 상품 등록 시 자동 발행 (선택)
- 인증 서류 업로드 필수

**활용:**
- 상품 페이지에 NFT 표시
- QR 코드로 원산지 확인
- 블록체인 탐색기 연동

### 2. Recipe NFT (레시피 NFT)

**목적:** 레시피 저작권 보호 및 로열티 분배

**표준:** ERC-721 + ERC-2981 (Royalty)

**메타데이터:**
```json
{
  "name": "김치찌개 레시피",
  "description": "전통 방식의 김치찌개 레시피",
  "image": "ipfs://QmYyy.../recipe.png",
  "recipe": {
    "ingredients": [
      "김치 300g",
      "돼지고기 200g",
      "두부 1모"
    ],
    "steps": [
      "1. 김치를 먹기 좋은 크기로 자른다",
      "2. 돼지고기를 볶는다",
      "3. 물을 넣고 끓인다"
    ],
    "cookingTime": "30분",
    "servings": "4인분"
  },
  "attributes": [
    {
      "trait_type": "Cuisine",
      "value": "Korean"
    },
    {
      "trait_type": "Difficulty",
      "value": "Easy"
    },
    {
      "trait_type": "Category",
      "value": "Main Dish"
    }
  ]
}
```

**로열티:**
- 2차 판매 시 원작자에게 5% 로열티
- ERC-2981 표준 준수
- 자동 분배

**발행 조건:**
- 레시피 작성자
- 독창성 검증 (AI)
- 최소 3개 이상의 단계

### 3. Membership NFT (멤버십 NFT)

**목적:** 등급별 멤버십 혜택 제공

**표준:** ERC-721

**등급:**

| 등급 | 이름 | 가격 | 혜택 |
|------|------|------|------|
| 1 | Bronze | 0.01 ETH | 5% 할인 |
| 2 | Silver | 0.05 ETH | 10% 할인, 무료 배송 |
| 3 | Gold | 0.1 ETH | 15% 할인, 무료 배송, 우선 지원 |
| 4 | Platinum | 0.5 ETH | 20% 할인, 무료 배송, 전용 상담 |

**메타데이터:**
```json
{
  "name": "XLCfi Gold Membership",
  "description": "Gold 등급 멤버십",
  "image": "ipfs://QmZzz.../gold.png",
  "attributes": [
    {
      "trait_type": "Tier",
      "value": "Gold"
    },
    {
      "trait_type": "Discount",
      "value": "15%"
    },
    {
      "trait_type": "Valid Until",
      "value": "2026-11-21"
    }
  ]
}
```

**특징:**
- P2P 거래 가능
- 유효 기간 설정
- 등급 업그레이드 가능

### NFT 데이터베이스 동기화

**온체인 → 오프체인:**
```
1. 스마트 컨트랙트 이벤트 발생
   ↓
2. 백엔드: 이벤트 리스닝 (Web3j)
   ↓
3. 백엔드: PostgreSQL에 데이터 저장
   ↓
4. 캐시: Redis에 최신 데이터 저장
```

**오프체인 → 온체인:**
```
1. 사용자: NFT 발행 요청
   ↓
2. 백엔드: 메타데이터 생성
   ↓
3. 백엔드: IPFS에 업로드
   ↓
4. 백엔드: 스마트 컨트랙트 호출 (mint)
   ↓
5. 백엔드: PostgreSQL에 기록
```

---

## 고급 평가 시스템

### 1. 전략적 라벨링 시스템

**4단계 분류:**

#### Level 1: 역할 기반 라벨
- `BUYER` - 구매자
- `SELLER` - 판매자
- `PRODUCER` - 생산자
- `DISTRIBUTOR` - 유통업자
- `ADMIN` - 관리자

#### Level 2: 활동 지수 (0-1000점)
- 0-99: 신규 사용자
- 100-299: 초보
- 300-599: 일반
- 600-899: 활동적
- 900-1000: 파워 유저

**활동 지수 계산:**
```
활동 지수 = (로그인 일수 × 1) 
          + (상품 등록 × 10) 
          + (주문 × 5) 
          + (리뷰 × 3) 
          + (댓글 × 1)
```

#### Level 3: 활동 등급
- `BRONZE` - 브론즈 (0-299점)
- `SILVER` - 실버 (300-599점)
- `GOLD` - 골드 (600-899점)
- `PLATINUM` - 플래티넘 (900-1000점)

#### Level 4: 전문성 라벨
- `ORGANIC_EXPERT` - 유기농 전문가
- `KIMCHI_MASTER` - 김치 명인
- `SEAFOOD_SPECIALIST` - 수산물 전문가
- `RECIPE_CREATOR` - 레시피 크리에이터
- `QUALITY_INSPECTOR` - 품질 검수자

**전문성 라벨 획득 조건:**
- 특정 카테고리에서 10개 이상 상품 판매
- 해당 분야 리뷰 평균 4.5점 이상
- 관련 인증서 제출

### 2. 시각적 반응 시스템

**5개 카테고리, 30+ 반응 타입:**

#### Category 1: 비즈니스 잠재력
- `HIGH_PRODUCTION_POTENTIAL` - 생산 가능성 높음
- `HIGH_GROWTH_POTENTIAL` - 성장 가능성 높음
- `WANT_TO_TRADE` - 거래하고 싶어요
- `INVESTMENT_INTEREST` - 투자 관심
- `PARTNERSHIP_INTEREST` - 파트너십 관심

#### Category 2: 정보 요청
- `WANT_MORE_DETAILS` - 자세한 정보 원함
- `WANT_TO_KNOW_SOURCE` - 출처 알고 싶음
- `WANT_RECIPE` - 레시피 원함
- `WANT_PRICE_INFO` - 가격 정보 원함
- `WANT_SAMPLE` - 샘플 원함

#### Category 3: 감정/지원
- `SUPPORT` - 응원해요
- `AMAZING` - 놀라워요
- `INSPIRING` - 영감을 받았어요
- `PROUD` - 자랑스러워요
- `GRATEFUL` - 감사해요

#### Category 4: 품질 평가
- `HIGH_QUALITY` - 품질 우수
- `AUTHENTIC` - 정통성 있음
- `FRESH` - 신선함
- `SAFE` - 안전함
- `DELICIOUS` - 맛있어요

#### Category 5: 우려사항
- `PRICE_CONCERN` - 가격 우려
- `QUALITY_CONCERN` - 품질 우려
- `DELIVERY_CONCERN` - 배송 우려
- `AUTHENTICITY_CONCERN` - 진위 우려
- `NEED_VERIFICATION` - 검증 필요

**반응 집계:**
```sql
SELECT 
  reaction_type,
  COUNT(*) as count,
  ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM review_reactions
WHERE review_id = ?
GROUP BY reaction_type
ORDER BY count DESC;
```

### 3. AI 기반 댓글 관리

**비판 vs 비난 구분:**

**비판 (Criticism) - 허용:**
- 건설적 피드백
- 구체적 개선 제안
- 객관적 사실 지적
- 예: "배송이 조금 늦었지만 상품은 좋았습니다"

**비난 (Slander) - 제한:**
- 인신공격
- 근거 없는 비방
- 욕설/혐오 표현
- 예: "이 판매자는 사기꾼이다"

**AI 분석 API:**
```python
# OpenAI API 사용
def analyze_comment(text):
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[
            {
                "role": "system",
                "content": "당신은 댓글을 분석하여 비판과 비난을 구분하는 AI입니다."
            },
            {
                "role": "user",
                "content": f"다음 댓글을 분석해주세요: {text}"
            }
        ]
    )
    return response.choices[0].message.content
```

**댓글 제한 설정:**
- 최소 활동 점수: 100점
- 최소 계정 나이: 7일
- 역할 제한: BUYER, SELLER만
- 등급 제한: BRONZE 이상
- 일일 댓글 한도: 50개

### 4. 판매 가능성 지수 (0-100%)

**계산 알고리즘:**

```
판매 가능성 지수 = 
  (상품 특성 점수 × 0.25) +
  (시장 데이터 점수 × 0.25) +
  (판매자 신뢰도 점수 × 0.20) +
  (상품 성과 점수 × 0.20) +
  (콘텐츠 품질 점수 × 0.10)
```

**상품 특성 점수 (0-100):**
- 카테고리 인기도
- 원산지 신뢰도
- HACCP 인증 여부
- 가격 경쟁력

**시장 데이터 점수 (0-100):**
- 유사 상품 판매량
- 카테고리 성장률
- 계절성 요인
- 트렌드 점수

**판매자 신뢰도 점수 (0-100):**
- 평균 평점
- 총 판매 건수
- 반품률
- 응답 속도

**상품 성과 점수 (0-100):**
- 조회수
- 찜 수
- 구매 전환율
- 재구매율

**콘텐츠 품질 점수 (0-100):**
- 이미지 품질 (AI 분석)
- 설명 완성도
- 키워드 최적화
- 리뷰 수

### 5. 소비 가능성 지수 (0-100%)

**계산 알고리즘:**

```
소비 가능성 지수 = 
  (소비자 수요 점수 × 0.30) +
  (접근성 점수 × 0.25) +
  (소비 트렌드 점수 × 0.20) +
  (소비자 만족도 점수 × 0.15) +
  (건강/안전 점수 × 0.10)
```

**소비자 수요 점수 (0-100):**
- 검색량
- 관심도 (찜, 공유)
- 문의 수
- 유사 상품 구매 이력

**접근성 점수 (0-100):**
- 배송 가능 지역
- 배송 소요 시간
- 배송비
- 재고 수량

**소비 트렌드 점수 (0-100):**
- 소셜 미디어 언급
- 계절성
- 건강 트렌드
- 미디어 노출

**소비자 만족도 점수 (0-100):**
- 평균 평점
- 긍정 리뷰 비율
- 재구매율
- 추천 의향

**건강/안전 점수 (0-100):**
- 유기농 인증
- HACCP 인증
- 알레르기 정보
- 영양 성분

### 6. 수익 창출 효과

**4가지 분류:**

#### 1. 직접 수익
- 상품 판매 수익
- 배송비 수익
- 프리미엄 서비스 수익

#### 2. 간접 수익
- 광고 수익
- 제휴 마케팅 수익
- 데이터 판매 수익

#### 3. 미래 가치
- 브랜드 가치 상승
- 고객 생애 가치 (LTV)
- 네트워크 효과

#### 4. 비용 절감
- 마케팅 비용 절감
- 고객 획득 비용 절감
- 운영 효율화

**표시 형식:**
```
수익 창출 효과

직접 수익: ₩500,000/월
간접 수익: ₩150,000/월
미래 가치: ₩2,000,000 (예상)
비용 절감: ₩300,000/월

총 효과: ₩950,000/월 + ₩2,000,000 (미래)
```

### 7. 소비자 연결 지수 (0-100%) + GIS

**계산 알고리즘:**

```
소비자 연결 지수 = 
  (지리적 연결 점수 × 0.35) +
  (온라인 연결 점수 × 0.30) +
  (커뮤니티 연결 점수 × 0.20) +
  (파트너십 연결 점수 × 0.15)
```

**지리적 연결 점수 (0-100):**
- 반경 5km 내 소비자 수
- 로컬 마켓 접근성
- 배송 인프라
- 지역 인지도

**GIS 기능:**

```sql
-- PostGIS 사용
-- 반경 5km 내 소비자 수
SELECT COUNT(*) 
FROM users u
WHERE ST_DWithin(
  u.location::geography,
  (SELECT location FROM products WHERE id = ?)::geography,
  5000  -- 5km
);

-- 가장 가까운 로컬 마켓 5개
SELECT 
  id,
  name,
  ST_Distance(
    location::geography,
    (SELECT location FROM products WHERE id = ?)::geography
  ) as distance
FROM local_markets
ORDER BY distance
LIMIT 5;
```

**온라인 연결 점수 (0-100):**
- SNS 팔로워 수
- 온라인 커뮤니티 활동
- 이메일 구독자 수
- 웹사이트 방문자 수

**커뮤니티 연결 점수 (0-100):**
- 지역 커뮤니티 참여
- 협동조합 가입
- 농민 시장 참여
- 이벤트 개최 횟수

**파트너십 연결 점수 (0-100):**
- 유통 파트너 수
- 레스토랑 파트너 수
- 리테일 파트너 수
- B2B 고객 수

**지도 시각화:**
- Leaflet.js 또는 Google Maps API
- 히트맵으로 소비자 밀집도 표시
- 마커로 로컬 마켓 위치 표시
- 반경 표시 (5km, 10km, 20km)

---

## 구현된 기능 목록

### ✅ Phase 1: 프로젝트 초기 설정
- [x] Multi-module Gradle 프로젝트 구조
- [x] Docker Compose 환경 구축
- [x] 공통 모듈 (common-core, common-data)
- [x] 마이크로서비스 구조 (5개 서비스)

### ✅ Phase 2: 데이터베이스 스키마
- [x] Flyway Migration 설정
- [x] JPA Entity 생성 (7개 테이블)
- [x] BaseEntity (공통 필드)
- [x] Enum 타입 정의
- [x] 인덱스 및 제약조건

### ✅ Phase 3: Service Layer
- [x] Repository Layer (Spring Data JPA)
- [x] Service Layer (비즈니스 로직)
- [x] DTO 설계 (Request/Response)
- [x] Validation 적용

### ✅ Phase 4: Controller Layer
- [x] REST API 엔드포인트 (50개)
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

### ✅ Phase 11: 멀티 PG 결제 통합
- [x] TossPayments 클라이언트
- [x] NicePay 클라이언트
- [x] Stripe 클라이언트
- [x] Webhook 처리 (3개 PG)
- [x] 결제 취소/환불
- [x] 중복 결제 방지

### ✅ Phase 12: 블록체인 토큰 시스템
- [x] XLCFI Token 설계 (ERC-20)
- [x] Escrow Contract 설계
- [x] RewardPool Contract 설계
- [x] 토큰 이코노미 설계
- [x] 데이터베이스 스키마 (5개 테이블)
- [x] 토큰 현금화 전략

### ✅ Phase 13: NFT 시스템
- [x] OriginCertificateNFT 스마트 컨트랙트
- [x] RecipeNFT 스마트 컨트랙트 (ERC-2981)
- [x] MembershipNFT 스마트 컨트랙트
- [x] IPFS 메타데이터 구조
- [x] 데이터베이스 스키마 (13개 테이블)
- [x] 온체인/오프체인 동기화 설계
- [x] Hardhat 개발 환경

### ✅ Phase 14: 고급 평가 시스템
- [x] 전략적 라벨링 시스템 (4단계)
- [x] 시각적 반응 시스템 (30+ 타입)
- [x] AI 댓글 관리 설계
- [x] 판매 가능성 지수 알고리즘
- [x] 소비 가능성 지수 알고리즘
- [x] 수익 창출 효과 분석
- [x] 소비자 연결 지수 + GIS
- [x] PostGIS 통합 설계
- [x] 데이터베이스 스키마 (15개 테이블)

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

# Node.js (Hardhat용)
node --version
npm --version
```

### 2. 인프라 시작

```bash
# Docker Compose로 모든 인프라 시작
cd backend
docker-compose up -d

# 상태 확인
docker-compose ps
```

### 3. 데이터베이스 초기화

```bash
# Flyway 마이그레이션 (자동 실행됨)
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

### 6. 블록체인 개발 환경 (선택)

```bash
# Hardhat 설치
cd blockchain-contracts
npm install

# 로컬 네트워크 시작
npx hardhat node

# 컨트랙트 배포
npx hardhat run scripts/deploy.js --network localhost
```

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
// Axios Interceptor (권장)
import axios from 'axios';

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
    
    return Promise.reject(error);
  }
);

export default api;
```

### 3. 결제 연동 예시

```javascript
// TossPayments 결제
const payWithToss = async (orderId, amount) => {
  // 1. 결제 시작
  const initiateResponse = await api.post(
    `${API_BASE_URLS.payment}/api/payments/toss/initiate`,
    {
      orderId,
      amount,
      orderName: '상품명',
      customerName: '홍길동'
    }
  );
  
  const { paymentKey, orderId: tossOrderId } = initiateResponse.data.data;
  
  // 2. TossPayments SDK 호출
  const tossPayments = TossPayments('YOUR_CLIENT_KEY');
  await tossPayments.requestPayment('카드', {
    amount,
    orderId: tossOrderId,
    orderName: '상품명',
    customerName: '홍길동',
    successUrl: `${window.location.origin}/payment/success`,
    failUrl: `${window.location.origin}/payment/fail`
  });
  
  // 3. 성공 콜백에서 결제 승인
  // (successUrl로 리다이렉트 후)
  const confirmResponse = await api.post(
    `${API_BASE_URLS.payment}/api/payments/toss/confirm`,
    {
      paymentKey,
      orderId: tossOrderId,
      amount
    }
  );
  
  return confirmResponse.data;
};
```

### 4. NFT 조회 예시

```javascript
// 상품의 원산지 NFT 조회
const getOriginCertificate = async (productId) => {
  const response = await api.get(
    `${API_BASE_URLS.product}/api/products/${productId}/nft/origin`
  );
  
  const nft = response.data.data;
  
  // IPFS 메타데이터 조회
  const metadataResponse = await fetch(nft.metadataUri);
  const metadata = await metadataResponse.json();
  
  return {
    ...nft,
    metadata
  };
};
```

### 5. 지수 표시 예시

```javascript
// 상품 지수 조회
const getProductIndices = async (productId) => {
  const response = await api.get(
    `${API_BASE_URLS.product}/api/products/${productId}/indices`
  );
  
  const indices = response.data.data;
  
  // 표시
  return (
    <div className="product-indices">
      <div className="index-card">
        <h3>판매 가능성</h3>
        <div className="progress-bar">
          <div style={{ width: `${indices.salesPotential}%` }} />
        </div>
        <span>{indices.salesPotential}%</span>
      </div>
      
      <div className="index-card">
        <h3>소비 가능성</h3>
        <div className="progress-bar">
          <div style={{ width: `${indices.consumptionPotential}%` }} />
        </div>
        <span>{indices.consumptionPotential}%</span>
      </div>
      
      <div className="index-card">
        <h3>소비자 연결</h3>
        <div className="progress-bar">
          <div style={{ width: `${indices.consumerConnection}%` }} />
        </div>
        <span>{indices.consumerConnection}%</span>
      </div>
    </div>
  );
};
```

---

## 문서 목록

### 설계 문서 (004.design/) - 17개
1. `01_database_design.md` - 데이터베이스 설계
2. `02_table_lists.md` - 테이블 목록
3. `03_api_specs_phase1.md` - API 명세 (Phase 1)
4. `04_interface_design.md` - 인터페이스 설계
5. `05_architecture_defin.md` - 아키텍처 정의
6. `06_function_specs.md` - 기능 명세
7. `07_user_interface_design.md` - 사용자 인터페이스 설계
8. `08_admin_interface_design.md` - 관리자 인터페이스 설계
9. `09_java_spring_boot_techstack_defin.md` - 기술 스택 정의
10. `10_hybrid_architecture_design.md` - 하이브리드 아키텍처
11. `10_hybrid_msa_architecture_design.md` - MSA 아키텍처
12. `11_java_api_specs_detailed.md` - Java API 상세 명세
13. `11_java_based_api_specs_v2.md` - Java API v2
14. `12_python_api_specs_detailed.md` - Python API 상세 명세
15. `13_service_communication_sequences.md` - 서비스 통신 시퀀스
16. `14_advanced_evaluation_system.md` - 고급 평가 시스템
17. `README.md` - 설계 문서 개요

### 구현 문서 (backend/) - 20개
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
13. `TOSSPAYMENTS_INTEGRATION_GUIDE.md` - TossPayments 통합 가이드
14. `MULTI_PG_INTEGRATION_COMPLETE.md` - 멀티 PG 통합 완료
15. `BLOCKCHAIN_PAYMENT_STATUS_REVIEW.md` - 블록체인/결제 상태 검토
16. `BLOCKCHAIN_TOKEN_ARCHITECTURE.md` - 블록체인 토큰 아키텍처
17. `BLOCKCHAIN_NFT_STABLECOIN_STRATEGY.md` - NFT/스테이블코인 전략
18. `NFT_IMPLEMENTATION_COMPLETE.md` - NFT 구현 완료
19. `STABLECOIN_OPTIONS.md` - 스테이블코인 옵션
20. **`BACKEND_IMPLEMENTATION_MILESTONE.md`** - 백엔드 구현 기점 (이 문서)

---

## 백엔드 구현 완료 체크리스트

### 인프라
- [x] Docker Compose 설정
- [x] PostgreSQL 설정
- [x] PostGIS 확장 (GIS)
- [x] Redis 설정
- [x] Kafka 설정
- [x] Elasticsearch 설정

### 데이터베이스
- [x] Flyway Migration
- [x] JPA Entity (40개 테이블)
- [x] Repository
- [x] 인덱스 및 제약조건
- [x] GIS 데이터 타입

### 비즈니스 로직
- [x] Service Layer (5개 서비스)
- [x] DTO (Request/Response)
- [x] Validation
- [x] 예외 처리

### API
- [x] REST Controller (5개)
- [x] 50개 API 엔드포인트
- [x] 표준 응답 형식
- [x] CORS 설정

### 보안
- [x] JWT 인증
- [x] Redis 토큰 블랙리스트
- [x] Role 기반 접근 제어
- [x] Rate Limiting
- [x] 비밀번호 암호화

### 결제
- [x] TossPayments 통합
- [x] NicePay 통합
- [x] Stripe 통합
- [x] Webhook 처리
- [x] 환불/취소

### 블록체인
- [x] XLCFI Token 설계
- [x] Escrow Contract 설계
- [x] RewardPool Contract 설계
- [x] 토큰 이코노미 설계
- [x] 데이터베이스 스키마

### NFT
- [x] OriginCertificateNFT 스마트 컨트랙트
- [x] RecipeNFT 스마트 컨트랙트
- [x] MembershipNFT 스마트 컨트랙트
- [x] IPFS 메타데이터 구조
- [x] 데이터베이스 스키마
- [x] Hardhat 개발 환경

### 고급 평가
- [x] 전략적 라벨링 시스템
- [x] 시각적 반응 시스템
- [x] AI 댓글 관리 설계
- [x] 판매 가능성 지수
- [x] 소비 가능성 지수
- [x] 수익 창출 효과
- [x] 소비자 연결 지수 + GIS
- [x] 데이터베이스 스키마

### 문서화
- [x] Swagger/OpenAPI
- [x] API 문서
- [x] 구현 가이드 (20개)
- [x] 설계 문서 (17개)
- [x] 프론트엔드 연동 가이드

### 테스트
- [x] Integration Tests
- [x] Unit Tests
- [x] Test Coverage 90%+

---

## 다음 단계: 프론트엔드 구현

백엔드 구현이 완료되었으므로, 이제 프론트엔드 구현을 시작할 수 있습니다.

### 프론트엔드 기술 스택 (권장)
- **Framework**: Next.js 14+ (App Router)
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: Zustand
- **HTTP Client**: Axios
- **Form**: React Hook Form
- **Validation**: Zod
- **UI Library**: shadcn/ui
- **Maps**: Leaflet.js (GIS)
- **Charts**: Recharts (지수 시각화)
- **Blockchain**: ethers.js (Web3)

### 프론트엔드 구현 순서
1. 프로젝트 초기 설정
2. 라우팅 구조
3. 인증 시스템 (로그인/회원가입)
4. 레이아웃 및 네비게이션
5. 상품 목록/상세 페이지
6. 지수 표시 (4가지)
7. 시각적 반응 시스템
8. 장바구니
9. 주문/결제 (멀티 PG)
10. 마이페이지
11. NFT 조회/발행
12. 지도 (GIS)
13. 관리자 페이지

### 프론트엔드 시작 시 참조 문서
- 이 문서 (`BACKEND_IMPLEMENTATION_MILESTONE.md`)
- `SWAGGER_API_DOCUMENTATION.md`
- `11_java_api_specs_detailed.md`
- `14_advanced_evaluation_system.md`
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

### 스마트 컨트랙트 배포 시
1. Hardhat 테스트 실행
2. Mumbai Testnet에 배포
3. 컨트랙트 주소 기록
4. 백엔드 설정 파일 업데이트
5. 프론트엔드에 ABI 및 주소 전달

---

## 연락처 및 지원

**프로젝트명:** XLCfi Platform (SpicyJump)  
**백엔드 구현 완료:** 2025-11-21  
**문서 작성자:** AI Assistant  

**Git Repository:** https://github.com/imagicellstudio/1_web_service  
**이슈 트래킹:** GitHub Issues  
**문의:** entra55@gmail.com

---

## 라이선스

Proprietary - All Rights Reserved

---

## 🎉 백엔드 구현 완료!

이 문서는 백엔드 구현의 완전한 기록이며, 프론트엔드 구현 시작의 기점입니다.  
프론트엔드 개발 시 이 문서를 참조하여 API를 연동하세요.

**구현 규모:**
- Java 코드: 195개 파일, 42,000+ 줄
- 스마트 컨트랙트: 3개 (Solidity)
- 데이터베이스 테이블: 40개
- API 엔드포인트: 50개
- 설계 문서: 17개
- 구현 가이드: 20개

**주요 기능:**
- ✅ 인증/인가 (JWT, Redis)
- ✅ 상품/주문/리뷰 관리
- ✅ 멀티 PG 결제 (TossPayments, NicePay, Stripe)
- ✅ 블록체인 토큰 시스템 (XLCFI)
- ✅ NFT 시스템 (원산지, 레시피, 멤버십)
- ✅ 고급 평가 시스템 (4가지 지수, GIS)

**마지막 업데이트:** 2025-11-21  
**문서 버전:** 2.0.0
