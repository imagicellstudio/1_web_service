# 테이블 목록 및 명세 (Phase 1 MVP)

## 문서 정보
- 작성일: 2025-11-19
- 버전: 1.0 (Phase 1 MVP)
- 목적: 테이블별 상세 명세 및 관계 정의

---

## 1. 테이블 목록

| # | 테이블명 | 한글명 | 레코드 예상 | 용도 |
|---|---------|--------|------------|------|
| 1 | users | 회원 | 10,000 | 사용자 계정 정보 |
| 2 | categories | 카테고리 | 100 | 상품 분류 |
| 3 | products | 상품 | 50,000 | 판매 상품 정보 |
| 4 | orders | 주문 | 100,000 | 주문 내역 |
| 5 | order_items | 주문항목 | 200,000 | 주문별 상품 목록 |
| 6 | payments | 결제 | 100,000 | 결제 정보 |
| 7 | reviews | 리뷰 | 50,000 | 상품 리뷰 |

**총 테이블 수**: 7개  
**예상 총 레코드**: ~510,000건

---

## 2. 테이블 상세 명세

### 2.1 users (회원)

#### 기본 정보
- **용도**: 사용자 계정 및 인증 정보
- **예상 레코드**: 10,000건
- **성장률**: 월 500명 증가 예상

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 사용자 ID | PK |
| email | VARCHAR(255) | NO | - | 이메일 | UK, 로그인 ID |
| password_hash | VARCHAR(255) | YES | NULL | 비밀번호 해시 | OAuth의 경우 NULL |
| name | VARCHAR(200) | NO | - | 이름 | |
| phone | VARCHAR(20) | YES | NULL | 전화번호 | |
| role | VARCHAR(20) | NO | 'buyer' | 역할 | buyer/seller/admin |
| status | VARCHAR(20) | NO | 'active' | 상태 | active/inactive/suspended |
| language | VARCHAR(10) | NO | 'ko' | 언어 | ko/en |
| created_at | TIMESTAMP | NO | NOW() | 생성일시 | |
| updated_at | TIMESTAMP | NO | NOW() | 수정일시 | 자동 갱신 |
| last_login_at | TIMESTAMP | YES | NULL | 마지막 로그인 | |

#### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE INDEX uk_users_email (email)
INDEX idx_users_role (role)
INDEX idx_users_status (status)
```

#### 제약조건
```sql
CHECK (role IN ('buyer', 'seller', 'admin'))
CHECK (status IN ('active', 'inactive', 'suspended'))
CHECK (language IN ('ko', 'en'))
CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$')
```

#### 샘플 데이터
```sql
-- 관리자
(1, 'admin@spicyjump.com', '$2b$12$...', 'Admin', NULL, 'admin', 'active', 'en', NOW(), NOW(), NULL)

-- 판매자
(2, 'seller@example.com', '$2b$12$...', 'John Kim', '010-1234-5678', 'seller', 'active', 'ko', NOW(), NOW(), NOW())

-- 구매자
(3, 'buyer@example.com', '$2b$12$...', 'Jane Lee', '010-9876-5432', 'buyer', 'active', 'en', NOW(), NOW(), NOW())
```

---

### 2.2 categories (카테고리)

#### 기본 정보
- **용도**: 상품 분류 체계 (2단계 계층)
- **예상 레코드**: 100건
- **성장률**: 분기별 10개 추가

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 카테고리 ID | PK |
| parent_id | INTEGER | YES | NULL | 상위 카테고리 | FK, NULL이면 최상위 |
| name | VARCHAR(200) | NO | - | 카테고리명(한글) | |
| name_en | VARCHAR(200) | YES | NULL | 카테고리명(영문) | |
| sort_order | INTEGER | NO | 0 | 정렬 순서 | |
| created_at | TIMESTAMP | NO | NOW() | 생성일시 | |

#### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_categories_parent_id (parent_id)
INDEX idx_categories_sort_order (sort_order)
```

#### 관계
```sql
FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE CASCADE
```

#### 계층 구조 예시
```
├── 김치/반찬 (1)
│   ├── 배추김치 (5)
│   └── 깍두기 (6)
├── 장류/양념 (2)
│   ├── 고추장 (7)
│   └── 된장 (8)
├── 면류 (3)
│   ├── 라면 (9)
│   └── 냉면 (10)
└── 과자/간식 (4)
```

#### 샘플 데이터
```sql
-- 최상위 카테고리
(1, NULL, '김치/반찬', 'Kimchi & Side Dishes', 1, NOW()),
(2, NULL, '장류/양념', 'Sauces & Seasonings', 2, NOW()),
(3, NULL, '면류', 'Noodles', 3, NOW()),
(4, NULL, '과자/간식', 'Snacks', 4, NOW()),

-- 하위 카테고리
(5, 1, '배추김치', 'Cabbage Kimchi', 1, NOW()),
(6, 1, '깍두기', 'Radish Kimchi', 2, NOW()),
(7, 2, '고추장', 'Gochujang', 1, NOW()),
(8, 2, '된장', 'Doenjang', 2, NOW()),
(9, 3, '라면', 'Instant Noodles', 1, NOW()),
(10, 3, '냉면', 'Cold Noodles', 2, NOW())
```

---

### 2.3 products (상품)

#### 기본 정보
- **용도**: 판매 상품 정보
- **예상 레코드**: 50,000건
- **성장률**: 월 1,000개 증가

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 상품 ID | PK |
| seller_id | INTEGER | NO | - | 판매자 ID | FK |
| category_id | INTEGER | YES | NULL | 카테고리 ID | FK |
| name | VARCHAR(500) | NO | - | 상품명(한글) | |
| name_en | VARCHAR(500) | YES | NULL | 상품명(영문) | |
| description | TEXT | YES | NULL | 설명(한글) | |
| description_en | TEXT | YES | NULL | 설명(영문) | |
| price | DECIMAL(15,2) | NO | - | 가격 | >= 0 |
| currency | VARCHAR(3) | NO | 'USD' | 통화 | USD/KRW/EUR 등 |
| stock_quantity | INTEGER | NO | 0 | 재고 수량 | >= 0 |
| images | JSONB | YES | NULL | 이미지 URL 배열 | |
| status | VARCHAR(20) | NO | 'draft' | 상태 | draft/published/soldout |
| view_count | INTEGER | NO | 0 | 조회수 | |
| rating_average | DECIMAL(3,2) | YES | 0 | 평균 평점 | 0.00-5.00 |
| review_count | INTEGER | NO | 0 | 리뷰 수 | |
| created_at | TIMESTAMP | NO | NOW() | 등록일시 | |
| updated_at | TIMESTAMP | NO | NOW() | 수정일시 | 자동 갱신 |

#### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_products_seller_id (seller_id)
INDEX idx_products_category_id (category_id)
INDEX idx_products_status (status)
INDEX idx_products_created_at (created_at DESC)
INDEX idx_products_rating (rating_average DESC)
INDEX idx_products_price (price)
GIN INDEX idx_products_name_search ON (to_tsvector('english', name))
```

#### 관계
```sql
FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE CASCADE
FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
```

#### 제약조건
```sql
CHECK (price >= 0)
CHECK (stock_quantity >= 0)
CHECK (status IN ('draft', 'published', 'soldout', 'discontinued'))
CHECK (currency IN ('USD', 'KRW', 'EUR', 'JPY', 'CNY'))
CHECK (rating_average >= 0 AND rating_average <= 5)
```

#### images JSONB 구조
```json
[
  "https://s3.amazonaws.com/.../product1.jpg",
  "https://s3.amazonaws.com/.../product2.jpg",
  "https://s3.amazonaws.com/.../product3.jpg"
]
```

#### 샘플 데이터
```sql
-- 배추김치
(1, 2, 5, '배추김치 1kg', 'Cabbage Kimchi 1kg', '정성껏 담근 배추김치입니다', 'Traditionally fermented cabbage kimchi', 15.00, 'USD', 100, '["https://...img1.jpg", "https://...img2.jpg"]', 'published', 150, 4.5, 10, NOW(), NOW()),

-- 고추장
(2, 2, 7, '태양초 고추장 500g', 'Gochujang 500g', '태양초로 만든 고추장', 'Made with sun-dried peppers', 12.00, 'USD', 50, '["https://...img1.jpg"]', 'published', 80, 4.8, 5, NOW(), NOW()),

-- 라면
(3, 2, 9, '신라면 5개입', 'Shin Ramyun 5pack', '매운맛 라면', 'Spicy Korean noodles', 8.00, 'USD', 200, '["https://...img1.jpg"]', 'published', 500, 4.7, 50, NOW(), NOW())
```

---

### 2.4 orders (주문)

#### 기본 정보
- **용도**: 주문 정보
- **예상 레코드**: 100,000건
- **성장률**: 월 3,000건 증가

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 주문 ID | PK |
| order_number | VARCHAR(50) | NO | - | 주문번호 | UK, 표시용 |
| buyer_id | INTEGER | NO | - | 구매자 ID | FK |
| seller_id | INTEGER | NO | - | 판매자 ID | FK |
| subtotal | DECIMAL(15,2) | NO | - | 소계 | >= 0 |
| shipping_fee | DECIMAL(15,2) | NO | 0 | 배송비 | |
| total | DECIMAL(15,2) | NO | - | 총액 | >= 0 |
| currency | VARCHAR(3) | NO | 'USD' | 통화 | |
| shipping_address | JSONB | NO | - | 배송지 정보 | JSON |
| status | VARCHAR(50) | NO | 'pending' | 주문 상태 | |
| buyer_note | TEXT | YES | NULL | 구매자 메모 | |
| created_at | TIMESTAMP | NO | NOW() | 주문일시 | |
| updated_at | TIMESTAMP | NO | NOW() | 수정일시 | |
| confirmed_at | TIMESTAMP | YES | NULL | 확인일시 | |
| shipped_at | TIMESTAMP | YES | NULL | 배송일시 | |
| delivered_at | TIMESTAMP | YES | NULL | 완료일시 | |
| cancelled_at | TIMESTAMP | YES | NULL | 취소일시 | |

#### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE INDEX uk_orders_number (order_number)
INDEX idx_orders_buyer_id (buyer_id)
INDEX idx_orders_seller_id (seller_id)
INDEX idx_orders_status (status)
INDEX idx_orders_created_at (created_at DESC)
```

#### 관계
```sql
FOREIGN KEY (buyer_id) REFERENCES users(id) ON DELETE RESTRICT
FOREIGN KEY (seller_id) REFERENCES users(id) ON DELETE RESTRICT
```

#### 제약조건
```sql
CHECK (subtotal >= 0)
CHECK (total >= 0)
CHECK (status IN ('pending', 'confirmed', 'preparing', 'shipped', 'delivered', 'cancelled'))
```

#### shipping_address JSONB 구조
```json
{
  "name": "홍길동",
  "phone": "010-1234-5678",
  "address": "123 Main St, Seoul",
  "city": "Seoul",
  "state": "Seoul",
  "zipcode": "06000",
  "country": "KR"
}
```

#### 주문번호 형식
```
ORD-YYYYMMDD-#### (예: ORD-20251119-0001)
```

#### 샘플 데이터
```sql
-- 완료된 주문
(1, 'ORD-20251119-0001', 3, 2, 15.00, 3.00, 18.00, 'USD', '{"name":"Jane Lee","phone":"010-9876-5432","address":"123 Main St","zipcode":"06000"}', 'delivered', NULL, '2025-11-19 10:00:00', '2025-11-19 10:05:00', '2025-11-19 10:10:00', '2025-11-19 14:30:00', '2025-11-20 15:00:00', NULL),

-- 진행중 주문
(2, 'ORD-20251119-0002', 3, 2, 20.00, 3.00, 23.00, 'USD', '{"name":"Jane Lee","phone":"010-9876-5432","address":"123 Main St","zipcode":"06000"}', 'preparing', NULL, NOW(), NOW(), NOW(), NULL, NULL, NULL)
```

---

### 2.5 order_items (주문항목)

#### 기본 정보
- **용도**: 주문별 상품 목록
- **예상 레코드**: 200,000건 (주문당 평균 2개 상품)
- **성장률**: 월 6,000건 증가

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 항목 ID | PK |
| order_id | INTEGER | NO | - | 주문 ID | FK |
| product_id | INTEGER | NO | - | 상품 ID | FK |
| product_name | VARCHAR(500) | NO | - | 상품명 스냅샷 | |
| product_name_en | VARCHAR(500) | YES | NULL | 상품명 영문 | |
| quantity | INTEGER | NO | - | 수량 | > 0 |
| unit_price | DECIMAL(15,2) | NO | - | 단가 | >= 0 |
| subtotal | DECIMAL(15,2) | NO | - | 소계 | >= 0 |
| created_at | TIMESTAMP | NO | NOW() | 생성일시 | |

#### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_order_items_order_id (order_id)
INDEX idx_order_items_product_id (product_id)
```

#### 관계
```sql
FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
```

#### 제약조건
```sql
CHECK (quantity > 0)
CHECK (unit_price >= 0)
CHECK (subtotal >= 0)
CHECK (subtotal = quantity * unit_price)
```

#### 샘플 데이터
```sql
-- 주문 1의 항목
(1, 1, 1, '배추김치 1kg', 'Cabbage Kimchi 1kg', 1, 15.00, 15.00, '2025-11-19 10:00:00'),

-- 주문 2의 항목들
(2, 2, 1, '배추김치 1kg', 'Cabbage Kimchi 1kg', 1, 15.00, 15.00, NOW()),
(3, 2, 3, '신라면 5개입', 'Shin Ramyun 5pack', 1, 8.00, 8.00, NOW())
```

---

### 2.6 payments (결제)

#### 기본 정보
- **용도**: 결제 정보 및 PG 연동
- **예상 레코드**: 100,000건
- **성장률**: 월 3,000건 증가

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 결제 ID | PK |
| order_id | INTEGER | NO | - | 주문 ID | FK |
| amount | DECIMAL(15,2) | NO | - | 결제 금액 | >= 0 |
| currency | VARCHAR(3) | NO | 'USD' | 통화 | |
| payment_method | VARCHAR(50) | NO | - | 결제 수단 | card/paypal 등 |
| status | VARCHAR(50) | NO | 'pending' | 결제 상태 | |
| pg_provider | VARCHAR(50) | YES | NULL | PG사 | stripe/paypal |
| pg_transaction_id | VARCHAR(255) | YES | NULL | PG 거래 ID | |
| pg_response | JSONB | YES | NULL | PG 응답 전체 | |
| created_at | TIMESTAMP | NO | NOW() | 생성일시 | |
| paid_at | TIMESTAMP | YES | NULL | 결제완료일시 | |
| refunded_at | TIMESTAMP | YES | NULL | 환불일시 | |

#### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_payments_order_id (order_id)
INDEX idx_payments_status (status)
INDEX idx_payments_pg_transaction_id (pg_transaction_id)
```

#### 관계
```sql
FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
```

#### 제약조건
```sql
CHECK (amount >= 0)
CHECK (status IN ('pending', 'completed', 'failed', 'cancelled', 'refunded'))
CHECK (payment_method IN ('card', 'bank_transfer', 'paypal', 'stripe'))
```

#### pg_response JSONB 구조 (Stripe 예시)
```json
{
  "id": "ch_3ABC123",
  "amount": 1800,
  "currency": "usd",
  "status": "succeeded",
  "payment_method": "card_1XYZ",
  "created": 1637000000
}
```

#### 샘플 데이터
```sql
-- 완료된 결제
(1, 1, 18.00, 'USD', 'stripe', 'completed', 'stripe', 'ch_3ABC123def456', '{"id":"ch_3ABC123","status":"succeeded"}', '2025-11-19 10:00:00', '2025-11-19 10:00:30', NULL),

-- 대기중 결제
(2, 2, 23.00, 'USD', 'stripe', 'pending', 'stripe', NULL, NULL, NOW(), NULL, NULL)
```

---

### 2.7 reviews (리뷰)

#### 기본 정보
- **용도**: 상품 리뷰
- **예상 레코드**: 50,000건
- **성장률**: 월 1,500건 증가

#### 컬럼 명세

| 컬럼명 | 타입 | Null | 기본값 | 설명 | 비고 |
|--------|------|------|--------|------|------|
| id | SERIAL | NO | AUTO | 리뷰 ID | PK |
| product_id | INTEGER | NO | - | 상품 ID | FK |
| user_id | INTEGER | NO | - | 작성자 ID | FK |
| rating | INTEGER | NO | - | 평점 | 1-5 |
| title | VARCHAR(200) | YES | NULL | 제목 | |
| content | TEXT | NO | - | 내용 | |
| images | JSONB | YES | NULL | 이미지 URL 배열 | |
| is_verified_purchase | BOOLEAN | NO | FALSE | 구매 인증 | |
| status | VARCHAR(20) | NO | 'published' | 상태 | |
| created_at | TIMESTAMP | NO | NOW() | 작성일시 | |
| updated_at | TIMESTAMP | NO | NOW() | 수정일시 | |

#### 인덱스
```sql
PRIMARY KEY (id)
INDEX idx_reviews_product_id (product_id)
INDEX idx_reviews_user_id (user_id)
INDEX idx_reviews_rating (rating)
INDEX idx_reviews_created_at (created_at DESC)
UNIQUE INDEX idx_reviews_unique ON (product_id, user_id) WHERE status='published'
```

#### 관계
```sql
FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
```

#### 제약조건
```sql
CHECK (rating >= 1 AND rating <= 5)
CHECK (status IN ('published', 'hidden', 'deleted'))
CHECK (LENGTH(content) >= 10) -- 최소 10자
```

#### images JSONB 구조
```json
[
  "https://s3.amazonaws.com/.../review1.jpg",
  "https://s3.amazonaws.com/.../review2.jpg"
]
```

#### 샘플 데이터
```sql
-- 긍정 리뷰
(1, 1, 3, 5, 'Great kimchi!', 'Very authentic taste. Highly recommend!', '["https://...img1.jpg"]', TRUE, 'published', '2025-11-20 10:00:00', '2025-11-20 10:00:00'),

-- 일반 리뷰
(2, 1, 3, 4, 'Good product', 'Nice kimchi but a bit expensive', NULL, TRUE, 'published', '2025-11-21 15:30:00', '2025-11-21 15:30:00'),

-- 부정 리뷰
(3, 2, 3, 3, 'Average', 'Not as spicy as I expected', NULL, FALSE, 'published', '2025-11-21 16:00:00', '2025-11-21 16:00:00')
```

---

## 3. 관계 매트릭스

### 3.1 테이블 간 관계

|  | users | categories | products | orders | order_items | payments | reviews |
|---|-------|-----------|----------|--------|-------------|----------|---------|
| **users** | - | - | 1:N (판매) | 1:N (구매) | - | - | 1:N (작성) |
| **categories** | - | 1:N (계층) | 1:N | - | - | - | - |
| **products** | N:1 | N:1 | - | - | 1:N | - | 1:N |
| **orders** | N:1 | - | - | - | 1:N | 1:1 | - |
| **order_items** | - | - | N:1 | N:1 | - | - | - |
| **payments** | - | - | - | 1:1 | - | - | - |
| **reviews** | N:1 | - | N:1 | - | - | - | - |

### 3.2 CASCADE 동작

| 부모 테이블 | 자식 테이블 | DELETE | UPDATE |
|-----------|-----------|--------|--------|
| users | products | CASCADE | CASCADE |
| users | orders | RESTRICT | CASCADE |
| users | reviews | CASCADE | CASCADE |
| categories | categories | CASCADE | CASCADE |
| categories | products | SET NULL | CASCADE |
| products | order_items | RESTRICT | CASCADE |
| products | reviews | CASCADE | CASCADE |
| orders | order_items | CASCADE | CASCADE |
| orders | payments | RESTRICT | CASCADE |

---

## 4. 데이터 무결성

### 4.1 참조 무결성
- 모든 FK는 참조하는 테이블의 PK와 매칭
- DELETE/UPDATE 시 CASCADE 또는 RESTRICT 적용

### 4.2 도메인 무결성
- CHECK 제약조건으로 값 범위 제한
- NOT NULL 제약으로 필수 값 보장

### 4.3 엔터티 무결성
- 모든 테이블에 PK 존재
- 자동 증가(SERIAL) 사용

### 4.4 비즈니스 규칙
- 리뷰: 사용자당 상품 1개만 (UNIQUE INDEX)
- 재고: 음수 불가 (CHECK)
- 평점: 1-5 범위 (CHECK)
- 가격: 음수 불가 (CHECK)

---

## 5. 데이터 볼륨 추정

### 5.1 연간 성장 예측

| 테이블 | 초기 | 6개월 | 12개월 | 증가율 |
|--------|------|-------|--------|--------|
| users | 1,000 | 4,000 | 7,000 | 500/월 |
| products | 5,000 | 11,000 | 17,000 | 1,000/월 |
| orders | 1,000 | 19,000 | 37,000 | 3,000/월 |
| order_items | 2,000 | 38,000 | 74,000 | 6,000/월 |
| payments | 1,000 | 19,000 | 37,000 | 3,000/월 |
| reviews | 500 | 9,500 | 18,500 | 1,500/월 |

### 5.2 스토리지 추정

```
테이블별 평균 row 크기:
- users: ~500 bytes
- products: ~2 KB (이미지 URL 포함)
- orders: ~1 KB
- order_items: ~200 bytes
- payments: ~500 bytes
- reviews: ~1.5 KB (이미지 URL 포함)

12개월 후 예상 DB 크기: ~150 MB (인덱스 포함)
```

---

## 부록

### A. 전체 테이블 생성 스크립트

```sql
-- database-schema-phase1.sql 참조
```

### B. 테이블 관계도 (ASCII)

```
           ┌─────────┐
           │  users  │
           └────┬────┘
                │
      ┌─────────┼─────────┐
      │         │         │
      ▼         ▼         ▼
┌──────────┐ ┌────────┐ ┌─────────┐
│ products │ │ orders │ │ reviews │
└────┬─────┘ └───┬────┘ └─────────┘
     │           │
     │      ┌────┴─────┐
     │      ▼          ▼
     │  ┌────────────┐ ┌──────────┐
     └─>│order_items │ │ payments │
        └────────────┘ └──────────┘
```

---

**문서 관리**
- 작성자: 장재훈
- 최종 업데이트: 2025-11-19
- 관련 문서: 데이터베이스 설계서


