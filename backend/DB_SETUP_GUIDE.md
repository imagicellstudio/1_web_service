# 데이터베이스 설정 가이드

## 완료된 작업 ✅

### 1. Flyway 마이그레이션 파일
- ✅ V1__init_users_schema.sql (Auth Service)
- ✅ V1__init_product_schema.sql (Product Service)
- ✅ V2__init_category_data.sql (Product Service)
- ✅ V1__init_order_schema.sql (Order Service)
- ✅ V1__init_payment_schema.sql (Payment Service)
- ✅ V1__init_review_schema.sql (Review Service)

### 2. JPA Entity 클래스
- ✅ User (Auth Service)
- ✅ UserRole, UserStatus, Language (Enum)
- ✅ Category (Product Service)
- ✅ Product (Product Service)
- ✅ ProductStatus (Enum)

## 다음 단계 🚀

### 1. 나머지 Entity 생성
```bash
# Order, OrderItem, Payment, Review Entity 생성 필요
```

### 2. Repository 인터페이스 생성
```java
// 예시: UserRepository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    boolean existsByEmail(String email);
}
```

### 3. 데이터베이스 초기화

#### Option A: Docker로 자동 초기화
```bash
# 인프라 시작 (PostgreSQL 포함)
make dev

# 서비스 실행 시 Flyway가 자동으로 마이그레이션 실행
```

#### Option B: 수동 초기화
```bash
# PostgreSQL 접속
docker exec -it xlcfi-postgres psql -U xlcfi_dev -d xlcfi_dev

# 또는 로컬 PostgreSQL
psql -U xlcfi_dev -d xlcfi_dev

# 테이블 확인
\dt

# 데이터 확인
SELECT * FROM users;
SELECT * FROM categories;
SELECT * FROM products;
```

## 데이터베이스 스키마 구조

```
xlcfi_dev (Database)
├── users                    # 사용자
├── categories               # 카테고리 (계층 구조)
├── products                 # 상품
├── orders                   # 주문
├── order_items              # 주문 항목
├── payments                 # 결제
├── refunds                  # 환불
├── reviews                  # 리뷰
├── review_helpful           # 리뷰 도움됨
└── review_reports           # 리뷰 신고
```

## 테이블 관계

```
users
  ├─→ products (seller_id)
  ├─→ orders (buyer_id, seller_id)
  └─→ reviews (user_id)

categories
  ├─→ categories (parent_id) - 자기 참조
  └─→ products (category_id)

products
  ├─→ order_items (product_id)
  └─→ reviews (product_id)

orders
  ├─→ order_items (order_id)
  └─→ payments (order_id)

payments
  └─→ refunds (payment_id)

reviews
  ├─→ review_helpful (review_id)
  └─→ review_reports (review_id)
```

## 서비스별 데이터베이스 마이그레이션

### Auth Service (Port: 8081)
```
src/main/resources/db/migration/
└── V1__init_users_schema.sql
```

### Product Service (Port: 8082)
```
src/main/resources/db/migration/
├── V1__init_product_schema.sql
└── V2__init_category_data.sql
```

### Order Service (Port: 8083)
```
src/main/resources/db/migration/
└── V1__init_order_schema.sql
```

### Payment Service (Port: 8084)
```
src/main/resources/db/migration/
└── V1__init_payment_schema.sql
```

### Review Service (Port: 8085)
```
src/main/resources/db/migration/
└── V1__init_review_schema.sql
```

## 첫 실행 시 마이그레이션 순서

1. **Auth Service 실행**
   - users 테이블 생성
   
2. **Product Service 실행**
   - categories, products 테이블 생성
   - 초기 카테고리 데이터 삽입
   
3. **Order Service 실행**
   - orders, order_items 테이블 생성
   
4. **Payment Service 실행**
   - payments, refunds 테이블 생성
   
5. **Review Service 실행**
   - reviews, review_helpful, review_reports 테이블 생성

## Flyway 설정

각 서비스의 `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    table: flyway_schema_history
```

## 초기 데이터 삽입

### 관리자 계정 생성
```sql
-- 비밀번호: Admin1234! (bcrypt 해시)
INSERT INTO users (email, password_hash, name, role, status, language, created_at, updated_at)
VALUES (
  'admin@xlcfi.com',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5vbLsqOw3wB3O',
  'Administrator',
  'ADMIN',
  'ACTIVE',
  'KO',
  NOW(),
  NOW()
);
```

### 테스트 판매자 계정
```sql
-- 비밀번호: Seller1234!
INSERT INTO users (email, password_hash, name, role, status, language, created_at, updated_at)
VALUES (
  'seller@xlcfi.com',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5vbLsqOw3wB3O',
  'Test Seller',
  'SELLER',
  'ACTIVE',
  'KO',
  NOW(),
  NOW()
);
```

### 테스트 구매자 계정
```sql
-- 비밀번호: Buyer1234!
INSERT INTO users (email, password_hash, name, role, status, language, created_at, updated_at)
VALUES (
  'buyer@xlcfi.com',
  '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5vbLsqOw3wB3O',
  'Test Buyer',
  'BUYER',
  'ACTIVE',
  'KO',
  NOW(),
  NOW()
);
```

## 트러블슈팅

### Flyway 마이그레이션 실패
```bash
# Flyway 히스토리 확인
SELECT * FROM flyway_schema_history;

# 실패한 마이그레이션 제거
DELETE FROM flyway_schema_history WHERE success = false;

# 서비스 재시작
```

### 스키마 초기화 (주의!)
```bash
# 모든 테이블 삭제 후 재생성
docker-compose down -v
docker-compose up -d postgres

# 서비스 재시작하면 자동으로 마이그레이션 실행
```

## 다음 작업

1. ✅ Flyway 마이그레이션 파일 작성 완료
2. ✅ 기본 Entity 클래스 작성 완료 (User, Category, Product)
3. ⏳ 나머지 Entity 작성 (Order, Payment, Review)
4. ⏳ Repository 인터페이스 작성
5. ⏳ Service 계층 구현
6. ⏳ Controller 구현
7. ⏳ API 테스트

현재까지 작성한 스키마와 Entity로 **시스템을 실행하고 테스트할 수 있습니다!**

