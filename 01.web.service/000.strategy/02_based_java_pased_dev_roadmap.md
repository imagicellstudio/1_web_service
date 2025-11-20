# Java Spring Boot 기반 단계별 구현 로드맵

**문서 버전**: v2.0 (Java 전환)  
**작성일**: 2025-11-20  
**프로젝트**: 식품 거래 플랫폼 (XLCfi)

---

## 1. 개요

플랫폼 구축 전환에 따라 Java Spring Boot 중심의 하이브리드 아키텍처로 단계별 구현 계획을 수립합니다.

### 1.1 전략 원칙

- **Phase 1**: 핵심 거래 플랫폼 (Java Spring Boot 단독)
- **Phase 2**: 지능형 기능 확장 (Python 서비스 추가)
- **Phase 3**: 글로벌 플랫폼 (블록체인 + AI 고도화)

### 1.2 기술 스택 요약

```yaml
Phase 1 (MVP):
  Frontend: React.js (Next.js)
  Backend: Java Spring Boot 3.2
  Database: PostgreSQL 15 + Redis 7
  Infra: AWS (EC2, RDS, S3, CloudFront)

Phase 2 (확장):
  추가: Python FastAPI (AI/분석)
  추가: Elasticsearch (검색)
  추가: Kafka (이벤트 스트리밍)

Phase 3 (고도화):
  추가: Blockchain (Web3j + Ethereum)
  추가: Kubernetes (오케스트레이션)
  추가: Multi-Region (글로벌 확장)
```

---

## 2. Phase 1: MVP 구축 (3-4개월)

### 2.1 목표

안정적인 핵심 거래 플랫폼 구축 및 초기 사용자 확보

### 2.2 주요 기능

```
✅ 필수 기능 (Must Have)
├── 회원 관리
│   ├── 회원가입/로그인 (이메일, 소셜)
│   ├── 프로필 관리
│   └── 권한 관리 (일반/판매자/관리자)
│
├── 상품 관리
│   ├── 상품 등록/수정/삭제
│   ├── 카테고리 관리
│   ├── 이미지 업로드 (S3)
│   └── 재고 관리
│
├── 주문/결제
│   ├── 장바구니
│   ├── 주문 생성
│   ├── PG 연동 (토스페이먼츠)
│   └── 주문 내역 조회
│
├── 평가/리뷰
│   ├── 상품 리뷰 작성
│   ├── 평점 관리
│   └── 이미지 첨부
│
└── 관리자
    ├── 대시보드 (통계)
    ├── 회원 관리
    ├── 상품 승인/관리
    └── 주문 관리

⭕ 선택 기능 (Nice to Have)
├── 다국어 지원 (한/영)
├── 알림 기능 (이메일)
└── 쿠폰/할인 (기본)
```

### 2.3 기술 아키텍처

```
┌─────────────────────────────────────────┐
│         Frontend (Next.js)              │
│  - SSR/SSG for SEO                      │
│  - React Query (상태 관리)              │
│  - Tailwind CSS (스타일링)              │
└─────────────────────────────────────────┘
                  ↓ HTTPS
┌─────────────────────────────────────────┐
│      API Gateway (Spring Cloud)         │
│  - JWT 인증                             │
│  - Rate Limiting                        │
│  - CORS 처리                            │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│     Monolithic Backend (Spring Boot)    │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │  Auth Module                       │ │
│  │  Member Module                     │ │
│  │  Product Module                    │ │
│  │  Order Module                      │ │
│  │  Payment Module                    │ │
│  │  Review Module                     │ │
│  │  Admin Module                      │ │
│  └────────────────────────────────────┘ │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  PostgreSQL (RDS)      Redis (Cache)    │
│  - 트랜잭션 데이터     - 세션 저장      │
│  - 회원/상품/주문      - API 캐시       │
└─────────────────────────────────────────┘
```

### 2.4 프로젝트 구조 (Modular Monolith)

```
xlcfi-platform/
├── src/main/java/com/xlcfi/
│   ├── XlcfiApplication.java
│   │
│   ├── auth/                    # 인증/인가 모듈
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   └── JwtTokenProvider.java
│   │   ├── dto/
│   │   │   ├── LoginRequest.java
│   │   │   └── TokenResponse.java
│   │   └── config/
│   │       └── SecurityConfig.java
│   │
│   ├── member/                  # 회원 모듈
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   │   └── Member.java
│   │   └── dto/
│   │
│   ├── product/                 # 상품 모듈
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   │   ├── Product.java
│   │   │   └── Category.java
│   │   └── dto/
│   │
│   ├── order/                   # 주문 모듈
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   │   ├── Order.java
│   │   │   └── OrderItem.java
│   │   └── dto/
│   │
│   ├── payment/                 # 결제 모듈
│   │   ├── controller/
│   │   ├── service/
│   │   │   ├── PaymentService.java
│   │   │   └── TossPaymentClient.java
│   │   ├── domain/
│   │   │   └── Payment.java
│   │   └── dto/
│   │
│   ├── review/                  # 리뷰 모듈
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── domain/
│   │   │   └── Review.java
│   │   └── dto/
│   │
│   ├── admin/                   # 관리자 모듈
│   │   ├── controller/
│   │   ├── service/
│   │   └── dto/
│   │
│   └── common/                  # 공통 모듈
│       ├── config/
│       │   ├── JpaConfig.java
│       │   ├── RedisConfig.java
│       │   └── S3Config.java
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java
│       │   └── BusinessException.java
│       ├── dto/
│       │   ├── ApiResponse.java
│       │   └── PageResponse.java
│       └── util/
│           └── FileUploadUtil.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       ├── V1__init_schema.sql
│       ├── V2__add_indexes.sql
│       └── V3__add_review_table.sql
│
└── src/test/java/
    ├── auth/
    ├── member/
    ├── product/
    └── integration/
```

### 2.5 데이터베이스 스키마 (핵심 테이블)

```sql
-- 회원
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL, -- USER, SELLER, ADMIN
    status VARCHAR(20) NOT NULL, -- ACTIVE, INACTIVE, SUSPENDED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 상품
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    seller_id BIGINT NOT NULL REFERENCES members(id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL, -- ACTIVE, INACTIVE, SOLD_OUT
    origin_country VARCHAR(100), -- 원산지
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 주문
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
    shipping_address TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 주문 상품
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 결제
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    payment_key VARCHAR(255) UNIQUE NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    method VARCHAR(50) NOT NULL, -- CARD, TRANSFER, VIRTUAL_ACCOUNT
    status VARCHAR(20) NOT NULL, -- PENDING, COMPLETED, FAILED, CANCELLED
    pg_provider VARCHAR(50) NOT NULL, -- TOSS, NICE
    approved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 리뷰
CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    member_id BIGINT NOT NULL REFERENCES members(id),
    order_id BIGINT NOT NULL REFERENCES orders(id),
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    status VARCHAR(20) NOT NULL, -- ACTIVE, DELETED, REPORTED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_products_seller ON products(seller_id);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_orders_member ON orders(member_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_reviews_member ON reviews(member_id);
```

### 2.6 주요 API 엔드포인트

```yaml
인증 API:
  POST   /api/v1/auth/register        # 회원가입
  POST   /api/v1/auth/login           # 로그인
  POST   /api/v1/auth/refresh         # 토큰 갱신
  POST   /api/v1/auth/logout          # 로그아웃

회원 API:
  GET    /api/v1/members/me           # 내 정보 조회
  PUT    /api/v1/members/me           # 내 정보 수정
  GET    /api/v1/members/{id}         # 회원 조회 (공개 정보)

상품 API:
  GET    /api/v1/products             # 상품 목록 (페이징, 필터)
  GET    /api/v1/products/{id}        # 상품 상세
  POST   /api/v1/products             # 상품 등록 (판매자)
  PUT    /api/v1/products/{id}        # 상품 수정
  DELETE /api/v1/products/{id}        # 상품 삭제

주문 API:
  POST   /api/v1/orders               # 주문 생성
  GET    /api/v1/orders               # 내 주문 목록
  GET    /api/v1/orders/{id}          # 주문 상세
  PUT    /api/v1/orders/{id}/cancel   # 주문 취소

결제 API:
  POST   /api/v1/payments             # 결제 요청
  GET    /api/v1/payments/{id}        # 결제 조회
  POST   /api/v1/payments/webhook     # PG 웹훅

리뷰 API:
  POST   /api/v1/reviews              # 리뷰 작성
  GET    /api/v1/reviews              # 리뷰 목록
  PUT    /api/v1/reviews/{id}         # 리뷰 수정
  DELETE /api/v1/reviews/{id}         # 리뷰 삭제

관리자 API:
  GET    /api/v1/admin/dashboard      # 대시보드 통계
  GET    /api/v1/admin/members        # 회원 관리
  GET    /api/v1/admin/products       # 상품 관리
  GET    /api/v1/admin/orders         # 주문 관리
```

### 2.7 개발 일정 (3-4개월)

```
Week 1-2: 프로젝트 셋업 및 인프라 구축
├── Spring Boot 프로젝트 생성
├── AWS 인프라 구축 (RDS, S3, EC2)
├── CI/CD 파이프라인 구축
└── 개발 환경 설정

Week 3-4: 인증/회원 모듈
├── Spring Security 설정
├── JWT 인증 구현
├── 회원가입/로그인 API
├── 소셜 로그인 (Google, Kakao)
└── 테스트 코드 작성

Week 5-7: 상품 모듈
├── 상품 CRUD API
├── 카테고리 관리
├── 이미지 업로드 (S3)
├── 재고 관리
└── 검색 기능 (PostgreSQL Full-Text)

Week 8-9: 주문/결제 모듈
├── 장바구니 기능
├── 주문 생성 API
├── 토스페이먼츠 연동
├── 결제 웹훅 처리
└── 트랜잭션 관리

Week 10-11: 리뷰 모듈 및 관리자
├── 리뷰 CRUD API
├── 평점 계산
├── 관리자 대시보드
└── 통계 API

Week 12-13: Frontend 개발
├── Next.js 프로젝트 셋업
├── 주요 페이지 개발
│   ├── 홈페이지
│   ├── 상품 목록/상세
│   ├── 장바구니
│   ├── 주문/결제
│   └── 마이페이지
└── 반응형 디자인

Week 14-15: 통합 테스트 및 배포
├── E2E 테스트
├── 성능 테스트
├── 보안 점검
└── 운영 배포

Week 16: 모니터링 및 안정화
├── 모니터링 대시보드 구축
├── 알림 설정
├── 버그 수정
└── 문서화
```

### 2.8 성공 지표 (KPI)

```
기술 지표:
- API 응답 시간: 평균 < 200ms
- 가용성: 99.5% 이상
- 동시 접속: 1,000명 처리 가능
- 테스트 커버리지: 80% 이상

비즈니스 지표:
- 초기 회원 가입: 500명
- 일일 활성 사용자: 100명
- 월 거래액: $10,000
- 상품 등록: 1,000개
```

---

## 3. Phase 2: 지능형 기능 확장 (2-3개월)

### 3.1 목표

AI 기반 지능형 정보 지원 시스템 구축 및 사용자 경험 향상

### 3.2 추가 기능

```
✅ AI/분석 기능
├── 상품 추천 시스템
│   ├── 협업 필터링
│   ├── 콘텐츠 기반 필터링
│   └── 개인화 추천
│
├── 시장 가격 분석
│   ├── 시계열 예측
│   ├── 지역별 가격 비교
│   └── 적정 가격 추천
│
├── 판매처 매칭
│   ├── 학교 급식 연계
│   ├── 레스토랑 매칭
│   └── 도매상 추천
│
└── 이미지 분석
    ├── 식품 자동 분류
    ├── 품질 판정
    └── 원산지 검증 (OCR)

✅ 고급 검색
├── Elasticsearch 통합
├── 전문 검색 (Full-Text)
├── 필터링 (가격, 지역, 카테고리)
└── 자동완성

✅ 알림 시스템
├── 실시간 알림 (WebSocket)
├── 이메일 알림
├── 푸시 알림 (모바일)
└── SMS 알림 (선택)
```

### 3.3 기술 아키텍처 (확장)

```
┌─────────────────────────────────────────┐
│         Frontend (Next.js)              │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│      API Gateway (Spring Cloud)         │
└─────────────────────────────────────────┘
         ↓                        ↓
┌──────────────────┐    ┌──────────────────┐
│  Core Backend    │    │  Python Services │
│  (Spring Boot)   │    │    (FastAPI)     │
│                  │    │                  │
│  기존 모듈들     │◄───┤  AI Analysis     │
│                  │    │  Market Insight  │
│                  │    │  Image Process   │
└──────────────────┘    └──────────────────┘
         ↓                        ↓
┌─────────────────────────────────────────┐
│  PostgreSQL   Redis   Elasticsearch     │
│  MongoDB (로그)   Kafka (이벤트)        │
└─────────────────────────────────────────┘
```

### 3.4 마이크로서비스 분리 시작

```
기존 Monolith를 점진적으로 분리:

1단계: 읽기 전용 서비스 분리
   └── Product Query Service (Elasticsearch)

2단계: 독립적 도메인 분리
   ├── Review Service (별도 DB)
   └── Notification Service (별도 DB)

3단계: 핵심 도메인 분리 (Phase 3에서)
   ├── Order Service
   ├── Payment Service
   └── Member Service
```

### 3.5 Python AI 서비스 구조

```
xlcfi-ai-services/
├── ai-analysis/              # AI 분석 서비스
│   ├── main.py
│   ├── models/
│   │   ├── recommendation.py
│   │   └── price_forecast.py
│   ├── services/
│   │   └── java_client.py
│   └── requirements.txt
│
├── market-insight/           # 시장 인사이트
│   ├── main.py
│   ├── services/
│   │   ├── price_analysis.py
│   │   └── market_matching.py
│   └── requirements.txt
│
└── image-processing/         # 이미지 처리
    ├── main.py
    ├── models/
    │   ├── food_classifier.py
    │   └── quality_detector.py
    └── requirements.txt
```

### 3.6 개발 일정 (2-3개월)

```
Week 1-2: 인프라 확장
├── Elasticsearch 구축
├── Kafka 구축
├── Python 서비스 환경 구축
└── 모니터링 강화

Week 3-5: AI 추천 시스템
├── 데이터 수집 파이프라인
├── 협업 필터링 모델 개발
├── 추천 API 개발
└── Java 서비스 연동

Week 6-7: 시장 가격 분석
├── 가격 데이터 수집
├── 시계열 예측 모델
├── 가격 분석 API
└── 대시보드 통합

Week 8-9: 검색 고도화
├── Elasticsearch 인덱싱
├── 검색 API 개발
├── 자동완성 기능
└── 필터링 최적화

Week 10-11: 알림 시스템
├── WebSocket 서버 구축
├── 이메일 발송 시스템
├── 알림 관리 API
└── Frontend 통합

Week 12: 테스트 및 배포
├── 통합 테스트
├── 성능 최적화
└── 운영 배포
```

### 3.7 성공 지표 (KPI)

```
기술 지표:
- 추천 정확도: 70% 이상
- 검색 응답 시간: < 100ms
- 가격 예측 오차: ±10% 이내
- 이벤트 처리 지연: < 1초

비즈니스 지표:
- 추천 클릭률: 15% 이상
- 검색 사용률: 60% 이상
- 판매자 매칭 성공률: 30%
- 사용자 재방문율: 40% 증가
```

---

## 4. Phase 3: 글로벌 플랫폼 (3-4개월)

### 4.1 목표

블록체인 기반 투명한 거래 시스템 및 글로벌 확장

### 4.2 추가 기능

```
✅ 블록체인 통합
├── 거래 이력 기록
├── 원산지 추적
├── 리워드 토큰 시스템
└── 스마트 컨트랙트

✅ 글로벌 확장
├── 다국어 지원 (10개 언어)
├── 다통화 지원
├── 지역별 배송 연동
└── 현지화 (Localization)

✅ 고급 관리 기능
├── 정산 시스템
├── 세금 계산
├── 리포트 생성
└── 감사 로그

✅ 모바일 앱
├── React Native 앱
├── 푸시 알림
├── 오프라인 모드
└── 앱 스토어 출시
```

### 4.3 최종 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│              Multi-Region CDN (CloudFront)              │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│         API Gateway (Spring Cloud Gateway)              │
│              + Service Mesh (Istio)                     │
└─────────────────────────────────────────────────────────┘
         ↓                                    ↓
┌──────────────────────┐          ┌──────────────────────┐
│  Java Microservices  │          │  Python Services     │
│  (Kubernetes)        │          │  (Kubernetes)        │
│                      │          │                      │
│  • Auth Service      │          │  • AI Analysis       │
│  • Member Service    │          │  • Market Insight    │
│  • Product Service   │          │  • Image Processing  │
│  • Order Service     │◄────────►│  • Recommendation    │
│  • Payment Service   │          │  • Data ETL          │
│  • Blockchain Svc    │          └──────────────────────┘
│  • Review Service    │
│  • Admin Service     │
└──────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                            │
│  PostgreSQL (Primary/Replica)   Redis Cluster           │
│  Elasticsearch Cluster          MongoDB Cluster         │
│  Kafka Cluster                  S3 (Multi-Region)       │
└─────────────────────────────────────────────────────────┘
         ↓
┌─────────────────────────────────────────────────────────┐
│              External Services                           │
│  Blockchain (Ethereum/Polygon)   Payment PG (Global)    │
│  Public APIs (MFDS, HACCP)       Email/SMS Provider     │
└─────────────────────────────────────────────────────────┘
```

### 4.4 블록체인 통합

```java
// Blockchain Service
@Service
@RequiredArgsConstructor
public class BlockchainService {
    
    private final Web3j web3j;
    private final Credentials credentials;
    
    @Value("${blockchain.contract.address}")
    private String contractAddress;
    
    // 거래 이력 기록
    public String recordTransaction(TransactionRecord record) {
        TraceabilityContract contract = TraceabilityContract.load(
            contractAddress, 
            web3j, 
            credentials, 
            new DefaultGasProvider()
        );
        
        TransactionReceipt receipt = contract.recordTransaction(
            record.getOrderId(),
            record.getProductId(),
            record.getSellerId(),
            record.getBuyerId(),
            record.getAmount(),
            record.getTimestamp()
        ).send();
        
        return receipt.getTransactionHash();
    }
    
    // 원산지 추적
    public OriginTrace getOriginTrace(Long productId) {
        TraceabilityContract contract = TraceabilityContract.load(
            contractAddress, 
            web3j, 
            credentials, 
            new DefaultGasProvider()
        );
        
        Tuple7<String, String, String, BigInteger, String, String, Boolean> result = 
            contract.getOriginTrace(BigInteger.valueOf(productId)).send();
        
        return OriginTrace.builder()
            .productId(productId)
            .origin(result.component1())
            .producer(result.component2())
            .certifications(result.component3())
            .productionDate(result.component4())
            .verified(result.component7())
            .build();
    }
    
    // 리워드 토큰 지급
    public void issueRewardToken(Long memberId, BigInteger amount) {
        RewardTokenContract contract = RewardTokenContract.load(
            contractAddress, 
            web3j, 
            credentials, 
            new DefaultGasProvider()
        );
        
        contract.mint(
            getMemberWalletAddress(memberId), 
            amount
        ).send();
    }
}
```

### 4.5 Kubernetes 배포

```yaml
# Deployment for Product Service
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
  namespace: xlcfi
spec:
  replicas: 5
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
        version: v1
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
              name: db-credentials
              key: url
        resources:
          requests:
            memory: "1Gi"
            cpu: "1000m"
          limits:
            memory: "2Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - product-service
              topologyKey: kubernetes.io/hostname

---
# HorizontalPodAutoscaler
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: product-service-hpa
  namespace: xlcfi
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: product-service
  minReplicas: 3
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 4.6 개발 일정 (3-4개월)

```
Week 1-3: 블록체인 통합
├── 스마트 컨트랙트 개발
├── Web3j 통합
├── 거래 이력 기록
└── 원산지 추적 기능

Week 4-6: 마이크로서비스 완전 분리
├── 서비스 분리 완료
├── Kubernetes 구축
├── Service Mesh 구축
└── 배포 자동화

Week 7-9: 글로벌 확장
├── 다국어 지원 (i18n)
├── 다통화 지원
├── 지역별 서버 구축
└── CDN 최적화

Week 10-12: 모바일 앱
├── React Native 개발
├── 푸시 알림 구현
├── 앱 스토어 출시
└── 앱-백엔드 연동

Week 13-15: 고급 관리 기능
├── 정산 시스템
├── 세금 계산
├── 리포트 생성
└── 감사 로그

Week 16: 최종 테스트 및 런칭
├── 부하 테스트
├── 보안 감사
├── 문서화
└── 공식 런칭
```

### 4.7 성공 지표 (KPI)

```
기술 지표:
- 가용성: 99.9% 이상
- 동시 접속: 10,000명 처리
- API 응답 시간: < 150ms (글로벌)
- 블록체인 트랜잭션: < 5분 확정

비즈니스 지표:
- 글로벌 사용자: 10개국 이상
- 월 거래액: $1,000,000
- 블록체인 거래: 1,000건/월
- 모바일 앱 다운로드: 10,000+
```

---

## 5. 인프라 및 비용

### 5.1 Phase별 인프라 비용

```yaml
Phase 1 (MVP):
  AWS EC2: 
    - t3.medium x 3 (Backend): $100/월
    - t3.small x 2 (Frontend): $40/월
  AWS RDS:
    - db.t3.medium (PostgreSQL): $80/월
  AWS ElastiCache:
    - cache.t3.micro (Redis): $15/월
  AWS S3 + CloudFront: $50/월
  기타 (로드밸런서, 도메인): $50/월
  
  총 예상 비용: $335/월 (~$4,000/년)

Phase 2 (확장):
  기존 인프라 +
  AWS EC2 (Python): $100/월
  Elasticsearch: $150/월
  Kafka (MSK): $200/월
  MongoDB Atlas: $100/월
  
  총 예상 비용: $885/월 (~$10,600/년)

Phase 3 (글로벌):
  Kubernetes (EKS): $300/월
  Multi-Region 복제: $500/월
  Blockchain 노드: $200/월
  증가된 트래픽 비용: $500/월
  
  총 예상 비용: $2,385/월 (~$28,600/년)
```

### 5.2 팀 구성

```yaml
Phase 1:
  - Backend 개발자: 2명 (Java Spring Boot)
  - Frontend 개발자: 1명 (React/Next.js)
  - DevOps: 0.5명 (파트타임)

Phase 2:
  - Backend 개발자: 2명
  - Python 개발자: 1명 (AI/ML)
  - Frontend 개발자: 1명
  - DevOps: 1명

Phase 3:
  - Backend 개발자: 3명
  - Python 개발자: 2명
  - Frontend 개발자: 2명
  - 모바일 개발자: 1명
  - DevOps: 1명
  - QA: 1명
```

---

## 6. 리스크 및 대응

### 6.1 기술 리스크

```
리스크 1: 마이크로서비스 복잡도 증가
대응: Phase 1은 Modular Monolith로 시작, 점진적 분리

리스크 2: 블록체인 가스비 부담
대응: Polygon 등 저비용 체인 사용, 배치 처리

리스크 3: AI 모델 정확도 부족
대응: Phase 2에서 충분한 데이터 수집 후 진행

리스크 4: 성능 병목
대응: 캐싱 전략, DB 인덱싱, 비동기 처리
```

### 6.2 비즈니스 리스크

```
리스크 1: 초기 사용자 확보 어려움
대응: MVP 빠른 출시, 마케팅 집중

리스크 2: 경쟁사 대비 차별화 부족
대응: 블록체인 + AI 조합으로 차별화

리스크 3: 규제 변화
대응: 법률 자문, 유연한 아키텍처

리스크 4: 자금 부족
대응: Phase별 투자 유치, MVP로 PMF 검증
```

---

## 7. 다음 단계

1. **Phase 1 상세 설계 문서** 작성
   - API 명세서 업데이트 (Java 기반)
   - 데이터베이스 스키마 상세화
   - Entity 클래스 설계

2. **개발 환경 구축**
   - Spring Boot 프로젝트 생성
   - AWS 인프라 구축
   - CI/CD 파이프라인

3. **프로토타입 개발**
   - 인증/회원 모듈 우선 구현
   - Frontend 메인 화면 (Figma → 실제 구현)

---

**문서 작성**: AI Assistant  
**검토 필요**: 프로젝트 매니저, 기술 리드, CTO

