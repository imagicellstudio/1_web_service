-- ==========================================
-- V1: 결제 관리 스키마 생성
-- ==========================================

-- 결제 테이블
CREATE TABLE payments (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계 (외래키는 애플리케이션 레벨에서 관리)
    order_id BIGINT NOT NULL,
    
    -- 결제 금액
    amount DECIMAL(15, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- 결제 방법
    payment_method VARCHAR(50) NOT NULL,
    
    -- 결제 상태
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    
    -- PG사 정보
    pg_provider VARCHAR(50),
    pg_transaction_id VARCHAR(255),
    pg_response JSONB,
    
    -- 실패 정보
    failure_reason TEXT,
    
    -- 환불 정보
    refund_amount DECIMAL(15, 2),
    refund_reason TEXT,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    paid_at TIMESTAMP,
    failed_at TIMESTAMP,
    refunded_at TIMESTAMP,
    expires_at TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT check_payment_status CHECK (status IN (
        'PENDING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED', 'PARTIAL_REFUNDED'
    )),
    CONSTRAINT check_payment_method CHECK (payment_method IN (
        'CARD', 'BANK_TRANSFER', 'PAYPAL', 'STRIPE', 'TOSS'
    ))
);

-- 환불 이력 테이블
CREATE TABLE refunds (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계
    payment_id BIGINT NOT NULL REFERENCES payments(id) ON DELETE CASCADE,
    
    -- 환불 금액
    amount DECIMAL(15, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    
    -- 환불 타입
    refund_type VARCHAR(20) NOT NULL,
    
    -- 환불 사유
    reason TEXT NOT NULL,
    
    -- 환불 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    
    -- PG사 환불 ID
    pg_refund_id VARCHAR(255),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT check_refund_type CHECK (refund_type IN ('FULL', 'PARTIAL')),
    CONSTRAINT check_refund_status CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

-- 인덱스
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_pg_transaction_id ON payments(pg_transaction_id);
CREATE INDEX idx_payments_created_at ON payments(created_at DESC);
CREATE INDEX idx_payments_pg_provider ON payments(pg_provider);

-- 환불 인덱스
CREATE INDEX idx_refunds_payment_id ON refunds(payment_id);
CREATE INDEX idx_refunds_status ON refunds(status);
CREATE INDEX idx_refunds_created_at ON refunds(created_at DESC);

-- 복합 인덱스
CREATE INDEX idx_payments_status_created ON payments(status, created_at DESC);

-- 유니크 인덱스 (PG 트랜잭션 ID 중복 방지)
CREATE UNIQUE INDEX idx_payments_pg_transaction_unique ON payments(pg_transaction_id) 
    WHERE pg_transaction_id IS NOT NULL;

-- 주석
COMMENT ON TABLE payments IS '결제 정보 테이블';
COMMENT ON COLUMN payments.order_id IS '주문 ID (orders 테이블 참조)';
COMMENT ON COLUMN payments.payment_method IS '결제 수단 (CARD, BANK_TRANSFER, PAYPAL, STRIPE, TOSS)';
COMMENT ON COLUMN payments.status IS '결제 상태';
COMMENT ON COLUMN payments.pg_provider IS 'PG사 (stripe, paypal, toss 등)';
COMMENT ON COLUMN payments.pg_transaction_id IS 'PG사 거래 ID';
COMMENT ON COLUMN payments.pg_response IS 'PG사 응답 전체 (JSON, 디버깅용)';

COMMENT ON TABLE refunds IS '환불 이력 테이블';
COMMENT ON COLUMN refunds.refund_type IS '환불 타입 (FULL: 전액, PARTIAL: 부분)';

