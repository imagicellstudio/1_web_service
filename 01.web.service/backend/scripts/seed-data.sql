-- ============================================
-- XLCfi Platform - Seed Data Script
-- ============================================
-- Purpose: Insert initial test data for development
-- Usage: Run this script after Flyway migrations
-- Example: psql -U xlcfi_user -d xlcfi_db -f seed-data.sql
-- ============================================

\c xlcfi_db

-- ============================================
-- 1. Insert Test Users
-- ============================================
-- Note: Passwords are hashed using bcrypt
-- Plain password for all test users: "password123"
-- Hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (email, password_hash, name, phone, role, status, language, created_at, updated_at) VALUES
-- Admin user
('admin@xlcfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', '010-1111-1111', 'ADMIN', 'ACTIVE', 'KO', NOW(), NOW()),

-- Seller users
('seller1@xlcfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '김판매', '010-2222-2222', 'SELLER', 'ACTIVE', 'KO', NOW(), NOW()),
('seller2@xlcfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'John Seller', '010-2222-3333', 'SELLER', 'ACTIVE', 'EN', NOW(), NOW()),

-- Buyer users
('buyer1@xlcfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '이구매', '010-3333-3333', 'BUYER', 'ACTIVE', 'KO', NOW(), NOW()),
('buyer2@xlcfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Jane Buyer', '010-3333-4444', 'BUYER', 'ACTIVE', 'EN', NOW(), NOW()),
('buyer3@xlcfi.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '박소비', '010-3333-5555', 'BUYER', 'INACTIVE', 'KO', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- ============================================
-- 2. Insert Sample Products
-- ============================================

INSERT INTO products (seller_id, category_id, name, name_en, description, description_en, price, currency, stock_quantity, images, status, view_count, rating_average, review_count, created_at, updated_at) VALUES
-- 고추장 (Gochujang)
(2, 1, '전통 고추장 500g', 'Traditional Gochujang 500g', 
'100% 국산 재료로 만든 전통 고추장입니다. HACCP 인증을 받았으며, 원산지는 한국입니다.',
'Traditional Korean Gochujang made with 100% Korean ingredients. HACCP certified.',
12000, 'KRW', 100, 
'["https://example.com/images/gochujang1.jpg", "https://example.com/images/gochujang2.jpg"]'::jsonb,
'PUBLISHED', 250, 4.5, 15, NOW(), NOW()),

-- 김치 (Kimchi)
(2, 2, '포기김치 1kg', 'Whole Cabbage Kimchi 1kg',
'신선한 배추로 만든 전통 포기김치. 원산지: 국산 배추 100%',
'Traditional Korean Kimchi made with fresh cabbage. Origin: 100% Korean cabbage',
15000, 'KRW', 50,
'["https://example.com/images/kimchi1.jpg"]'::jsonb,
'PUBLISHED', 180, 4.8, 22, NOW(), NOW()),

-- 된장 (Doenjang)
(3, 1, '3년 숙성 된장 1kg', '3-Year Aged Doenjang 1kg',
'3년 동안 발효시킨 전통 된장. 깊은 맛이 특징입니다.',
'Traditional Korean soybean paste aged for 3 years. Rich and deep flavor.',
25000, 'KRW', 30,
'["https://example.com/images/doenjang1.jpg", "https://example.com/images/doenjang2.jpg"]'::jsonb,
'PUBLISHED', 95, 4.7, 8, NOW(), NOW()),

-- 고춧가루 (Red Pepper Powder)
(2, 3, '태양초 고춧가루 500g', 'Sun-dried Red Pepper Powder 500g',
'햇볕에 말린 태양초로 만든 고춧가루. HACCP 인증',
'Red pepper powder made from sun-dried peppers. HACCP certified',
18000, 'KRW', 120,
'["https://example.com/images/pepper-powder1.jpg"]'::jsonb,
'PUBLISHED', 320, 4.6, 28, NOW(), NOW()),

-- 잡채 (Japchae)
(3, 4, '즉석 잡채 300g', 'Ready-to-eat Japchae 300g',
'간편하게 데워먹는 잡채. 냉장 보관',
'Convenient ready-to-eat Japchae. Keep refrigerated',
8000, 'KRW', 80,
'["https://example.com/images/japchae1.jpg"]'::jsonb,
'PUBLISHED', 145, 4.2, 12, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ============================================
-- 3. Insert Sample Reviews
-- ============================================

INSERT INTO reviews (product_id, user_id, rating, title, content, images, is_verified_purchase, status, created_at, updated_at) VALUES
-- 고추장 리뷰
(1, 4, 5, '정말 맛있어요!', '엄마가 만든 맛이 나요. 강추합니다!', 
'["https://example.com/reviews/review1.jpg"]'::jsonb, TRUE, 'PUBLISHED', NOW(), NOW()),
(1, 5, 4, 'Great taste', 'Authentic Korean flavor. Will buy again!', 
NULL, TRUE, 'PUBLISHED', NOW(), NOW()),

-- 김치 리뷰
(2, 4, 5, '신선해요', '배송도 빠르고 김치가 아주 신선합니다.', 
NULL, TRUE, 'PUBLISHED', NOW(), NOW()),
(2, 5, 5, 'Best Kimchi ever', 'This is the best kimchi I have ever tasted!', 
'["https://example.com/reviews/review2.jpg"]'::jsonb, TRUE, 'PUBLISHED', NOW(), NOW()),

-- 된장 리뷰
(3, 4, 5, '진한 맛', '3년 숙성이라 그런지 맛이 정말 진해요.', 
NULL, TRUE, 'PUBLISHED', NOW(), NOW())
ON CONFLICT DO NOTHING;

-- ============================================
-- 4. Display Success Message
-- ============================================

SELECT 'Seed data inserted successfully!' AS status;
SELECT 'Test Users:' AS info;
SELECT email, name, role FROM users ORDER BY role, email;
SELECT '' AS separator;
SELECT 'Test Products:' AS info;
SELECT id, name, name_en, price, currency, stock_quantity FROM products ORDER BY id;

