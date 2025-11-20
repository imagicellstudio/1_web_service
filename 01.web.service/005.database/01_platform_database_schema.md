# 플랫폼 데이터베이스 스키마 설계

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0 (플랫폼)
- DBMS: PostgreSQL 15+

---

## 1. 데이터베이스 구조 개요

### 1.1 데이터베이스 분리 전략

```
┌─────────────────────────────────────────┐
│  Primary Database (PostgreSQL)          │
│  - 사용자, 거래, 상품, 주문 등 핵심 데이터  │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Cache Layer (Redis)                    │
│  - 세션, 임시 데이터, 실시간 데이터        │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Search Engine (Elasticsearch)          │
│  - 상품 검색, 전문 검색                   │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Document Store (MongoDB)               │
│  - 로그, 비정형 데이터, 분석 데이터        │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│  Blockchain Ledger                      │
│  - 거래 원장, 스마트 컨트랙트             │
└─────────────────────────────────────────┘
```

---

## 2. PostgreSQL 스키마

### 2.1 사용자 관련 테이블

```sql
-- 사용자 테이블
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    username VARCHAR(100),
    full_name VARCHAR(200),
    phone VARCHAR(20),
    role VARCHAR(50) DEFAULT 'buyer', -- buyer, seller, admin, moderator, super_admin
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, suspended, deleted
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    language_preference VARCHAR(10) DEFAULT 'ko',
    timezone VARCHAR(50) DEFAULT 'Asia/Seoul',
    avatar_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    last_login_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);

-- 사용자 프로필 (추가 정보)
CREATE TABLE user_profiles (
    id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    company_name VARCHAR(200),
    business_number VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(2), -- ISO 3166-1 alpha-2
    postal_code VARCHAR(20),
    bio TEXT,
    website VARCHAR(255),
    social_links JSONB, -- {twitter: '', facebook: '', instagram: ''}
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_country ON user_profiles(country);

-- 역할 및 권한
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    description TEXT,
    is_system BOOLEAN DEFAULT FALSE, -- 시스템 기본 역할 여부
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    resource VARCHAR(100) NOT NULL, -- 'transaction', 'member', 'product', etc.
    action VARCHAR(50) NOT NULL, -- 'read', 'create', 'update', 'delete'
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_permissions_resource_action ON permissions(resource, action);

CREATE TABLE role_permissions (
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permissions(id) ON DELETE CASCADE,
    granted_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(id) ON DELETE CASCADE,
    granted_by INTEGER REFERENCES users(id),
    granted_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- 로그인 이력
CREATE TABLE login_history (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    ip_address INET,
    user_agent TEXT,
    location JSONB, -- {country, city, latitude, longitude}
    success BOOLEAN,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_login_history_user_id ON login_history(user_id);
CREATE INDEX idx_login_history_created_at ON login_history(created_at);
```

### 2.2 상품 관련 테이블

```sql
-- 카테고리
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    parent_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    name_ko VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    name_zh VARCHAR(200),
    name_ja VARCHAR(200),
    slug VARCHAR(200) UNIQUE,
    description TEXT,
    icon_url VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_slug ON categories(slug);

-- 상품
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    seller_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES categories(id),
    hs_code VARCHAR(10), -- HS Code
    name_ko VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    name_zh VARCHAR(500),
    name_ja VARCHAR(500),
    description_ko TEXT,
    description_en TEXT,
    description_zh TEXT,
    description_ja TEXT,
    price DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KRW',
    discount_percentage DECIMAL(5, 2) DEFAULT 0,
    stock_quantity INTEGER DEFAULT 0,
    min_order_quantity INTEGER DEFAULT 1,
    max_order_quantity INTEGER,
    weight DECIMAL(10, 3), -- kg
    dimensions JSONB, -- {length, width, height} in cm
    status VARCHAR(20) DEFAULT 'draft', -- draft, published, soldout, discontinued
    is_featured BOOLEAN DEFAULT FALSE,
    view_count INTEGER DEFAULT 0,
    rating_average DECIMAL(3, 2) DEFAULT 0,
    review_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_hs_code ON products(hs_code);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_rating ON products(rating_average DESC);

-- 상품 이미지
CREATE TABLE product_images (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    alt_text VARCHAR(255),
    sort_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);

-- 상품 옵션 (사이즈, 색상 등)
CREATE TABLE product_options (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL, -- '사이즈', '색상' 등
    values JSONB NOT NULL, -- ['S', 'M', 'L'] 또는 ['Red', 'Blue']
    created_at TIMESTAMP DEFAULT NOW()
);

-- 상품 변형 (옵션 조합별 SKU)
CREATE TABLE product_variants (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) UNIQUE,
    option_values JSONB, -- {size: 'M', color: 'Red'}
    price DECIMAL(15, 2),
    stock_quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);
CREATE INDEX idx_product_variants_sku ON product_variants(sku);

-- 원산지 정보
CREATE TABLE product_origin (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    ingredient_name VARCHAR(200),
    origin_country VARCHAR(2), -- ISO 3166-1 alpha-2
    origin_region VARCHAR(200),
    percentage DECIMAL(5, 2), -- 함량 비율
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_origin_product_id ON product_origin(product_id);

-- HACCP 인증
CREATE TABLE product_haccp (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    certification_number VARCHAR(50),
    certification_agency VARCHAR(200),
    issue_date DATE,
    expiry_date DATE,
    certificate_file_url TEXT,
    is_valid BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_haccp_product_id ON product_haccp(product_id);

-- 영양성분 정보
CREATE TABLE product_nutrition (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    serving_size VARCHAR(50), -- '210g'
    calories DECIMAL(10, 2),
    carbohydrates DECIMAL(10, 2),
    protein DECIMAL(10, 2),
    fat DECIMAL(10, 2),
    saturated_fat DECIMAL(10, 2),
    trans_fat DECIMAL(10, 2),
    cholesterol DECIMAL(10, 2),
    sodium DECIMAL(10, 2),
    sugars DECIMAL(10, 2),
    fiber DECIMAL(10, 2),
    additional_info JSONB, -- 기타 영양소
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 알러지 정보
CREATE TABLE product_allergens (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    allergen_type VARCHAR(50), -- '우유', '계란', '밀', '대두' 등
    severity VARCHAR(20), -- 'contains', 'may_contain'
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_product_allergens_product_id ON product_allergens(product_id);
```

### 2.3 거래 관련 테이블

```sql
-- 장바구니
CREATE TABLE cart (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    variant_id INTEGER REFERENCES product_variants(id) ON DELETE SET NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    added_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_cart_user_id ON cart(user_id);
CREATE UNIQUE INDEX idx_cart_user_product ON cart(user_id, product_id, variant_id);

-- 주문
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    buyer_id INTEGER REFERENCES users(id),
    seller_id INTEGER REFERENCES users(id),
    status VARCHAR(50) DEFAULT 'pending', -- pending, confirmed, preparing, shipped, delivered, cancelled, refunded
    subtotal DECIMAL(15, 2) NOT NULL,
    shipping_fee DECIMAL(15, 2) DEFAULT 0,
    tax DECIMAL(15, 2) DEFAULT 0,
    discount DECIMAL(15, 2) DEFAULT 0,
    total DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KRW',
    shipping_address JSONB, -- {name, phone, address, city, state, country, postal_code}
    billing_address JSONB,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP
);

CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

-- 주문 항목
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    product_id INTEGER REFERENCES products(id),
    variant_id INTEGER REFERENCES product_variants(id),
    product_name VARCHAR(500),
    sku VARCHAR(100),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- 결제
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    payment_method VARCHAR(50), -- card, bank_transfer, paypal, crypto
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'KRW',
    status VARCHAR(50) DEFAULT 'pending', -- pending, completed, failed, cancelled, refunded
    pg_provider VARCHAR(50), -- 'stripe', 'tosspayments', 'inicis'
    pg_transaction_id VARCHAR(255),
    pg_response JSONB, -- PG사 응답 전체
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    refund_amount DECIMAL(15, 2),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);

-- 블록체인 거래
CREATE TABLE blockchain_transactions (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    transaction_hash VARCHAR(255) UNIQUE,
    block_number BIGINT,
    from_address VARCHAR(255),
    to_address VARCHAR(255),
    value DECIMAL(30, 18),
    gas_used INTEGER,
    confirmations INTEGER DEFAULT 0,
    status VARCHAR(50) DEFAULT 'pending', -- pending, confirmed, failed
    smart_contract_address VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    confirmed_at TIMESTAMP
);

CREATE INDEX idx_blockchain_tx_order_id ON blockchain_transactions(order_id);
CREATE INDEX idx_blockchain_tx_hash ON blockchain_transactions(transaction_hash);

-- 배송
CREATE TABLE shipments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id) ON DELETE CASCADE,
    tracking_number VARCHAR(100),
    carrier VARCHAR(100), -- 'CJ대한통운', 'DHL', 'FedEx'
    status VARCHAR(50) DEFAULT 'preparing', -- preparing, shipped, in_transit, out_for_delivery, delivered
    shipped_at TIMESTAMP,
    estimated_delivery DATE,
    delivered_at TIMESTAMP,
    recipient_name VARCHAR(200),
    recipient_phone VARCHAR(20),
    shipping_address TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_shipments_order_id ON shipments(order_id);
CREATE INDEX idx_shipments_tracking_number ON shipments(tracking_number);
```

### 2.4 리뷰 및 평가

```sql
-- 리뷰
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    order_id INTEGER REFERENCES orders(id) ON DELETE SET NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    content TEXT,
    images JSONB, -- [url1, url2, ...]
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    helpful_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'published', -- published, hidden, flagged
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- 리뷰 도움됨 투표
CREATE TABLE review_votes (
    review_id INTEGER REFERENCES reviews(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    is_helpful BOOLEAN,
    created_at TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (review_id, user_id)
);
```

### 2.5 라벨 및 리워드

```sql
-- 라벨
CREATE TABLE labels (
    id SERIAL PRIMARY KEY,
    name_ko VARCHAR(100) NOT NULL,
    name_en VARCHAR(100),
    name_zh VARCHAR(100),
    name_ja VARCHAR(100),
    type VARCHAR(50), -- quality, certification, origin, custom
    color VARCHAR(7), -- HEX color
    icon_url VARCHAR(255),
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 상품 라벨
CREATE TABLE product_labels (
    product_id INTEGER REFERENCES products(id) ON DELETE CASCADE,
    label_id INTEGER REFERENCES labels(id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT NOW(),
    assigned_by INTEGER REFERENCES users(id),
    PRIMARY KEY (product_id, label_id)
);

-- 리워드 포인트
CREATE TABLE reward_points (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    points INTEGER DEFAULT 0,
    level VARCHAR(50) DEFAULT 'bronze', -- bronze, silver, gold, platinum
    total_earned INTEGER DEFAULT 0,
    total_spent INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_reward_points_user_id ON reward_points(user_id);

-- 포인트 이력
CREATE TABLE point_transactions (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50), -- earn, spend, expire, refund
    amount INTEGER NOT NULL,
    reason VARCHAR(200),
    reference_type VARCHAR(50), -- order, review, referral
    reference_id INTEGER,
    balance_after INTEGER,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_point_tx_user_id ON point_transactions(user_id);
```

### 2.6 표준정보 테이블

```sql
-- HS Code
CREATE TABLE hs_codes (
    id SERIAL PRIMARY KEY,
    code VARCHAR(10) UNIQUE NOT NULL,
    code_6 VARCHAR(6), -- 국제 공통 코드
    chapter VARCHAR(2),
    heading VARCHAR(4),
    description_ko TEXT,
    description_en TEXT,
    parent_code VARCHAR(10),
    level INTEGER,
    is_leaf BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_hs_codes_code ON hs_codes(code);
CREATE INDEX idx_hs_codes_chapter ON hs_codes(chapter);

-- 관세율
CREATE TABLE tariff_rates (
    id SERIAL PRIMARY KEY,
    hs_code VARCHAR(10) REFERENCES hs_codes(code),
    country_code VARCHAR(2), -- 수출국
    rate_type VARCHAR(20), -- basic, fta, quota
    tariff_rate DECIMAL(5, 2),
    fta_name VARCHAR(100),
    effective_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_tariff_rates_hs_code ON tariff_rates(hs_code);

-- HACCP 인증 업체
CREATE TABLE haccp_certifications (
    id SERIAL PRIMARY KEY,
    company_id INTEGER REFERENCES users(id),
    certification_number VARCHAR(50) UNIQUE,
    certification_type VARCHAR(20), -- food, livestock
    certification_agency VARCHAR(100),
    product_category VARCHAR(100),
    issue_date DATE,
    expiry_date DATE,
    status VARCHAR(20) DEFAULT 'active',
    certificate_file_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_haccp_company_id ON haccp_certifications(company_id);

-- 알러지 유발 식품 표준
CREATE TABLE allergen_standards (
    id SERIAL PRIMARY KEY,
    name_ko VARCHAR(100),
    name_en VARCHAR(100),
    code VARCHAR(50),
    category VARCHAR(100),
    severity_level VARCHAR(20), -- high, medium, low
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 2.7 데이터 관리

```sql
-- 데이터 전처리 작업
CREATE TABLE data_preprocessing_jobs (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200),
    data_type VARCHAR(50), -- food, origin, transaction, member
    operations JSONB, -- ['clean', 'normalize', 'validate']
    status VARCHAR(50) DEFAULT 'pending', -- pending, running, completed, failed
    records_total INTEGER,
    records_processed INTEGER DEFAULT 0,
    records_success INTEGER DEFAULT 0,
    records_failed INTEGER DEFAULT 0,
    error_log JSONB,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 데이터 라벨링 (AI 학습용)
CREATE TABLE data_labels (
    id SERIAL PRIMARY KEY,
    data_type VARCHAR(50), -- image, text, product
    data_id INTEGER,
    labels JSONB, -- [{label: 'category', value: 'kimchi', confidence: 0.95}]
    labeled_by INTEGER REFERENCES users(id),
    is_verified BOOLEAN DEFAULT FALSE,
    verified_by INTEGER REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);
```

### 2.8 보안 및 감사

```sql
-- 보안 로그
CREATE TABLE security_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    event_type VARCHAR(100), -- login_failed, unauthorized_access, password_changed
    severity VARCHAR(20), -- critical, high, medium, low
    ip_address INET,
    user_agent TEXT,
    details JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_security_logs_user_id ON security_logs(user_id);
CREATE INDEX idx_security_logs_severity ON security_logs(severity);
CREATE INDEX idx_security_logs_created_at ON security_logs(created_at);

-- API 키 관리
CREATE TABLE api_keys (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    key_hash VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(100),
    permissions JSONB, -- ['read:products', 'write:orders']
    rate_limit INTEGER DEFAULT 1000, -- per hour
    is_active BOOLEAN DEFAULT TRUE,
    last_used_at TIMESTAMP,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_api_keys_user_id ON api_keys(user_id);
CREATE INDEX idx_api_keys_key_hash ON api_keys(key_hash);

-- 감사 로그
CREATE TABLE audit_logs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    action VARCHAR(100), -- create, update, delete
    resource_type VARCHAR(50), -- product, order, user
    resource_id INTEGER,
    old_data JSONB,
    new_data JSONB,
    ip_address INET,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource_type, resource_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

### 2.9 연계 관리

```sql
-- 외부 API 연동 설정
CREATE TABLE external_integrations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    type VARCHAR(50), -- api, payment, blockchain, public
    provider VARCHAR(100),
    config JSONB, -- {api_key, api_secret, endpoint}
    status VARCHAR(20) DEFAULT 'active', -- active, inactive, error
    last_sync_at TIMESTAMP,
    sync_frequency VARCHAR(50), -- hourly, daily, manual
    success_count INTEGER DEFAULT 0,
    error_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- API 연동 로그
CREATE TABLE integration_logs (
    id SERIAL PRIMARY KEY,
    integration_id INTEGER REFERENCES external_integrations(id),
    request_method VARCHAR(10),
    request_url TEXT,
    request_body JSONB,
    response_status INTEGER,
    response_body JSONB,
    response_time INTEGER, -- milliseconds
    success BOOLEAN,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_integration_logs_integration_id ON integration_logs(integration_id);
CREATE INDEX idx_integration_logs_created_at ON integration_logs(created_at);
```

### 2.10 다국어 지원

```sql
-- 번역
CREATE TABLE translations (
    id SERIAL PRIMARY KEY,
    key VARCHAR(255) UNIQUE NOT NULL,
    ko TEXT,
    en TEXT,
    zh TEXT,
    ja TEXT,
    vi TEXT,
    es TEXT,
    category VARCHAR(100), -- ui, product, email
    status VARCHAR(20) DEFAULT 'draft', -- draft, published, needs_review
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_translations_key ON translations(key);
CREATE INDEX idx_translations_category ON translations(category);
```

---

## 3. 데이터베이스 함수 및 트리거

### 3.1 상품 평점 자동 업데이트

```sql
CREATE OR REPLACE FUNCTION update_product_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE products
    SET 
        rating_average = (
            SELECT AVG(rating)::DECIMAL(3,2)
            FROM reviews
            WHERE product_id = NEW.product_id AND status = 'published'
        ),
        review_count = (
            SELECT COUNT(*)
            FROM reviews
            WHERE product_id = NEW.product_id AND status = 'published'
        ),
        updated_at = NOW()
    WHERE id = NEW.product_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_product_rating
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_product_rating();
```

### 3.2 재고 감소

```sql
CREATE OR REPLACE FUNCTION decrease_stock()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE products
    SET 
        stock_quantity = stock_quantity - NEW.quantity,
        updated_at = NOW()
    WHERE id = NEW.product_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_decrease_stock
AFTER INSERT ON order_items
FOR EACH ROW
EXECUTE FUNCTION decrease_stock();
```

### 3.3 포인트 잔액 업데이트

```sql
CREATE OR REPLACE FUNCTION update_point_balance()
RETURNS TRIGGER AS $$
DECLARE
    current_balance INTEGER;
BEGIN
    SELECT points INTO current_balance
    FROM reward_points
    WHERE user_id = NEW.user_id;
    
    IF NEW.type IN ('earn', 'refund') THEN
        current_balance := current_balance + NEW.amount;
    ELSIF NEW.type IN ('spend', 'expire') THEN
        current_balance := current_balance - NEW.amount;
    END IF;
    
    NEW.balance_after := current_balance;
    
    UPDATE reward_points
    SET 
        points = current_balance,
        total_earned = total_earned + CASE WHEN NEW.type = 'earn' THEN NEW.amount ELSE 0 END,
        total_spent = total_spent + CASE WHEN NEW.type = 'spend' THEN NEW.amount ELSE 0 END,
        updated_at = NOW()
    WHERE user_id = NEW.user_id;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_point_balance
BEFORE INSERT ON point_transactions
FOR EACH ROW
EXECUTE FUNCTION update_point_balance();
```

---

## 4. 인덱싱 전략

### 4.1 복합 인덱스

```sql
-- 상품 검색 최적화
CREATE INDEX idx_products_search ON products(status, is_featured, rating_average DESC, created_at DESC);

-- 주문 조회 최적화
CREATE INDEX idx_orders_buyer_status ON orders(buyer_id, status, created_at DESC);

-- 거래 분석 최적화
CREATE INDEX idx_payments_date_status ON payments(DATE(created_at), status);
```

### 4.2 전문 검색 인덱스 (Full-Text Search)

```sql
-- 상품명 전문 검색
CREATE INDEX idx_products_name_fts ON products 
USING gin(to_tsvector('korean', name_ko));

-- 상품 설명 전문 검색
CREATE INDEX idx_products_desc_fts ON products 
USING gin(to_tsvector('korean', description_ko));
```

---

## 5. 파티셔닝 전략

### 5.1 주문 테이블 월별 파티션

```sql
-- 파티션 테이블 생성
CREATE TABLE orders_partitioned (
    LIKE orders INCLUDING ALL
) PARTITION BY RANGE (created_at);

-- 월별 파티션
CREATE TABLE orders_2025_01 PARTITION OF orders_partitioned
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE orders_2025_02 PARTITION OF orders_partitioned
FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- ... 계속
```

---

## 6. 백업 및 복구 전략

### 6.1 백업 스크립트

```bash
#!/bin/bash
# daily_backup.sh

BACKUP_DIR="/backups/postgresql"
DB_NAME="spicyjump_platform"
DATE=$(date +%Y%m%d_%H%M%S)

# 전체 백업
pg_dump -U postgres -d $DB_NAME -F c -f "$BACKUP_DIR/full_${DATE}.dump"

# 스키마만 백업
pg_dump -U postgres -d $DB_NAME -s -f "$BACKUP_DIR/schema_${DATE}.sql"

# 7일 이상 된 백업 삭제
find $BACKUP_DIR -name "*.dump" -mtime +7 -delete
```

---

**문서 관리**
- 작성자: [담당자명]
- 최종 업데이트: 2025-11-19


