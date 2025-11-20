# XLCfi Platform - Database Schema Implementation Summary

## 개요

이 문서는 XLCfi 플랫폼의 데이터베이스 스키마 구현 내역을 요약합니다.

**작업 완료 날짜:** 2025-11-20

## 구현 완료 항목

### 1. Flyway 마이그레이션 스크립트

모든 핵심 테이블에 대한 Flyway 마이그레이션 스크립트가 생성되었습니다.

| 서비스 | 마이그레이션 파일 | 테이블 |
|--------|-------------------|--------|
| auth-service | V1__init_users_schema.sql | users |
| product-service | V1__init_product_schema.sql | categories, products |
| product-service | V2__init_category_data.sql | 카테고리 초기 데이터 |
| order-service | V1__init_order_schema.sql | orders, order_items |
| payment-service | V1__init_payment_schema.sql | payments |
| review-service | V1__init_review_schema.sql | reviews |

**위치:** `backend/java-services/xlcfi-{service}/src/main/resources/db/migration/`

### 2. JPA Entity 클래스

모든 핵심 도메인에 대한 JPA Entity 클래스가 생성되었습니다.

#### 공통 모듈 (common-data)
- **BaseEntity.java**: 공통 필드 (id, createdAt, updatedAt)를 포함하는 추상 클래스

#### Auth Service
- **User.java**: 사용자 엔티티
- **UserRole.java**: 사용자 역할 Enum (BUYER, SELLER, ADMIN)
- **UserStatus.java**: 사용자 상태 Enum (ACTIVE, INACTIVE, SUSPENDED)
- **Language.java**: 언어 Enum (KO, EN)

#### Product Service
- **Category.java**: 카테고리 엔티티 (자기 참조 관계)
- **Product.java**: 상품 엔티티
- **ProductStatus.java**: 상품 상태 Enum (DRAFT, PUBLISHED, SOLDOUT, DISCONTINUED)

#### Order Service
- **Order.java**: 주문 엔티티
- **OrderItem.java**: 주문 항목 엔티티
- **OrderStatus.java**: 주문 상태 Enum (PENDING, PAID, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)

#### Payment Service
- **Payment.java**: 결제 엔티티
- **PaymentMethod.java**: 결제 수단 Enum (CARD, BANK_TRANSFER, PAYPAL, STRIPE)
- **PaymentStatus.java**: 결제 상태 Enum (PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED)

#### Review Service
- **Review.java**: 리뷰 엔티티
- **ReviewStatus.java**: 리뷰 상태 Enum (PUBLISHED, HIDDEN, DELETED)

### 3. Repository 인터페이스

모든 Entity에 대한 Spring Data JPA Repository 인터페이스가 생성되었습니다.

#### Auth Service
- **UserRepository**: 사용자 데이터 접근
  - `findByEmail()`: 이메일로 조회
  - `existsByEmail()`: 이메일 중복 확인
  - `findByEmailAndStatus()`: 이메일 + 상태로 조회

#### Product Service
- **CategoryRepository**: 카테고리 데이터 접근
  - `findByParentIsNull()`: 최상위 카테고리 조회
  - `findByParentIdOrderBySortOrder()`: 자식 카테고리 조회
  - `findByNameContaining()`: 이름 검색
  
- **ProductRepository**: 상품 데이터 접근
  - `findByStatus()`: 상태별 조회
  - `findByCategoryIdAndStatus()`: 카테고리별 조회
  - `searchByKeyword()`: 키워드 검색
  - `incrementViewCount()`: 조회수 증가
  - `findByStatusOrderByViewCountDesc()`: 인기 상품
  - `findByStatusOrderByRatingAverageDesc()`: 평점 높은 상품

#### Order Service
- **OrderRepository**: 주문 데이터 접근
  - `findByOrderNumber()`: 주문번호로 조회
  - `findByBuyerId()`: 구매자별 조회
  - `findBySellerId()`: 판매자별 조회
  - `findByDateRange()`: 기간별 조회
  - `getSellerOrderStats()`: 판매자 주문 통계
  
- **OrderItemRepository**: 주문 항목 데이터 접근
  - `findByOrderId()`: 주문별 항목 조회
  - `getTotalSoldQuantity()`: 총 판매량 조회
  - `findTopSellingProducts()`: 인기 상품 조회

#### Payment Service
- **PaymentRepository**: 결제 데이터 접근
  - `findByOrderId()`: 주문별 결제 조회
  - `findByPgTransactionId()`: PG사 거래 ID로 조회
  - `findByStatus()`: 상태별 조회
  - `getPaymentStats()`: 결제 통계

#### Review Service
- **ReviewRepository**: 리뷰 데이터 접근
  - `findByProductIdAndStatus()`: 상품별 리뷰 조회
  - `findByUserIdAndStatus()`: 사용자별 리뷰 조회
  - `getAverageRating()`: 평균 평점 계산
  - `countByProductIdAndStatus()`: 리뷰 개수
  - `findByProductIdAndIsVerifiedPurchaseAndStatus()`: 인증 구매 리뷰

### 4. 데이터베이스 초기화 스크립트

개발 환경 설정을 위한 다양한 스크립트가 생성되었습니다.

- **init-database.sql**: 데이터베이스 및 사용자 생성
- **seed-data.sql**: 테스트 데이터 삽입
  - 테스트 사용자 6명 (Admin, Seller x2, Buyer x3)
  - 샘플 상품 5개 (고추장, 김치, 된장, 고춧가루, 잡채)
  - 샘플 리뷰 5개
- **reset-database.sh**: Linux/Mac용 데이터베이스 초기화
- **reset-database.bat**: Windows용 데이터베이스 초기화
- **scripts/README.md**: 스크립트 사용 가이드

**위치:** `backend/scripts/`

### 5. 설정 파일

각 서비스별 Spring Boot 설정 파일이 생성되었습니다.

- **application.yml**: 기본 설정 (각 서비스)
- **application-dev.yml**: 개발 환경 설정 (각 서비스)
- **build.gradle.kts**: Gradle 빌드 설정 (각 서비스)

**주요 설정:**
- PostgreSQL 데이터베이스 연결
- JPA/Hibernate 설정 (ddl-auto: validate)
- Flyway 자동 마이그레이션 활성화
- 서비스별 포트 설정:
  - auth-service: 8081
  - product-service: 8082
  - order-service: 8083
  - payment-service: 8084
  - review-service: 8085

## 데이터베이스 구조

### ERD 개요

```
users (사용자)
  ├─> products (상품 - seller_id)
  │     ├─> order_items (주문항목 - product_id)
  │     └─> reviews (리뷰 - product_id)
  │
  ├─> orders (주문 - buyer_id, seller_id)
  │     ├─> order_items (주문항목 - order_id)
  │     └─> payments (결제 - order_id)
  │
  └─> reviews (리뷰 - user_id)

categories (카테고리)
  ├─> categories (자기참조 - parent_id)
  └─> products (상품 - category_id)
```

### 주요 관계

1. **User ↔ Product**: 1:N (판매자)
2. **User ↔ Order**: 1:N (구매자/판매자)
3. **User ↔ Review**: 1:N
4. **Category ↔ Category**: 1:N (자기참조)
5. **Category ↔ Product**: 1:N
6. **Product ↔ OrderItem**: 1:N
7. **Product ↔ Review**: 1:N
8. **Order ↔ OrderItem**: 1:N
9. **Order ↔ Payment**: 1:N

## 데이터베이스 기능

### 트리거 (Triggers)

1. **update_updated_at_column**: 모든 테이블의 updated_at 자동 갱신
2. **generate_order_number**: 주문번호 자동 생성 (ORD-YYYYMMDD-####)
3. **decrease_product_stock**: 주문 시 재고 자동 감소
4. **update_product_rating**: 리뷰 작성/수정 시 상품 평점 자동 업데이트

### 제약 조건 (Constraints)

- **UNIQUE**: 이메일, 주문번호, 리뷰 중복 방지
- **CHECK**: 가격/수량 양수, 평점 범위 (1-5), 상태 값 검증
- **FOREIGN KEY**: 참조 무결성 (CASCADE, RESTRICT)

### 인덱스 (Indexes)

각 테이블에 적절한 인덱스가 설정되어 있습니다:
- 단일 컬럼 인덱스 (status, created_at 등)
- 복합 인덱스 (category_id + status 등)
- 고유 인덱스 (email, order_number 등)

## 사용 방법

### 1. 데이터베이스 초기 설정

```bash
# 1. PostgreSQL 설치 확인
psql --version

# 2. 데이터베이스 생성
psql -U postgres -f backend/scripts/init-database.sql

# 3. Flyway 마이그레이션 실행
cd backend/java-services
./gradlew flywayMigrate

# 4. 테스트 데이터 삽입 (선택사항)
psql -U xlcfi_user -d xlcfi_db -f ../scripts/seed-data.sql
```

### 2. Docker Compose 사용

```bash
# 1. Docker Compose로 PostgreSQL 시작
cd backend
docker-compose up -d postgres

# 2. 서비스 시작 (Flyway 자동 실행)
docker-compose up -d
```

### 3. 데이터베이스 초기화 (개발 중)

```bash
# Linux/Mac
cd backend/scripts
./reset-database.sh

# Windows
cd backend\scripts
reset-database.bat
```

## 다음 단계

데이터베이스 스키마 구현이 완료되었습니다. 다음 작업으로 진행할 수 있습니다:

1. **Service Layer 구현**: 비즈니스 로직 작성
2. **Controller Layer 구현**: REST API 엔드포인트 작성
3. **Security 설정**: JWT 인증/인가 구현
4. **통합 테스트**: 데이터베이스 연동 테스트
5. **API 문서화**: Swagger/OpenAPI 설정

## 참고 문서

- [Database Design](../004.design/01_database_design.md): 상세 데이터베이스 설계
- [Java API Specs](../004.design/11_java_api_specs_detailed.md): Java API 명세
- [DB Setup Guide](./DB_SETUP_GUIDE.md): 데이터베이스 설정 가이드
- [Scripts README](./scripts/README.md): 스크립트 사용 가이드

## 테스트 계정

seed-data.sql을 실행한 경우 다음 계정을 사용할 수 있습니다:

| 이메일 | 비밀번호 | 역할 | 이름 |
|--------|----------|------|------|
| admin@xlcfi.com | password123 | ADMIN | Admin User |
| seller1@xlcfi.com | password123 | SELLER | 김판매 |
| seller2@xlcfi.com | password123 | SELLER | John Seller |
| buyer1@xlcfi.com | password123 | BUYER | 이구매 |
| buyer2@xlcfi.com | password123 | BUYER | Jane Buyer |

**주의**: 프로덕션 환경에서는 반드시 비밀번호를 변경하세요!

## 문제 해결

### Flyway 마이그레이션 오류

```bash
# 마이그레이션 상태 확인
./gradlew flywayInfo

# 마이그레이션 재시도
./gradlew flywayRepair
./gradlew flywayMigrate
```

### PostgreSQL 연결 오류

```bash
# PostgreSQL 상태 확인
sudo systemctl status postgresql  # Linux
brew services list  # Mac
sc query postgresql-x64-14  # Windows

# 포트 확인
netstat -an | grep 5432
```

### Entity 매핑 오류

- application.yml에서 `spring.jpa.show-sql: true`로 설정하여 SQL 로그 확인
- `spring.jpa.properties.hibernate.format_sql: true`로 SQL 포맷 확인

---

**작성자**: AI Assistant  
**최종 수정**: 2025-11-20

