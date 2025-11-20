-- ==========================================
-- V1: 상품 관리 스키마 생성
-- ==========================================

-- 카테고리 테이블
CREATE TABLE categories (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 계층 구조
    parent_id BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    
    -- 카테고리명
    name VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    
    -- 정렬 순서
    sort_order INTEGER NOT NULL DEFAULT 0,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 카테고리 인덱스
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_sort_order ON categories(sort_order);

-- 상품 테이블
CREATE TABLE products (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계 (외래키는 애플리케이션 레벨에서 관리)
    seller_id BIGINT NOT NULL,
    category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    
    -- 상품 정보 (다국어)
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500),
    description TEXT,
    description_en TEXT,
    
    -- 가격 및 재고
    price DECIMAL(15, 2) NOT NULL CHECK (price >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    
    -- 이미지 (JSON 배열)
    images JSONB,
    
    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    
    -- 통계
    view_count INTEGER NOT NULL DEFAULT 0,
    rating_average DECIMAL(3, 2) DEFAULT 0,
    review_count INTEGER NOT NULL DEFAULT 0,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT check_product_status CHECK (status IN ('DRAFT', 'PUBLISHED', 'SOLDOUT', 'DISCONTINUED')),
    CONSTRAINT check_currency CHECK (currency IN ('USD', 'KRW', 'EUR', 'JPY', 'CNY'))
);

-- 상품 인덱스
CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_created_at ON products(created_at DESC);
CREATE INDEX idx_products_rating ON products(rating_average DESC);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_deleted_at ON products(deleted_at);

-- 복합 인덱스
CREATE INDEX idx_products_category_status ON products(category_id, status);
CREATE INDEX idx_products_status_created ON products(status, created_at DESC);

-- 전문 검색 인덱스 (상품명)
CREATE INDEX idx_products_name_search ON products USING gin(to_tsvector('english', name));

-- updated_at 트리거
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 주석
COMMENT ON TABLE categories IS '상품 카테고리 테이블 (계층 구조)';
COMMENT ON COLUMN categories.parent_id IS '상위 카테고리 ID (NULL이면 최상위)';

COMMENT ON TABLE products IS '상품 정보 테이블';
COMMENT ON COLUMN products.seller_id IS '판매자 ID (users 테이블 참조)';
COMMENT ON COLUMN products.images IS '이미지 URL 배열 (JSON)';
COMMENT ON COLUMN products.status IS '상품 상태 (DRAFT, PUBLISHED, SOLDOUT, DISCONTINUED)';
COMMENT ON COLUMN products.deleted_at IS 'Soft Delete 시간 (NULL이면 활성)';

