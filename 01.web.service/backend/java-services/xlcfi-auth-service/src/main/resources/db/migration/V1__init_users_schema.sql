-- ==========================================
-- V1: 사용자 관리 스키마 생성
-- ==========================================

-- 사용자 테이블
CREATE TABLE users (
    -- 기본키
    id BIGSERIAL PRIMARY KEY,
    
    -- 인증 정보
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),  -- OAuth의 경우 NULL 가능
    
    -- 기본 정보
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    
    -- 역할 및 상태
    role VARCHAR(20) NOT NULL DEFAULT 'BUYER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- 다국어 설정
    language VARCHAR(10) NOT NULL DEFAULT 'ko',
    
    -- 마케팅 동의
    marketing_consent BOOLEAN DEFAULT FALSE,
    
    -- 타임스탬프
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_at TIMESTAMP,
    
    -- 제약조건
    CONSTRAINT check_role CHECK (role IN ('BUYER', 'SELLER', 'ADMIN')),
    CONSTRAINT check_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT check_language CHECK (language IN ('ko', 'en'))
);

-- 인덱스
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- updated_at 자동 갱신 트리거 함수
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- users 테이블 트리거
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 주석
COMMENT ON TABLE users IS '사용자 정보 테이블';
COMMENT ON COLUMN users.email IS '이메일 (로그인 ID)';
COMMENT ON COLUMN users.password_hash IS '비밀번호 해시 (bcrypt)';
COMMENT ON COLUMN users.role IS '사용자 역할 (BUYER, SELLER, ADMIN)';
COMMENT ON COLUMN users.status IS '계정 상태 (ACTIVE, INACTIVE, SUSPENDED)';

