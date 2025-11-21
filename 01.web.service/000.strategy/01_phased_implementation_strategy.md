# SpicyJump 플랫폼 단계별 구축 전략

## 문서 정보
- 작성일: 2025-11-19
- 버전: 2.0
- 목적: 가볍게 시작, 단계적 확장

---

## 1. 구축 철학

### 1.1 핵심 원칙
```
"Start Light, Scale Smart"
가볍게 시작하고, 똑똑하게 확장하라
```

- **MVP First**: 최소 기능으로 빠른 출시
- **Prove, Then Scale**: 검증 후 확장
- **Modular Design**: 모듈식 구조로 점진적 추가
- **Global Ready**: 처음부터 글로벌 준비

---

## 2. 3단계 구축 로드맵

### Phase 1: MVP (3개월) - 가볍게 시작
**목표**: 핵심 기능으로 빠른 시장 진입

#### 기능 범위
```
✅ 필수 (Must Have)
- 회원가입/로그인 (이메일, 소셜)
- 상품 등록/조회 (판매자)
- 상품 구매 (구매자)
- 기본 결제 (PG 1개)
- 간단한 리뷰 (별점 + 텍스트)
- 다국어 (한국어, 영어만)

❌ 제외 (나중에)
- 블록체인
- AI 분석
- 고급 CMS
- 복잡한 리워드
- 실시간 채팅
```

#### 기술 스택 (최소화)
```yaml
Frontend:
  - Next.js 14 (App Router)
  - TypeScript
  - Tailwind CSS
  - React Query
  
Backend:
  - FastAPI (Python)
  - PostgreSQL (단일 DB)
  - Redis (세션만)
  
Infrastructure:
  - Vercel (Frontend)
  - Railway/Fly.io (Backend)
  - AWS S3 (이미지)
```

#### 데이터베이스 (간소화)
```sql
-- Phase 1 필수 테이블만
- users (회원)
- products (상품)
- orders (주문)
- payments (결제)
- reviews (리뷰 - 기본)
- categories (카테고리)
```

#### Phase 1 우선순위
1. **Week 1-2**: 회원 + 인증
2. **Week 3-4**: 상품 등록/조회
3. **Week 5-6**: 장바구니 + 주문
4. **Week 7-8**: 결제 연동
5. **Week 9-10**: 리뷰 시스템
6. **Week 11-12**: 테스트 + 배포

---

### Phase 2: 확장 (6개월) - 검증 후 추가
**목표**: 사용자 피드백 기반 핵심 기능 추가

#### 추가 기능
```
✅ 추가할 것
- 관리자 대시보드 (기본)
- 블록체인 (거래 기록만)
- 간단한 리워드 (포인트)
- 댓글 시스템
- 다국어 추가 (중국어, 일본어)
- 원산지/HACCP 표시
- 기본 통계

⏸️ 보류
- AI 분석
- 복잡한 CMS
- 실시간 채팅
- 라이브 스트리밍
```

#### 추가 테이블 (필요시만)
```sql
- comments (댓글)
- reward_points (포인트)
- blockchain_transactions (블록체인 기록)
- product_origin (원산지)
- product_haccp (HACCP)
- admin_logs (관리 로그)
```

---

### Phase 3: 고도화 (12개월+) - 지능형 시스템
**목표**: AI 기반 정보지원 플랫폼

#### 고급 기능
```
- AI 분석 (식품 인식, 품질 평가)
- 지능형 매칭 (판매처, 구매처)
- 시장가격 정보
- 실시간 채팅
- 라이브 스트리밍
- 고급 CMS
- 전문 분석 도구
```

---

## 3. Phase 1 상세 설계 (MVP)

### 3.1 최소 데이터베이스 스키마

```sql
-- 1. 사용자 (최소)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    name VARCHAR(200),
    role VARCHAR(20) DEFAULT 'buyer', -- buyer, seller, admin
    status VARCHAR(20) DEFAULT 'active',
    language VARCHAR(10) DEFAULT 'ko',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 2. 상품 (간소화)
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    seller_id INTEGER REFERENCES users(id),
    category_id INTEGER REFERENCES categories(id),
    name VARCHAR(500) NOT NULL,
    name_en VARCHAR(500), -- 영문명
    description TEXT,
    price DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD', -- 글로벌 기본 USD
    stock_quantity INTEGER DEFAULT 0,
    images JSONB, -- 이미지 URL 배열
    status VARCHAR(20) DEFAULT 'published',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 3. 카테고리 (단순)
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    name_en VARCHAR(200),
    parent_id INTEGER REFERENCES categories(id)
);

-- 4. 주문
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    buyer_id INTEGER REFERENCES users(id),
    seller_id INTEGER REFERENCES users(id),
    total DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    shipping_address JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 5. 주문 항목
CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    product_id INTEGER REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL
);

-- 6. 결제 (간소화)
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER REFERENCES orders(id),
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    payment_method VARCHAR(50),
    pg_transaction_id VARCHAR(255), -- PG사 거래 ID
    created_at TIMESTAMP DEFAULT NOW()
);

-- 7. 리뷰 (최소)
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    product_id INTEGER REFERENCES products(id),
    user_id INTEGER REFERENCES users(id),
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    images JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 3.2 간소화된 API 구조

```python
# Phase 1 API (최소)
/api/v1/
  /auth/
    POST /register          # 회원가입
    POST /login            # 로그인
    POST /logout           # 로그아웃
    
  /products/
    GET  /                 # 상품 목록
    GET  /{id}            # 상품 상세
    POST /                 # 상품 등록 (판매자)
    PUT  /{id}            # 상품 수정
    
  /cart/
    GET  /                 # 장바구니 조회
    POST /items           # 장바구니 추가
    DELETE /items/{id}    # 장바구니 삭제
    
  /orders/
    POST /                 # 주문 생성
    GET  /                 # 주문 목록
    GET  /{id}            # 주문 상세
    
  /payments/
    POST /                 # 결제 시작
    POST /callback        # 결제 콜백
    
  /reviews/
    GET  /                 # 리뷰 목록
    POST /                 # 리뷰 작성
```

### 3.3 간단한 Frontend 구조

```
/app                      # Next.js 14 App Router
  /(auth)/
    /login               # 로그인
    /register            # 회원가입
    
  /(shop)/
    /                    # 홈 (상품 목록)
    /products/[id]       # 상품 상세
    /cart               # 장바구니
    /checkout           # 결제
    
  /(account)/
    /orders             # 주문 내역
    /reviews            # 내 리뷰
    
  /(seller)/           # 판매자 전용
    /products          # 상품 관리
    /orders            # 주문 관리
    
/components             # 재사용 컴포넌트
  /ui/                 # 기본 UI (shadcn/ui)
  /product/            # 상품 관련
  /cart/               # 장바구니 관련
```

---

## 4. 글로벌 대응 (처음부터)

### 4.1 다국어 (i18n) - Phase 1부터

```typescript
// 간단한 다국어 구조
/locales/
  en.json              # 영어
  ko.json              # 한국어
  
// Phase 2 추가
  zh.json              # 중국어
  ja.json              # 일본어
  vi.json              # 베트남어
```

```json
// ko.json (최소)
{
  "common": {
    "login": "로그인",
    "register": "회원가입",
    "logout": "로그아웃"
  },
  "product": {
    "add_to_cart": "장바구니",
    "buy_now": "구매하기",
    "price": "가격",
    "stock": "재고"
  },
  "order": {
    "checkout": "결제하기",
    "total": "총 금액",
    "status": "주문 상태"
  }
}
```

### 4.2 글로벌 판매처 연계 (확장 시)

```sql
-- Phase 2-3에서 추가
CREATE TABLE global_partners (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200),
    type VARCHAR(50), -- restaurant, market, distributor
    country VARCHAR(2), -- US, KR, JP, CN, etc.
    region VARCHAR(100), -- California, New York, etc.
    contact JSONB,
    products_interested JSONB, -- 관심 품목
    purchase_volume DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 예시 데이터
INSERT INTO global_partners (name, type, country, region, products_interested) VALUES
('LA 한식당', 'restaurant', 'US', 'California', '["kimchi", "gochujang", "soy_sauce"]'),
('뉴욕 한국마트', 'market', 'US', 'New York', '["kimchi", "ramen", "snacks"]'),
('시카고 식자재 유통', 'distributor', 'US', 'Illinois', '["frozen_food", "sauce", "noodles"]');
```

---

## 5. 단계별 확장 가이드

### 5.1 Phase 1 → Phase 2 전환 시점

**확장 조건 (다음 중 하나 충족 시)**
```
✅ 일일 활성 사용자 1,000명 이상
✅ 월 거래액 $50,000 이상
✅ 등록 상품 5,000개 이상
✅ 사용자 피드백으로 명확한 needs 확인
```

**확장 우선순위**
```
1순위: 사용자가 가장 많이 요청한 기능
2순위: 운영 효율화 (관리자 도구)
3순위: 매출 증대 기능 (리워드, 프로모션)
4순위: 고급 기능 (AI, 블록체인)
```

### 5.2 Phase 2 → Phase 3 전환 시점

**고도화 조건**
```
✅ 일일 활성 사용자 10,000명 이상
✅ 월 거래액 $500,000 이상
✅ 다국가 진출 (3개국 이상)
✅ 충분한 데이터 축적 (AI 학습용)
```

---

## 6. 기술 부채 관리

### 6.1 Phase 1에서 의도적으로 단순화한 부분

```python
# 나중에 개선할 부분 (주석으로 표시)
class Product:
    # TODO Phase 2: Add origin information
    # TODO Phase 3: Add AI quality score
    # TODO Phase 3: Add blockchain verification
    
    id: int
    name: str
    price: Decimal
    # ... 현재 필수 필드만
```

### 6.2 확장 포인트 (Extension Points)

```python
# 플러그인 가능한 구조
class PaymentGateway(ABC):
    """결제 게이트웨이 인터페이스"""
    
    @abstractmethod
    async def process_payment(self, order: Order) -> PaymentResult:
        pass

# Phase 1: Stripe만
class StripeGateway(PaymentGateway):
    async def process_payment(self, order: Order) -> PaymentResult:
        # Stripe 결제 처리
        pass

# Phase 2 추가: 토스페이먼츠
class TossPaymentsGateway(PaymentGateway):
    async def process_payment(self, order: Order) -> PaymentResult:
        # 토스페이먼츠 처리
        pass
```

---

## 7. 운영 고려사항

### 7.1 Phase 1 운영 (최소 인력)

```
필수 인력:
- 개발자 1-2명 (Full-stack)
- 디자이너 0.5명 (파트타임)
- 운영자 1명

인프라:
- Vercel (Frontend) - $20/월
- Railway (Backend) - $20/월
- AWS S3 (이미지) - $10/월
- PostgreSQL (Managed) - $20/월
---
총 예상 비용: $70-100/월
```

### 7.2 Phase 2 운영

```
추가 인력:
- Backend 개발자 +1명
- CS 담당자 +1명
- 마케터 +1명

인프라 확장:
- CDN 추가
- Redis 클러스터
- 모니터링 도구
---
총 예상 비용: $300-500/월
```

---

## 8. 마일스톤 체크리스트

### Phase 1 완료 기준
```
□ 회원가입/로그인 작동
□ 상품 등록 및 조회 가능
□ 결제 처리 (실제 거래 가능)
□ 주문 관리 기능
□ 리뷰 작성/조회 가능
□ 한국어/영어 지원
□ 모바일 반응형
□ 기본 보안 (HTTPS, 인증)
□ 100명 베타 테스트 완료
```

### Phase 2 완료 기준
```
□ 관리자 대시보드
□ 블록체인 거래 기록
□ 포인트 시스템
□ 4개 언어 지원
□ 원산지/HACCP 표시
□ 1,000명 활성 사용자
□ 결제 성공률 95% 이상
```

---

## 9. 위험 관리

### 9.1 기술 위험

| 위험 | 영향 | 대응 |
|------|------|------|
| 과도한 기능 | 출시 지연 | MVP 원칙 고수 |
| 확장성 부족 | 성능 저하 | 모듈식 설계 |
| 기술 부채 | 유지보수 어려움 | 리팩토링 주기 설정 |

### 9.2 비즈니스 위험

| 위험 | 영향 | 대응 |
|------|------|------|
| 시장 검증 실패 | 투자 손실 | 빠른 MVP, 피드백 |
| 경쟁사 출현 | 시장 점유율 하락 | 차별화 기능 |
| 글로벌 진출 실패 | 매출 정체 | 단계적 확장 |

---

## 10. 의사결정 프레임워크

### "지금 만들까, 나중에 만들까?"

```
질문 체크리스트:
□ 이 기능 없이 서비스가 불가능한가? → YES면 Phase 1
□ 사용자가 명확히 요청했는가? → YES면 Phase 2
□ 매출에 직접 영향을 주는가? → YES면 우선순위 UP
□ 구현 비용이 큰가? → YES면 Phase 3
□ 경쟁 우위 요소인가? → YES면 조기 검토

결정:
- 3개 이상 YES: Phase 1
- 2개 YES: Phase 2
- 1개 이하 YES: Phase 3 또는 보류
```

---

## 부록: Quick Start 가이드

### Phase 1 개발 시작하기

```bash
# 1. 프로젝트 초기화
mkdir spicyjump-mvp
cd spicyjump-mvp

# 2. Frontend
npx create-next-app@latest frontend --typescript --tailwind --app
cd frontend
npm install react-query axios zustand

# 3. Backend
mkdir backend
cd backend
pip install fastapi uvicorn sqlalchemy psycopg2-binary python-jose

# 4. Database
# PostgreSQL 설치 또는 Railway/Supabase 사용

# 5. 개발 시작
npm run dev  # Frontend
uvicorn main:app --reload  # Backend
```

---

**문서 관리**
- 작성자: 장재훈
- 최종 업데이트: 2025-11-19
- 리뷰 주기: 단계 전환 시
- 다음 리뷰: Phase 1 완료 시점


