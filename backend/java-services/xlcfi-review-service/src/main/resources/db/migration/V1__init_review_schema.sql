-- ==========================================
-- V1: 리뷰 관리 스키마 생성
-- ==========================================

-- 리뷰 테이블
CREATE TABLE reviews (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계 (외래키는 애플리케이션 레벨에서 관리)
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    
    -- 평점
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    
    -- 리뷰 내용
    title VARCHAR(200),
    content TEXT NOT NULL,
    
    -- 이미지 (JSON 배열)
    images JSONB,
    
    -- 구매 인증 여부
    is_verified_purchase BOOLEAN DEFAULT FALSE,
    
    -- 도움됨 카운트
    helpful_count INTEGER NOT NULL DEFAULT 0,
    
    -- 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    
    -- 감정 분석 결과 (Python ML Service에서 업데이트)
    sentiment VARCHAR(20),
    sentiment_score DECIMAL(3, 2),
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 제약조건
    CONSTRAINT check_review_status CHECK (status IN ('PUBLISHED', 'HIDDEN', 'DELETED', 'PENDING'))
);

-- 리뷰 도움됨 테이블
CREATE TABLE review_helpful (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 유니크 제약 (한 사용자당 한 번만)
    CONSTRAINT unique_review_helpful UNIQUE (review_id, user_id)
);

-- 리뷰 신고 테이블
CREATE TABLE review_reports (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 관계
    review_id BIGINT NOT NULL REFERENCES reviews(id) ON DELETE CASCADE,
    reporter_id BIGINT NOT NULL,
    
    -- 신고 사유
    reason VARCHAR(50) NOT NULL,
    description TEXT,
    
    -- 처리 상태
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    -- 처리 정보
    handled_by BIGINT,
    handled_at TIMESTAMP,
    resolution TEXT,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 제약조건
    CONSTRAINT check_report_reason CHECK (reason IN (
        'SPAM', 'INAPPROPRIATE', 'FAKE', 'OFFENSIVE', 'OTHER'
    )),
    CONSTRAINT check_report_status CHECK (status IN (
        'PENDING', 'APPROVED', 'REJECTED', 'RESOLVED'
    ))
);

-- 인덱스
CREATE INDEX idx_reviews_product_id ON reviews(product_id);
CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_order_id ON reviews(order_id);
CREATE INDEX idx_reviews_rating ON reviews(rating);
CREATE INDEX idx_reviews_created_at ON reviews(created_at DESC);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_reviews_sentiment ON reviews(sentiment);

-- 복합 인덱스
CREATE INDEX idx_reviews_product_status ON reviews(product_id, status);
CREATE INDEX idx_reviews_product_rating ON reviews(product_id, rating DESC);

-- 중복 리뷰 방지 (사용자당 상품 1개, PUBLISHED 상태만)
CREATE UNIQUE INDEX idx_reviews_unique_user_product ON reviews(product_id, user_id) 
    WHERE status = 'PUBLISHED';

-- 리뷰 도움됨 인덱스
CREATE INDEX idx_review_helpful_review_id ON review_helpful(review_id);
CREATE INDEX idx_review_helpful_user_id ON review_helpful(user_id);

-- 리뷰 신고 인덱스
CREATE INDEX idx_review_reports_review_id ON review_reports(review_id);
CREATE INDEX idx_review_reports_status ON review_reports(status);
CREATE INDEX idx_review_reports_created_at ON review_reports(created_at DESC);

-- updated_at 트리거
CREATE TRIGGER update_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 상품 평점 업데이트 함수
CREATE OR REPLACE FUNCTION update_product_rating()
RETURNS TRIGGER AS $$
DECLARE
    avg_rating DECIMAL(3,2);
    total_reviews INTEGER;
BEGIN
    -- 상품의 평균 평점 계산 (PUBLISHED 상태만)
    SELECT 
        COALESCE(AVG(rating)::DECIMAL(3,2), 0),
        COUNT(*)
    INTO avg_rating, total_reviews
    FROM reviews
    WHERE product_id = NEW.product_id 
      AND status = 'PUBLISHED';
    
    -- products 테이블 업데이트 (애플리케이션에서 처리하거나 별도 서비스 호출)
    -- 여기서는 트리거로 직접 업데이트하지 않고 이벤트 발행으로 처리
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 리뷰 생성/수정 시 상품 평점 업데이트 트리거
CREATE TRIGGER trigger_update_product_rating
    AFTER INSERT OR UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION update_product_rating();

-- 주석
COMMENT ON TABLE reviews IS '리뷰 테이블';
COMMENT ON COLUMN reviews.product_id IS '상품 ID (products 테이블 참조)';
COMMENT ON COLUMN reviews.user_id IS '작성자 ID (users 테이블 참조)';
COMMENT ON COLUMN reviews.order_id IS '주문 ID (구매 인증용)';
COMMENT ON COLUMN reviews.is_verified_purchase IS '구매 인증 여부';
COMMENT ON COLUMN reviews.sentiment IS '감정 분석 결과 (positive, neutral, negative)';
COMMENT ON COLUMN reviews.sentiment_score IS '감정 분석 신뢰도 (0-1)';

COMMENT ON TABLE review_helpful IS '리뷰 도움됨 테이블';
COMMENT ON TABLE review_reports IS '리뷰 신고 테이블';

