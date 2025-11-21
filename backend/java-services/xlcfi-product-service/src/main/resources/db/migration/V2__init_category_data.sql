-- ==========================================
-- V2: 카테고리 초기 데이터
-- ==========================================

-- 최상위 카테고리
INSERT INTO categories (id, parent_id, name, name_en, sort_order) VALUES
(1, NULL, '김치/반찬', 'Kimchi & Side Dishes', 1),
(2, NULL, '장류/양념', 'Sauces & Seasonings', 2),
(3, NULL, '면류', 'Noodles', 3),
(4, NULL, '과자/간식', 'Snacks', 4),
(5, NULL, '음료', 'Beverages', 5),
(6, NULL, '가공식품', 'Processed Foods', 6),
(7, NULL, '쌀/곡물', 'Rice & Grains', 7),
(8, NULL, '해산물', 'Seafood', 8),
(9, NULL, '육류', 'Meat', 9),
(10, NULL, '과일/채소', 'Fruits & Vegetables', 10);

-- 하위 카테고리 (김치/반찬)
INSERT INTO categories (id, parent_id, name, name_en, sort_order) VALUES
(11, 1, '배추김치', 'Cabbage Kimchi', 1),
(12, 1, '깍두기', 'Radish Kimchi', 2),
(13, 1, '열무김치', 'Young Radish Kimchi', 3),
(14, 1, '나박김치', 'Water Kimchi', 4),
(15, 1, '반찬류', 'Side Dishes', 5);

-- 하위 카테고리 (장류/양념)
INSERT INTO categories (id, parent_id, name, name_en, sort_order) VALUES
(21, 2, '고추장', 'Gochujang', 1),
(22, 2, '된장', 'Doenjang', 2),
(23, 2, '간장', 'Soy Sauce', 3),
(24, 2, '쌈장', 'Ssamjang', 4),
(25, 2, '참기름', 'Sesame Oil', 5);

-- 하위 카테고리 (면류)
INSERT INTO categories (id, parent_id, name, name_en, sort_order) VALUES
(31, 3, '라면', 'Instant Noodles', 1),
(32, 3, '냉면', 'Cold Noodles', 2),
(33, 3, '짜장면', 'Jajangmyeon', 3),
(34, 3, '우동', 'Udon', 4),
(35, 3, '국수', 'Noodles', 5);

-- 하위 카테고리 (과자/간식)
INSERT INTO categories (id, parent_id, name, name_en, sort_order) VALUES
(41, 4, '과자', 'Snacks', 1),
(42, 4, '초콜릿', 'Chocolate', 2),
(43, 4, '젤리', 'Jelly', 3),
(44, 4, '견과류', 'Nuts', 4),
(45, 4, '건조과일', 'Dried Fruits', 5);

-- ID 시퀀스 조정
SELECT setval('categories_id_seq', 100, false);

