-- ==========================================
-- NFT 관련 데이터베이스 스키마
-- ==========================================

-- 1. 원산지 인증 NFT
CREATE TABLE origin_certificate_nfts (
    id BIGSERIAL PRIMARY KEY,
    
    -- NFT 정보
    token_id BIGINT NOT NULL UNIQUE,
    contract_address VARCHAR(42) NOT NULL,
    
    -- 상품 정보
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    
    -- 농장 정보
    farm_name VARCHAR(255) NOT NULL,
    location TEXT NOT NULL,
    farmer_name VARCHAR(100) NOT NULL,
    farmer_address VARCHAR(42),        -- 블록체인 주소
    
    -- 인증 정보
    harvest_date DATE NOT NULL,
    haccp_certified BOOLEAN DEFAULT FALSE,
    organic_certified BOOLEAN DEFAULT FALSE,
    food_code VARCHAR(50),
    
    -- 인증서 이미지 (IPFS)
    certification_images JSONB,        -- ["ipfs://...", "ipfs://..."]
    
    -- 메타데이터
    token_uri TEXT NOT NULL,           -- IPFS URI
    metadata JSONB,
    
    -- 발행 정보
    issuer_address VARCHAR(42) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    
    -- 상태
    is_active BOOLEAN DEFAULT TRUE,
    revoked_at TIMESTAMP,
    revoke_reason TEXT,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    INDEX idx_product_id (product_id),
    INDEX idx_token_id (token_id),
    INDEX idx_farmer_address (farmer_address),
    INDEX idx_is_active (is_active),
    
    -- 외래키
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 2. 레시피 NFT
CREATE TABLE recipe_nfts (
    id BIGSERIAL PRIMARY KEY,
    
    -- NFT 정보
    token_id BIGINT NOT NULL UNIQUE,
    contract_address VARCHAR(42) NOT NULL,
    
    -- 레시피 정보
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,     -- 한식, 중식, 양식, 일식 등
    difficulty VARCHAR(20) NOT NULL,   -- 쉬움, 보통, 어려움
    cooking_time INTEGER NOT NULL,     -- 분
    servings INTEGER NOT NULL,         -- 인분
    
    -- 재료 및 조리법
    ingredients JSONB NOT NULL,        -- ["재료1", "재료2", ...]
    steps JSONB NOT NULL,              -- ["단계1", "단계2", ...]
    
    -- 미디어
    image_uri TEXT NOT NULL,           -- IPFS
    video_uri TEXT,                    -- IPFS (선택)
    
    -- 크리에이터 정보
    creator_id BIGINT NOT NULL,
    creator_address VARCHAR(42) NOT NULL,
    
    -- 가격 및 로열티
    price DECIMAL(30, 18) NOT NULL,    -- XLCFI Token
    royalty_percentage INTEGER NOT NULL, -- basis points (500 = 5%)
    
    -- 판매 정보
    is_for_sale BOOLEAN DEFAULT TRUE,
    total_sales INTEGER DEFAULT 0,
    total_revenue DECIMAL(30, 18) DEFAULT 0,
    
    -- 메타데이터
    token_uri TEXT NOT NULL,
    metadata JSONB,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    INDEX idx_token_id (token_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_creator_address (creator_address),
    INDEX idx_category (category),
    INDEX idx_is_for_sale (is_for_sale),
    
    -- 외래키
    FOREIGN KEY (creator_id) REFERENCES users(id)
);

-- 3. 레시피 NFT 판매 이력
CREATE TABLE recipe_nft_sales (
    id BIGSERIAL PRIMARY KEY,
    
    -- NFT 정보
    recipe_nft_id BIGINT NOT NULL,
    token_id BIGINT NOT NULL,
    
    -- 거래 정보
    seller_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    price DECIMAL(30, 18) NOT NULL,
    
    -- 로열티 정보
    royalty_amount DECIMAL(30, 18) NOT NULL,
    creator_royalty DECIMAL(30, 18) NOT NULL,
    platform_fee DECIMAL(30, 18) NOT NULL,
    
    -- 블록체인 정보
    transaction_hash VARCHAR(66) NOT NULL,
    block_number BIGINT,
    
    -- 타임스탬프
    sold_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    INDEX idx_recipe_nft_id (recipe_nft_id),
    INDEX idx_seller_id (seller_id),
    INDEX idx_buyer_id (buyer_id),
    INDEX idx_sold_at (sold_at),
    
    -- 외래키
    FOREIGN KEY (recipe_nft_id) REFERENCES recipe_nfts(id),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id)
);

-- 4. 멤버십 NFT
CREATE TABLE membership_nfts (
    id BIGSERIAL PRIMARY KEY,
    
    -- NFT 정보
    token_id BIGINT NOT NULL UNIQUE,
    contract_address VARCHAR(42) NOT NULL,
    
    -- 사용자 정보
    user_id BIGINT NOT NULL UNIQUE,    -- 한 사용자당 하나의 멤버십
    holder_address VARCHAR(42) NOT NULL,
    
    -- 등급 정보
    tier VARCHAR(20) NOT NULL,         -- BRONZE, SILVER, GOLD, PLATINUM, DIAMOND
    
    -- 혜택 정보
    discount_rate INTEGER NOT NULL,    -- basis points (5000 = 50%)
    priority_access BOOLEAN DEFAULT FALSE,
    exclusive_community BOOLEAN DEFAULT FALSE,
    monthly_tokens DECIMAL(30, 18) NOT NULL,
    
    -- 유효기간
    issued_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    last_renewed_at TIMESTAMP,
    
    -- 상태
    is_active BOOLEAN DEFAULT TRUE,
    revoked_at TIMESTAMP,
    revoke_reason TEXT,
    
    -- 메타데이터
    token_uri TEXT NOT NULL,
    metadata JSONB,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    INDEX idx_token_id (token_id),
    INDEX idx_user_id (user_id),
    INDEX idx_holder_address (holder_address),
    INDEX idx_tier (tier),
    INDEX idx_is_active (is_active),
    INDEX idx_expires_at (expires_at),
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    -- 제약조건
    CONSTRAINT check_tier CHECK (tier IN ('BRONZE', 'SILVER', 'GOLD', 'PLATINUM', 'DIAMOND'))
);

-- 5. 멤버십 갱신 이력
CREATE TABLE membership_renewal_history (
    id BIGSERIAL PRIMARY KEY,
    
    -- 멤버십 정보
    membership_nft_id BIGINT NOT NULL,
    token_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    
    -- 갱신 정보
    old_expires_at TIMESTAMP NOT NULL,
    new_expires_at TIMESTAMP NOT NULL,
    renewed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 결제 정보
    payment_amount DECIMAL(30, 18),
    payment_method VARCHAR(50),        -- XLCFI, USDT, KRW
    
    -- 블록체인 정보
    transaction_hash VARCHAR(66),
    
    -- 인덱스
    INDEX idx_membership_nft_id (membership_nft_id),
    INDEX idx_user_id (user_id),
    INDEX idx_renewed_at (renewed_at),
    
    -- 외래키
    FOREIGN KEY (membership_nft_id) REFERENCES membership_nfts(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 6. 멤버십 업그레이드 이력
CREATE TABLE membership_upgrade_history (
    id BIGSERIAL PRIMARY KEY,
    
    -- 멤버십 정보
    membership_nft_id BIGINT NOT NULL,
    token_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    
    -- 업그레이드 정보
    old_tier VARCHAR(20) NOT NULL,
    new_tier VARCHAR(20) NOT NULL,
    upgraded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 결제 정보
    upgrade_cost DECIMAL(30, 18),
    payment_method VARCHAR(50),
    
    -- 블록체인 정보
    transaction_hash VARCHAR(66),
    
    -- 인덱스
    INDEX idx_membership_nft_id (membership_nft_id),
    INDEX idx_user_id (user_id),
    INDEX idx_upgraded_at (upgraded_at),
    
    -- 외래키
    FOREIGN KEY (membership_nft_id) REFERENCES membership_nfts(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 7. NFT 메타데이터 캐시 (IPFS 데이터 캐싱)
CREATE TABLE nft_metadata_cache (
    id BIGSERIAL PRIMARY KEY,
    
    -- NFT 정보
    contract_address VARCHAR(42) NOT NULL,
    token_id BIGINT NOT NULL,
    nft_type VARCHAR(50) NOT NULL,    -- ORIGIN, RECIPE, MEMBERSHIP
    
    -- 메타데이터
    token_uri TEXT NOT NULL,
    name VARCHAR(255),
    description TEXT,
    image_uri TEXT,
    attributes JSONB,
    
    -- 캐시 정보
    cached_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    UNIQUE INDEX idx_contract_token (contract_address, token_id),
    INDEX idx_nft_type (nft_type),
    
    -- 제약조건
    CONSTRAINT check_nft_type CHECK (nft_type IN ('ORIGIN', 'RECIPE', 'MEMBERSHIP'))
);

-- 8. NFT 소유권 이력 (Transfer 추적)
CREATE TABLE nft_ownership_history (
    id BIGSERIAL PRIMARY KEY,
    
    -- NFT 정보
    contract_address VARCHAR(42) NOT NULL,
    token_id BIGINT NOT NULL,
    nft_type VARCHAR(50) NOT NULL,
    
    -- 거래 정보
    from_address VARCHAR(42) NOT NULL,
    to_address VARCHAR(42) NOT NULL,
    from_user_id BIGINT,
    to_user_id BIGINT,
    
    -- 블록체인 정보
    transaction_hash VARCHAR(66) NOT NULL,
    block_number BIGINT,
    
    -- 타임스탬프
    transferred_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    INDEX idx_contract_token (contract_address, token_id),
    INDEX idx_from_address (from_address),
    INDEX idx_to_address (to_address),
    INDEX idx_from_user (from_user_id),
    INDEX idx_to_user (to_user_id),
    INDEX idx_transferred_at (transferred_at),
    
    -- 외래키
    FOREIGN KEY (from_user_id) REFERENCES users(id),
    FOREIGN KEY (to_user_id) REFERENCES users(id)
);

-- 9. NFT 좋아요/북마크
CREATE TABLE nft_favorites (
    id BIGSERIAL PRIMARY KEY,
    
    -- 사용자 정보
    user_id BIGINT NOT NULL,
    
    -- NFT 정보
    nft_type VARCHAR(50) NOT NULL,
    nft_id BIGINT NOT NULL,            -- origin_certificate_nfts.id, recipe_nfts.id, membership_nfts.id
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- 인덱스
    UNIQUE INDEX idx_user_nft (user_id, nft_type, nft_id),
    INDEX idx_user_id (user_id),
    INDEX idx_nft_type (nft_type),
    
    -- 외래키
    FOREIGN KEY (user_id) REFERENCES users(id),
    
    -- 제약조건
    CONSTRAINT check_nft_type_fav CHECK (nft_type IN ('ORIGIN', 'RECIPE', 'MEMBERSHIP'))
);

-- 10. 트리거: updated_at 자동 업데이트
CREATE OR REPLACE FUNCTION update_nft_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_origin_certificate_nfts_updated_at
    BEFORE UPDATE ON origin_certificate_nfts
    FOR EACH ROW
    EXECUTE FUNCTION update_nft_updated_at();

CREATE TRIGGER trigger_recipe_nfts_updated_at
    BEFORE UPDATE ON recipe_nfts
    FOR EACH ROW
    EXECUTE FUNCTION update_nft_updated_at();

CREATE TRIGGER trigger_membership_nfts_updated_at
    BEFORE UPDATE ON membership_nfts
    FOR EACH ROW
    EXECUTE FUNCTION update_nft_updated_at();

-- 11. 뷰: 활성 멤버십 조회
CREATE OR REPLACE VIEW active_memberships AS
SELECT 
    m.*,
    u.email,
    u.name as user_name
FROM membership_nfts m
JOIN users u ON m.user_id = u.id
WHERE m.is_active = TRUE
  AND m.expires_at > NOW();

-- 12. 뷰: 판매 중인 레시피 NFT
CREATE OR REPLACE VIEW recipes_for_sale AS
SELECT 
    r.*,
    u.name as creator_name,
    u.email as creator_email
FROM recipe_nfts r
JOIN users u ON r.creator_id = u.id
WHERE r.is_for_sale = TRUE;

-- 13. 인덱스 최적화
CREATE INDEX idx_origin_cert_product_active ON origin_certificate_nfts(product_id, is_active);
CREATE INDEX idx_recipe_creator_sale ON recipe_nfts(creator_id, is_for_sale);
CREATE INDEX idx_membership_user_active ON membership_nfts(user_id, is_active, expires_at);

