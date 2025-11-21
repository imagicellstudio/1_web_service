# 지능형 정보지원 평가 시스템

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0 (플랫폼 - 정보지원 확장)
- 대상: 생산자-소비자 연결 및 비즈니스 정보 제공

---

## 1. 시스템 개요

### 1.1 기본 개념 전환

**기존 게시판** → **지능형 정보지원 플랫폼**

| 구분 | 기존 게시판 | 지능형 정보지원 시스템 |
|------|-----------|-------------------|
| 목적 | 소통 | 소통 + 비즈니스 정보 제공 |
| 댓글 | 단순 의견 | 의견 + 판매처/구매처 정보 |
| 기능 | 글 작성/읽기 | 작성 + AI 분석 + 정보 추천 |
| 가치 | 커뮤니티 | 실질적 비즈니스 연결 |

### 1.2 핵심 정보지원 기능

```
게시글 작성 (로컬 재배 식품 소개)
         ↓
┌────────────────────────────────────────────────┐
│        AI 분석 및 정보 추출                      │
│  - 식품 종류 인식                               │
│  - 원산지 정보 추출                             │
│  - 생산기간/유통기한 파싱                        │
│  - 이미지에서 품질 분석                          │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│        자동 정보 매칭 및 제공                    │
│  ① 판매 가능 장소 추천                          │
│  ② 원재료 공급처 정보                           │
│  ③ 시장가격 정보 (지역/시간별)                  │
│  ④ 잠재 구매자 연결 (급식소, 식당 등)           │
└────────────────────────────────────────────────┘
         ↓
┌────────────────────────────────────────────────┐
│        댓글 및 정보 제공                         │
│  - 일반 댓글                                    │
│  - 정보 제공 댓글 (판매처, 공급처)              │
│  - 가격 정보 댓글                               │
│  - 구매 의향 댓글 (급식소, 바이어)              │
└────────────────────────────────────────────────┘
```

---

## 2. 확장된 데이터베이스 스키마

### 2.1 게시글 확장 (생산자 정보 포함)

```sql
-- 생산자 게시글 (확장)
CREATE TABLE producer_posts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    
    -- 기본 정보
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    images JSONB,
    videos JSONB,
    
    -- 식품 정보
    food_type VARCHAR(200), -- AI 자동 분석
    food_category_id INTEGER REFERENCES categories(id),
    quantity DECIMAL(10, 2), -- 생산량
    quantity_unit VARCHAR(20), -- kg, 톤, 박스 등
    
    -- 생산 정보
    production_location JSONB, -- {address, city, region, coordinates}
    production_date DATE, -- 생산/수확일
    harvest_season VARCHAR(50), -- 수확 시기
    expiry_date DATE, -- 유통기한
    storage_condition VARCHAR(100), -- 보관 조건
    
    -- 원산지 정보
    origin_country VARCHAR(2),
    origin_region VARCHAR(200),
    is_local BOOLEAN DEFAULT TRUE,
    is_organic BOOLEAN DEFAULT FALSE,
    certifications JSONB, -- [HACCP, 유기농, GAP 등]
    
    -- 가격 정보
    price_per_unit DECIMAL(10, 2),
    price_currency VARCHAR(3) DEFAULT 'KRW',
    price_negotiable BOOLEAN DEFAULT FALSE,
    
    -- 판매 의향
    is_for_sale BOOLEAN DEFAULT FALSE,
    sale_type VARCHAR(50), -- wholesale, retail, both
    min_order_quantity DECIMAL(10, 2),
    
    -- AI 분석 결과
    ai_analysis JSONB, -- {quality_score, freshness, market_demand}
    extracted_info JSONB, -- AI가 추출한 정보
    
    -- 매칭 정보
    matched_buyers JSONB, -- 잠재 구매자 목록
    matched_suppliers JSONB, -- 관련 공급처 목록
    market_price_info JSONB, -- 시장가격 정보
    
    -- 상태
    status VARCHAR(20) DEFAULT 'published',
    view_count INTEGER DEFAULT 0,
    inquiry_count INTEGER DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_producer_posts_food_type ON producer_posts(food_type);
CREATE INDEX idx_producer_posts_location ON producer_posts USING gin(production_location);
CREATE INDEX idx_producer_posts_is_for_sale ON producer_posts(is_for_sale);

-- 댓글 확장 (정보 제공 타입 추가)
CREATE TABLE enhanced_comments (
    id SERIAL PRIMARY KEY,
    post_id INTEGER REFERENCES producer_posts(id) ON DELETE CASCADE,
    parent_id INTEGER REFERENCES enhanced_comments(id) ON DELETE CASCADE,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    
    content TEXT NOT NULL,
    
    -- 댓글 타입
    comment_type VARCHAR(50) DEFAULT 'general', 
    -- general: 일반 댓글
    -- sales_channel: 판매처 정보
    -- supplier_info: 공급처 정보
    -- price_info: 가격 정보
    -- buyer_inquiry: 구매 문의
    -- advice: 조언/팁
    
    -- 정보 제공 (타입별 데이터)
    info_data JSONB,
    /*
    sales_channel: {
        name: "농협 하나로마트",
        contact: "02-1234-5678",
        address: "서울시 강남구...",
        commission: 15,
        requirements: ["HACCP 필수", "최소 100kg"]
    }
    supplier_info: {
        name: "씨앗 전문점",
        product: "토마토 씨앗",
        price: 50000,
        contact: "010-1234-5678"
    }
    price_info: {
        region: "서울 가락시장",
        date: "2025-11-19",
        price_range: {min: 3000, max: 5000},
        unit: "kg",
        source: "농산물유통정보"
    }
    buyer_inquiry: {
        organization: "서울초등학교",
        quantity: 500,
        unit: "kg",
        frequency: "weekly",
        contact: "02-9876-5432"
    }
    */
    
    -- 검증
    is_verified BOOLEAN DEFAULT FALSE, -- 정보 검증 여부
    verified_by INTEGER REFERENCES users(id),
    verified_at TIMESTAMP,
    
    -- 유용성
    helpful_count INTEGER DEFAULT 0,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_enhanced_comments_post_id ON enhanced_comments(post_id);
CREATE INDEX idx_enhanced_comments_type ON enhanced_comments(comment_type);

-- 판매처 정보 DB
CREATE TABLE sales_channels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50), -- retail, wholesale, online, restaurant, school
    
    -- 연락처
    contact_person VARCHAR(200),
    phone VARCHAR(20),
    email VARCHAR(200),
    website VARCHAR(500),
    
    -- 위치
    address TEXT,
    city VARCHAR(100),
    region VARCHAR(100),
    country VARCHAR(2),
    coordinates JSONB, -- {lat, lng}
    
    -- 거래 조건
    commission_rate DECIMAL(5, 2), -- 수수료율
    min_order_quantity DECIMAL(10, 2),
    payment_terms VARCHAR(100), -- 결제 조건
    delivery_terms VARCHAR(100),
    requirements JSONB, -- ["HACCP 필수", "유기농 인증"]
    
    -- 취급 품목
    product_categories JSONB, -- 취급하는 식품 카테고리
    
    -- 평가
    rating_average DECIMAL(3, 2),
    review_count INTEGER DEFAULT 0,
    
    -- 상태
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_sales_channels_type ON sales_channels(type);
CREATE INDEX idx_sales_channels_region ON sales_channels(region);

-- 원재료 공급처 정보
CREATE TABLE material_suppliers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50), -- seed, fertilizer, equipment, packaging
    
    -- 연락처
    contact_person VARCHAR(200),
    phone VARCHAR(20),
    email VARCHAR(200),
    
    -- 위치
    address TEXT,
    region VARCHAR(100),
    
    -- 제공 품목
    products JSONB, 
    /*
    [{
        name: "토마토 씨앗",
        variety: "대추방울토마토",
        price: 50000,
        unit: "1kg",
        min_order: 10
    }]
    */
    
    -- 평가
    rating_average DECIMAL(3, 2),
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 시장 가격 정보
CREATE TABLE market_prices (
    id SERIAL PRIMARY KEY,
    food_type VARCHAR(200) NOT NULL,
    food_variety VARCHAR(200), -- 품종
    
    -- 가격 정보
    price_min DECIMAL(10, 2),
    price_max DECIMAL(10, 2),
    price_average DECIMAL(10, 2),
    unit VARCHAR(20), -- kg, 박스, 단 등
    
    -- 위치
    market_name VARCHAR(200), -- 가락시장, 노량진수산시장 등
    region VARCHAR(100),
    city VARCHAR(100),
    
    -- 시간
    price_date DATE NOT NULL,
    price_time TIME,
    season VARCHAR(20), -- spring, summer, fall, winter
    
    -- 출처
    data_source VARCHAR(200), -- 농산물유통정보, KAMIS 등
    
    -- 추가 정보
    supply_level VARCHAR(20), -- high, medium, low (공급량)
    demand_level VARCHAR(20), -- high, medium, low (수요)
    quality_grade VARCHAR(20), -- 상, 중, 하
    
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_market_prices_food ON market_prices(food_type, price_date);
CREATE INDEX idx_market_prices_region ON market_prices(region, price_date);
CREATE INDEX idx_market_prices_date ON market_prices(price_date DESC);

-- 잠재 구매자 (급식소, 식당 등)
CREATE TABLE potential_buyers (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50), -- school, restaurant, hotel, hospital, catering
    
    -- 연락처
    contact_person VARCHAR(200),
    phone VARCHAR(20),
    email VARCHAR(200),
    
    -- 위치
    address TEXT,
    city VARCHAR(100),
    region VARCHAR(100),
    
    -- 구매 정보
    purchase_frequency VARCHAR(50), -- daily, weekly, monthly
    average_order_volume DECIMAL(10, 2),
    preferred_categories JSONB, -- 선호하는 식품 카테고리
    
    -- 요구사항
    requirements JSONB, -- ["HACCP 필수", "로컬 식품 우선"]
    budget_range JSONB, -- {min, max}
    
    -- 상태
    is_active BOOLEAN DEFAULT TRUE,
    is_verified BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_potential_buyers_type ON potential_buyers(type);
CREATE INDEX idx_potential_buyers_region ON potential_buyers(region);

-- 정보 제공 보상 (정보 제공자에게 포인트)
CREATE TABLE info_contribution_rewards (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    comment_id INTEGER REFERENCES enhanced_comments(id),
    contribution_type VARCHAR(50), -- sales_channel, supplier_info, price_info, buyer_inquiry
    points_earned INTEGER,
    verification_status VARCHAR(20), -- pending, verified, rejected
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 3. AI 분석 및 정보 추출

### 3.1 게시글 AI 분석 시스템

```python
# ai_analysis.py
from transformers import pipeline
import cv2
from geopy.geocoders import Nominatim

class PostAnalyzer:
    """게시글 AI 분석"""
    
    def __init__(self):
        self.nlp = pipeline("ner", model="korean-ner-model")
        self.image_classifier = pipeline("image-classification")
        self.geolocator = Nominatim(user_agent="spicyjump")
    
    async def analyze_post(self, post: ProducerPostCreate):
        """게시글 종합 분석"""
        
        analysis_result = {
            "food_info": await self.extract_food_info(post.content, post.images),
            "location_info": await self.extract_location_info(post.content),
            "production_info": await self.extract_production_info(post.content),
            "quality_assessment": await self.assess_quality(post.images),
            "market_matching": await self.match_market_info(post)
        }
        
        return analysis_result
    
    async def extract_food_info(self, content: str, images: List[str]):
        """식품 정보 추출"""
        
        # 텍스트에서 식품명 추출
        entities = self.nlp(content)
        food_items = [e['word'] for e in entities if e['entity'] == 'FOOD']
        
        # 이미지 분석 (식품 종류 판별)
        image_results = []
        for img_url in images[:3]:  # 최대 3장 분석
            img = download_image(img_url)
            result = self.image_classifier(img)
            image_results.append(result[0])
        
        return {
            "detected_foods": food_items,
            "image_classification": image_results,
            "primary_food": food_items[0] if food_items else None
        }
    
    async def extract_location_info(self, content: str):
        """위치 정보 추출"""
        
        # NER로 지명 추출
        entities = self.nlp(content)
        locations = [e['word'] for e in entities if e['entity'] == 'LOCATION']
        
        if not locations:
            return None
        
        # 지오코딩
        location = self.geolocator.geocode(locations[0])
        
        if location:
            return {
                "address": location.address,
                "coordinates": {
                    "lat": location.latitude,
                    "lng": location.longitude
                },
                "city": extract_city(location.address),
                "region": extract_region(location.address)
            }
        
        return None
    
    async def extract_production_info(self, content: str):
        """생산 정보 추출 (날짜, 기간, 수량 등)"""
        
        import re
        from dateutil import parser
        
        # 날짜 패턴 추출
        date_patterns = re.findall(r'\d{4}[-./]\d{1,2}[-./]\d{1,2}', content)
        dates = [parser.parse(d) for d in date_patterns]
        
        # 수량 정보 추출
        quantity_patterns = re.findall(r'(\d+(?:\.\d+)?)\s*(kg|톤|박스|개)', content)
        
        # 기간 정보 추출
        period_keywords = ['수확', '재배', '생산']
        periods = []
        for keyword in period_keywords:
            if keyword in content:
                # 해당 키워드 주변 날짜 정보 추출
                pass
        
        return {
            "dates": dates,
            "quantities": [{"value": q[0], "unit": q[1]} for q in quantity_patterns],
            "production_period": periods
        }
    
    async def assess_quality(self, images: List[str]):
        """이미지 기반 품질 평가"""
        
        if not images:
            return None
        
        quality_scores = []
        
        for img_url in images[:3]:
            img = download_image(img_url)
            
            # 신선도 평가 (색상, 형태 분석)
            freshness = analyze_freshness(img)
            
            # 품질 점수
            quality_score = calculate_quality_score(img)
            
            quality_scores.append({
                "freshness": freshness,
                "quality_score": quality_score
            })
        
        avg_quality = sum(q['quality_score'] for q in quality_scores) / len(quality_scores)
        
        return {
            "average_quality": avg_quality,
            "individual_scores": quality_scores,
            "recommendation": get_quality_recommendation(avg_quality)
        }
    
    async def match_market_info(self, post: ProducerPostCreate):
        """시장 정보 매칭"""
        
        # 1. 현재 시장 가격 조회
        market_prices = await get_market_prices(
            food_type=post.food_type,
            region=post.location_info.get('region')
        )
        
        # 2. 판매 가능 채널 추천
        sales_channels = await recommend_sales_channels(
            food_type=post.food_type,
            quantity=post.quantity,
            location=post.location_info
        )
        
        # 3. 잠재 구매자 매칭
        potential_buyers = await match_potential_buyers(
            food_type=post.food_type,
            quantity=post.quantity,
            location=post.location_info
        )
        
        return {
            "market_prices": market_prices,
            "recommended_channels": sales_channels,
            "potential_buyers": potential_buyers
        }
```

---

## 4. 정보 제공 API

### 4.1 게시글 작성 시 자동 정보 제공

```python
# producer_posts_api.py
@router.post("/producer-posts")
async def create_producer_post(
    post: ProducerPostCreate,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """생산자 게시글 작성 (AI 분석 포함)"""
    
    # 1. AI 분석
    analyzer = PostAnalyzer()
    analysis = await analyzer.analyze_post(post)
    
    # 2. 게시글 저장
    post_id = await db.fetch_val(
        """
        INSERT INTO producer_posts (
            user_id, title, content, images, videos,
            food_type, quantity, quantity_unit,
            production_location, production_date, expiry_date,
            origin_region, is_local, is_organic, certifications,
            price_per_unit, is_for_sale, sale_type,
            ai_analysis, extracted_info
        ) VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14, $15, $16, $17, $18, $19, $20)
        RETURNING id
        """,
        current_user.id, post.title, post.content,
        json.dumps(post.images), json.dumps(post.videos),
        analysis['food_info']['primary_food'],
        post.quantity, post.quantity_unit,
        json.dumps(analysis['location_info']),
        post.production_date, post.expiry_date,
        analysis['location_info'].get('region'),
        post.is_local, post.is_organic, json.dumps(post.certifications),
        post.price_per_unit, post.is_for_sale, post.sale_type,
        json.dumps(analysis), json.dumps(analysis)
    )
    
    # 3. 시장 정보 매칭
    matched_info = await match_and_store_info(db, post_id, analysis)
    
    # 4. 자동 정보 댓글 생성
    await create_auto_info_comments(db, post_id, matched_info)
    
    # 5. 알림 발송 (잠재 구매자에게)
    await notify_potential_buyers(matched_info['potential_buyers'], post_id)
    
    return {
        "id": post_id,
        "analysis": analysis,
        "matched_info": matched_info,
        "message": "게시글이 작성되었으며, 관련 정보가 자동으로 제공되었습니다."
    }

async def create_auto_info_comments(
    db: Session,
    post_id: int,
    matched_info: dict
):
    """자동 정보 댓글 생성"""
    
    system_user_id = 1  # 시스템 봇 사용자
    
    # 1. 판매처 정보 댓글
    if matched_info.get('sales_channels'):
        for channel in matched_info['sales_channels'][:3]:
            await db.execute(
                """
                INSERT INTO enhanced_comments (
                    post_id, user_id, content, comment_type, info_data, is_verified
                ) VALUES ($1, $2, $3, $4, $5, TRUE)
                """,
                post_id, system_user_id,
                f"💼 추천 판매처: {channel['name']}",
                'sales_channel',
                json.dumps(channel)
            )
    
    # 2. 시장 가격 정보 댓글
    if matched_info.get('market_prices'):
        price_info = matched_info['market_prices'][0]
        await db.execute(
            """
            INSERT INTO enhanced_comments (
                post_id, user_id, content, comment_type, info_data, is_verified
            ) VALUES ($1, $2, $3, $4, $5, TRUE)
            """,
            post_id, system_user_id,
            f"📊 현재 시장가격: {price_info['market_name']} {price_info['price_average']:,}원/{price_info['unit']}",
            'price_info',
            json.dumps(price_info)
        )
    
    # 3. 잠재 구매자 정보 댓글
    if matched_info.get('potential_buyers'):
        for buyer in matched_info['potential_buyers'][:2]:
            await db.execute(
                """
                INSERT INTO enhanced_comments (
                    post_id, user_id, content, comment_type, info_data, is_verified
                ) VALUES ($1, $2, $3, $4, $5, TRUE)
                """,
                post_id, system_user_id,
                f"🏫 잠재 구매처: {buyer['name']} ({buyer['type']})",
                'buyer_inquiry',
                json.dumps(buyer)
            )
```

### 4.2 판매처 정보 제공 API

```python
# sales_channels_api.py
@router.get("/sales-channels/recommend")
async def recommend_sales_channels(
    food_type: str,
    region: Optional[str] = None,
    quantity: Optional[float] = None,
    db: Session = Depends(get_db)
):
    """판매처 추천"""
    
    query = """
        SELECT * FROM sales_channels
        WHERE is_active = TRUE
        AND is_verified = TRUE
        AND $1 = ANY(product_categories)
    """
    
    params = [food_type]
    
    if region:
        query += " AND region = $2"
        params.append(region)
    
    if quantity:
        query += f" AND (min_order_quantity IS NULL OR min_order_quantity <= ${len(params) + 1})"
        params.append(quantity)
    
    query += " ORDER BY rating_average DESC LIMIT 10"
    
    channels = await db.fetch_all(query, *params)
    
    return [
        {
            **channel,
            "match_score": calculate_match_score(channel, food_type, region, quantity)
        }
        for channel in channels
    ]

@router.post("/enhanced-comments/sales-channel")
async def add_sales_channel_comment(
    post_id: int,
    channel_info: SalesChannelInfo,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """판매처 정보 댓글 작성"""
    
    comment_id = await db.execute(
        """
        INSERT INTO enhanced_comments (
            post_id, user_id, content, comment_type, info_data
        ) VALUES ($1, $2, $3, 'sales_channel', $4)
        RETURNING id
        """,
        post_id, current_user.id,
        f"💼 판매처 정보: {channel_info.name}",
        json.dumps(channel_info.dict())
    )
    
    # 정보 제공 보상 (포인트 지급)
    await grant_info_contribution_reward(
        db, current_user.id, comment_id, 'sales_channel', 50
    )
    
    return {"id": comment_id, "reward_points": 50}
```

### 4.3 원재료 공급처 정보 API

```python
# suppliers_api.py
@router.get("/material-suppliers/search")
async def search_suppliers(
    product_type: str,  # seed, fertilizer, equipment
    region: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """원재료 공급처 검색"""
    
    suppliers = await db.fetch_all(
        """
        SELECT * FROM material_suppliers
        WHERE type = $1
        AND ($2 IS NULL OR region = $2)
        AND is_active = TRUE
        ORDER BY rating_average DESC
        """,
        product_type, region
    )
    
    return suppliers

@router.post("/enhanced-comments/supplier-info")
async def add_supplier_info_comment(
    post_id: int,
    supplier_info: SupplierInfo,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """공급처 정보 댓글 작성"""
    
    comment_id = await db.execute(
        """
        INSERT INTO enhanced_comments (
            post_id, user_id, content, comment_type, info_data
        ) VALUES ($1, $2, $3, 'supplier_info', $4)
        RETURNING id
        """,
        post_id, current_user.id,
        f"🌱 원재료 공급처: {supplier_info.name} - {supplier_info.product}",
        json.dumps(supplier_info.dict())
    )
    
    # 보상
    await grant_info_contribution_reward(
        db, current_user.id, comment_id, 'supplier_info', 30
    )
    
    return {"id": comment_id, "reward_points": 30}
```

### 4.4 시장 가격 정보 API

```python
# market_prices_api.py
@router.get("/market-prices/current")
async def get_current_market_prices(
    food_type: str,
    region: Optional[str] = None,
    db: Session = Depends(get_db)
):
    """현재 시장 가격 조회"""
    
    # 최근 7일 평균
    prices = await db.fetch_all(
        """
        SELECT 
            market_name,
            region,
            AVG(price_average) as avg_price,
            MIN(price_min) as min_price,
            MAX(price_max) as max_price,
            unit,
            MAX(price_date) as latest_date
        FROM market_prices
        WHERE food_type = $1
        AND ($2 IS NULL OR region = $2)
        AND price_date >= CURRENT_DATE - INTERVAL '7 days'
        GROUP BY market_name, region, unit
        ORDER BY latest_date DESC
        """,
        food_type, region
    )
    
    return prices

@router.get("/market-prices/trend")
async def get_price_trend(
    food_type: str,
    region: str,
    days: int = 30,
    db: Session = Depends(get_db)
):
    """가격 추이 조회"""
    
    trend = await db.fetch_all(
        """
        SELECT 
            price_date,
            AVG(price_average) as avg_price,
            supply_level,
            demand_level
        FROM market_prices
        WHERE food_type = $1
        AND region = $2
        AND price_date >= CURRENT_DATE - INTERVAL '$3 days'
        GROUP BY price_date, supply_level, demand_level
        ORDER BY price_date ASC
        """,
        food_type, region, days
    )
    
    # 추세 분석
    analysis = analyze_price_trend(trend)
    
    return {
        "data": trend,
        "analysis": analysis,
        "recommendation": generate_price_recommendation(analysis)
    }

def generate_price_recommendation(analysis: dict) -> str:
    """가격 기반 추천"""
    
    if analysis['trend'] == 'increasing' and analysis['rate'] > 10:
        return "가격이 상승 추세입니다. 지금 판매하시면 좋은 가격을 받으실 수 있습니다."
    elif analysis['trend'] == 'decreasing':
        return "가격이 하락 추세입니다. 가능하면 빠른 판매를 권장드립니다."
    elif analysis['supply'] == 'low' and analysis['demand'] == 'high':
        return "공급이 부족하고 수요가 높습니다. 프리미엄 가격을 책정할 수 있습니다."
    else:
        return "시장 가격이 안정적입니다. 적정 가격으로 판매하세요."

@router.post("/enhanced-comments/price-info")
async def add_price_info_comment(
    post_id: int,
    price_info: PriceInfo,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """가격 정보 댓글 작성"""
    
    comment_id = await db.execute(
        """
        INSERT INTO enhanced_comments (
            post_id, user_id, content, comment_type, info_data
        ) VALUES ($1, $2, $3, 'price_info', $4)
        RETURNING id
        """,
        post_id, current_user.id,
        f"📊 {price_info.market_name} 시장가격: {price_info.price_range['min']:,}~{price_info.price_range['max']:,}원/{price_info.unit}",
        json.dumps(price_info.dict())
    )
    
    await grant_info_contribution_reward(
        db, current_user.id, comment_id, 'price_info', 20
    )
    
    return {"id": comment_id, "reward_points": 20}
```

### 4.5 잠재 구매자 매칭 API

```python
# buyers_matching_api.py
@router.get("/potential-buyers/match")
async def match_potential_buyers(
    food_type: str,
    region: str,
    quantity: float,
    db: Session = Depends(get_db)
):
    """잠재 구매자 매칭"""
    
    buyers = await db.fetch_all(
        """
        SELECT * FROM potential_buyers
        WHERE is_active = TRUE
        AND is_verified = TRUE
        AND region = $1
        AND $2 = ANY(preferred_categories)
        AND (average_order_volume >= $3 * 0.5)  -- 수량의 50% 이상 구매 가능
        ORDER BY average_order_volume DESC
        """,
        region, food_type, quantity
    )
    
    return [
        {
            **buyer,
            "match_score": calculate_buyer_match_score(buyer, food_type, quantity),
            "estimated_purchase_volume": estimate_purchase_volume(buyer, quantity)
        }
        for buyer in buyers
    ]

@router.post("/enhanced-comments/buyer-inquiry")
async def add_buyer_inquiry_comment(
    post_id: int,
    buyer_info: BuyerInquiry,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    """구매 문의 댓글 작성"""
    
    comment_id = await db.execute(
        """
        INSERT INTO enhanced_comments (
            post_id, user_id, content, comment_type, info_data
        ) VALUES ($1, $2, $3, 'buyer_inquiry', $4)
        RETURNING id
        """,
        post_id, current_user.id,
        f"🏫 구매 문의: {buyer_info.organization} - {buyer_info.quantity}{buyer_info.unit} 구매 희망",
        json.dumps(buyer_info.dict())
    )
    
    # 게시글 작성자에게 알림
    post = await db.fetch_one("SELECT user_id FROM producer_posts WHERE id = $1", post_id)
    await send_notification(
        user_id=post['user_id'],
        type='buyer_inquiry',
        data={'comment_id': comment_id, 'buyer': buyer_info.organization}
    )
    
    return {"id": comment_id, "message": "구매 문의가 전달되었습니다."}
```

---

## 5. Frontend 컴포넌트

### 5.1 정보 제공 댓글 컴포넌트

```tsx
// InfoCommentCard.tsx
interface InfoCommentProps {
  comment: EnhancedComment;
  onContact: (info: any) => void;
}

export const InfoCommentCard: React.FC<InfoCommentProps> = ({
  comment,
  onContact
}) => {
  const renderInfoContent = () => {
    switch (comment.comment_type) {
      case 'sales_channel':
        return (
          <SalesChannelInfo>
            <InfoIcon>💼</InfoIcon>
            <InfoContent>
              <InfoTitle>판매처 정보</InfoTitle>
              <CompanyName>{comment.info_data.name}</CompanyName>
              <InfoDetails>
                <DetailItem>
                  <Label>연락처:</Label>
                  <Value>{comment.info_data.contact}</Value>
                </DetailItem>
                <DetailItem>
                  <Label>주소:</Label>
                  <Value>{comment.info_data.address}</Value>
                </DetailItem>
                <DetailItem>
                  <Label>수수료:</Label>
                  <Value>{comment.info_data.commission}%</Value>
                </DetailItem>
                <DetailItem>
                  <Label>최소주문:</Label>
                  <Value>{comment.info_data.requirements?.join(', ')}</Value>
                </DetailItem>
              </InfoDetails>
              <ContactButton onClick={() => onContact(comment.info_data)}>
                <PhoneIcon /> 연락하기
              </ContactButton>
            </InfoContent>
          </SalesChannelInfo>
        );
      
      case 'supplier_info':
        return (
          <SupplierInfo>
            <InfoIcon>🌱</InfoIcon>
            <InfoContent>
              <InfoTitle>원재료 공급처</InfoTitle>
              <CompanyName>{comment.info_data.name}</CompanyName>
              <ProductInfo>
                {comment.info_data.product} - {formatCurrency(comment.info_data.price)}
              </ProductInfo>
              <ContactButton onClick={() => onContact(comment.info_data)}>
                문의하기
              </ContactButton>
            </InfoContent>
          </SupplierInfo>
        );
      
      case 'price_info':
        return (
          <PriceInfo>
            <InfoIcon>📊</InfoIcon>
            <InfoContent>
              <InfoTitle>시장 가격 정보</InfoTitle>
              <MarketName>{comment.info_data.market_name}</MarketName>
              <PriceRange>
                <MinPrice>{formatCurrency(comment.info_data.price_range.min)}</MinPrice>
                <Separator>~</Separator>
                <MaxPrice>{formatCurrency(comment.info_data.price_range.max)}</MaxPrice>
                <Unit>/ {comment.info_data.unit}</Unit>
              </PriceRange>
              <PriceDate>
                기준일: {formatDate(comment.info_data.date)}
              </PriceDate>
            </InfoContent>
          </PriceInfo>
        );
      
      case 'buyer_inquiry':
        return (
          <BuyerInquiry>
            <InfoIcon>🏫</InfoIcon>
            <InfoContent>
              <InfoTitle>구매 문의</InfoTitle>
              <OrganizationName>{comment.info_data.organization}</OrganizationName>
              <InquiryDetails>
                <DetailItem>
                  <Label>희망 수량:</Label>
                  <Value>{comment.info_data.quantity} {comment.info_data.unit}</Value>
                </DetailItem>
                <DetailItem>
                  <Label>구매 주기:</Label>
                  <Value>{comment.info_data.frequency}</Value>
                </DetailItem>
                <DetailItem>
                  <Label>연락처:</Label>
                  <Value>{comment.info_data.contact}</Value>
                </DetailItem>
              </InquiryDetails>
              <ResponseButton onClick={() => handleResponse(comment)}>
                견적 제공하기
              </ResponseButton>
            </InfoContent>
          </BuyerInquiry>
        );
      
      default:
        return <p>{comment.content}</p>;
    }
  };
  
  return (
    <CommentCard verified={comment.is_verified}>
      {comment.is_verified && (
        <VerifiedBadge>
          <CheckIcon /> 검증된 정보
        </VerifiedBadge>
      )}
      
      {renderInfoContent()}
      
      <CommentFooter>
        <Author>
          <Avatar src={comment.user.avatar} />
          <AuthorName>{comment.user.name}</AuthorName>
        </Author>
        <Actions>
          <HelpfulButton onClick={() => markHelpful(comment.id)}>
            <ThumbsUpIcon /> 도움됨 {comment.helpful_count}
          </HelpfulButton>
        </Actions>
      </CommentFooter>
    </CommentCard>
  );
};
```

### 5.2 시장 가격 위젯

```tsx
// MarketPriceWidget.tsx
export const MarketPriceWidget: React.FC<{ foodType: string; region: string }> = ({
  foodType,
  region
}) => {
  const { data: priceData } = useQuery(
    ['market-prices', foodType, region],
    () => api.get(`/market-prices/current?food_type=${foodType}&region=${region}`)
  );
  
  const { data: trendData } = useQuery(
    ['price-trend', foodType, region],
    () => api.get(`/market-prices/trend?food_type=${foodType}&region=${region}&days=30`)
  );
  
  return (
    <PriceWidgetContainer>
      <WidgetHeader>
        <h3>📊 현재 시장 가격</h3>
        <RefreshButton onClick={refetch}>
          <RefreshIcon />
        </RefreshButton>
      </WidgetHeader>
      
      <PriceList>
        {priceData?.map(price => (
          <PriceItem key={price.market_name}>
            <MarketName>{price.market_name}</MarketName>
            <PriceValue>
              {formatCurrency(price.avg_price)} / {price.unit}
            </PriceValue>
            <PriceRange>
              ({formatCurrency(price.min_price)} ~ {formatCurrency(price.max_price)})
            </PriceRange>
          </PriceItem>
        ))}
      </PriceList>
      
      {trendData && (
        <TrendSection>
          <h4>30일 가격 추이</h4>
          <LineChart data={trendData.data} />
          <Recommendation>{trendData.recommendation}</Recommendation>
        </TrendSection>
      )}
    </PriceWidgetContainer>
  );
};
```

---

**문서 관리**
- 작성자: 장재훈
- 최종 업데이트: 2025-11-19
- 연관 문서: 평가관리 CMS 시스템, 플랫폼 아키텍처


