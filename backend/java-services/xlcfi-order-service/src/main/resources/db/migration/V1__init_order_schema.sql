-- ==========================================
-- V1: 주문 관리 스키마 생성
-- ==========================================

-- 주문 테이블
CREATE TABLE orders (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 주문번호 (표시용)
    order_number VARCHAR(50) UNIQUE NOT NULL,
    
    -- 관계 (외래키는 애플리케이션 레벨에서 관리)
    buyer_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    
    -- 금액
    subtotal DECIMAL(15, 2) NOT NULL CHECK (subtotal >= 0),
    shipping_fee DECIMAL(15, 2) NOT NULL DEFAULT 0,
    tax DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total DECIMAL(15, 2) NOT NULL CHECK (total >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- 배송 정보 (JSON)
    shipping_address JSONB NOT NULL,
    
    -- 주문 상태
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    
    -- 메모
    buyer_note TEXT,
    seller_note TEXT,
    
    -- 배송 정보
    tracking_number VARCHAR(100),
    carrier_name VARCHAR(100),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    confirmed_at TIMESTAMP,
    preparing_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT check_order_status CHECK (status IN (
        'PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPED', 
        'DELIVERED', 'CANCELLED', 'RETURN_REQUESTED', 'RETURNED'
    ))
);

-- 주문 항목 테이블
CREATE TABLE order_items (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    
    -- 주문 시점 상품 정보 (스냅샷)
    product_name VARCHAR(500) NOT NULL,
    product_name_en VARCHAR(500),
    product_image_url TEXT,
    
    -- 수량 및 가격
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(15, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(15, 2) NOT NULL CHECK (subtotal >= 0),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- 인덱스
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_buyer_id ON orders(buyer_id);
CREATE INDEX idx_orders_seller_id ON orders(seller_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);

-- 복합 인덱스
CREATE INDEX idx_orders_buyer_status ON orders(buyer_id, status);
CREATE INDEX idx_orders_seller_status ON orders(seller_id, status);
CREATE INDEX idx_orders_status_created ON orders(status, created_at DESC);

-- 주문 항목 인덱스
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

-- updated_at 트리거
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 주문번호 생성 함수
CREATE OR REPLACE FUNCTION generate_order_number()
RETURNS TEXT AS $$
DECLARE
    new_number TEXT;
    counter INTEGER;
BEGIN
    SELECT COUNT(*) + 1 INTO counter 
    FROM orders 
    WHERE DATE(created_at) = CURRENT_DATE;
    
    new_number := 'ORD-' || TO_CHAR(CURRENT_DATE, 'YYYYMMDD') || '-' || LPAD(counter::TEXT, 4, '0');
    
    RETURN new_number;
END;
$$ LANGUAGE plpgsql;

-- 주석
COMMENT ON TABLE orders IS '주문 정보 테이블';
COMMENT ON COLUMN orders.order_number IS '주문번호 (표시용, 예: ORD-20251120-0001)';
COMMENT ON COLUMN orders.buyer_id IS '구매자 ID (users 테이블 참조)';
COMMENT ON COLUMN orders.seller_id IS '판매자 ID (users 테이블 참조)';
COMMENT ON COLUMN orders.shipping_address IS '배송지 정보 (JSON)';
COMMENT ON COLUMN orders.status IS '주문 상태';

COMMENT ON TABLE order_items IS '주문 항목 테이블';
COMMENT ON COLUMN order_items.product_name IS '주문 시점의 상품명 (스냅샷)';

