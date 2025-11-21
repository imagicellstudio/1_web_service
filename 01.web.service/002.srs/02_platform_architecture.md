# SpicyJump K-Food 플랫폼 전체 아키텍처

## 문서 정보
- 프로젝트명: SpicyJump K-Food Platform (플랫폼 전환)
- 작성일: 2025-11-19
- 버전: 2.0
- 상태: 플랫폼 서비스 전환

---

## 1. 프로젝트 성격 변경

### 1.1 변경 전 → 변경 후

| 구분 | 변경 전 (Website) | 변경 후 (Platform) |
|------|------------------|-------------------|
| **성격** | 정적 웹사이트 | 동적 플랫폼 서비스 |
| **사용자** | 단순 방문자 | 다중 역할 사용자 (구매자/판매자/관리자) |
| **데이터** | 컨텐츠 중심 | 거래/생성 데이터 중심 |
| **운영** | 수동 컨텐츠 관리 | 실시간 생태계 관리 |
| **확장성** | 제한적 | 무제한 확장 가능 |
| **비즈니스** | 정보 제공 | 거래 중개 플랫폼 |

### 1.2 플랫폼 정의
**SpicyJump는 K-Food 글로벌 거래 플랫폼**
- B2B, B2C 복합 거래 플랫폼
- 블록체인 기반 신뢰 거래
- 다국어 글로벌 서비스
- 데이터 기반 의사결정
- 실시간 생태계 관리

---

## 2. 전체 시스템 아키텍처

### 2.1 3-Tier 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                       │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │   Web UI   │  │ Tablet UI  │  │ Mobile UI  │            │
│  │  (React)   │  │  (React)   │  │  (React)   │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│         반응형 프론트엔드 (사용자 중심)                        │
└─────────────────────────────────────────────────────────────┘
                              │
                              │ HTTPS/REST API/GraphQL
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                       │
│  ┌──────────────────────────────────────────────────────┐   │
│  │              API Gateway (Kong/Nginx)                │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                              │
│  ┌─────────────────┐         ┌──────────────────────┐      │
│  │  User Services  │         │  Admin Services      │      │
│  │  (FastAPI)      │         │  (FastAPI)           │      │
│  │                 │         │                      │      │
│  │ - 회원          │         │ - 대시보드           │      │
│  │ - 상품 조회     │         │ - 거래/결제 관리     │      │
│  │ - 장바구니      │         │ - 회원 관리          │      │
│  │ - 주문/결제     │         │ - 권한 관리          │      │
│  │ - 리뷰/평가     │         │ - 표준정보 관리      │      │
│  └─────────────────┘         │ - 데이터 관리        │      │
│                               │ - 보안/인증 관리     │      │
│                               │ - 연계 관리          │      │
│                               │ - 언어지원 관리      │      │
│                               └──────────────────────┘      │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Core Business Logic Services              │   │
│  ├─────────────────────────────────────────────────────┤   │
│  │ 거래 엔진 │ 결제 엔진 │ 블록체인 │ 추천 엔진 │     │   │
│  │ 검색 엔진 │ 알림 서비스 │ 분석 엔진 │ AI/ML     │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                         DATA LAYER                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │PostgreSQL│  │  Redis   │  │Elasticsearch│ │ MongoDB │  │
│  │(거래/회원)│  │ (캐시)   │  │  (검색)    │ │(로그/분석)│  │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
│                                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐                 │
│  │   S3     │  │Blockchain│  │  Kafka   │                 │
│  │(파일저장)│  │(거래원장)│  │(메시지큐)│                 │
│  └──────────┘  └──────────┘  └──────────┘                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                    EXTERNAL INTEGRATIONS                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │식품안전나라│  │ 관세청  │  │   PG사   │  │ 블록체인  │   │
│  │   API    │  │  API    │  │(결제API) │  │ Network   │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Frontend 아키텍처 (반응형)

### 3.1 반응형 구조

```typescript
// 디바이스별 분기점
const breakpoints = {
  mobile: '320px - 767px',   // 모바일
  tablet: '768px - 1023px',  // 태블릿
  desktop: '1024px+'         // 데스크톱
}

// 공통 컴포넌트 구조
/src
  /components
    /common          // 공통 컴포넌트
      Header.tsx
      Footer.tsx
      Navigation.tsx
    /product         // 상품 관련
      ProductCard.tsx
      ProductList.tsx
      ProductDetail.tsx
    /cart            // 장바구니
    /order           // 주문
    /user            // 회원
    /review          // 리뷰
  /layouts
    /mobile          // 모바일 레이아웃
    /tablet          // 태블릿 레이아웃
    /desktop         // 데스크톱 레이아웃
  /hooks            // Custom Hooks
  /services         // API 서비스
  /store            // 상태 관리 (Zustand/Redux)
  /utils            // 유틸리티
  /locales          // 다국어
```

### 3.2 소통형 기능 컴포넌트

#### 실시간 소통 기능
```typescript
// 1. 실시간 채팅
interface ChatComponent {
  type: '1:1' | 'group' | 'support';
  realtime: boolean;  // WebSocket
  translation: boolean;  // 자동 번역
}

// 2. 라이브 스트리밍
interface LiveStreamComponent {
  broadcasting: boolean;
  chat: boolean;
  product_link: boolean;  // 상품 연결
}

// 3. 커뮤니티
interface CommunityComponent {
  forum: boolean;
  Q&A: boolean;
  review: boolean;
  rating: boolean;
}

// 4. 알림
interface NotificationComponent {
  push: boolean;
  email: boolean;
  sms: boolean;
  in_app: boolean;
}
```

### 3.3 반응형 UI 예시

```tsx
// ProductCard.tsx - 반응형 컴포넌트
import { useMediaQuery } from '@/hooks/useMediaQuery';

export const ProductCard: React.FC<ProductProps> = ({ product }) => {
  const isMobile = useMediaQuery('(max-width: 767px)');
  const isTablet = useMediaQuery('(min-width: 768px) and (max-width: 1023px)');
  const isDesktop = useMediaQuery('(min-width: 1024px)');

  return (
    <Card className={`
      ${isMobile ? 'card-mobile' : ''}
      ${isTablet ? 'card-tablet' : ''}
      ${isDesktop ? 'card-desktop' : ''}
    `}>
      {/* 디바이스별 최적화된 레이아웃 */}
      {isMobile && <MobileLayout product={product} />}
      {isTablet && <TabletLayout product={product} />}
      {isDesktop && <DesktopLayout product={product} />}
    </Card>
  );
};
```

---

## 4. Backend 아키텍처 (관리자 중심)

### 4.1 관리자 화면 구조

```
/admin-dashboard
  /overview              # 대시보드 (통계/지표)
  /transaction          # 거래/결제 관리
  /payment              # 결제 관리
  /member               # 회원 관리
  /auth                 # 권한 관리
  /reward               # 라벨/리워드 관리
  /compliance           # 규정/개인정보 관리
  /standards            # 표준정보 관리
    - HS Code 관리
    - HACCP 관리
    - 원산지 관리
    - 영양성분 관리
    - 알러지 식품 관리
    - 식약처 정보 관리
    - 농식품 코드 관리
  /data-management      # 데이터 관리
    - 식품 데이터
    - 원산지 데이터
    - 코드 데이터
    - 거래 데이터 (블록체인)
    - 결제 데이터
    - 회원 데이터
    - 평가 데이터
    - 라벨링 데이터
    - 정형/비정형 데이터
    - 전처리 관리
  /security             # 보안 관리
  /integration          # 연계 관리
    - API 연동 관리
    - 결제 연동
    - 블록체인 연동
    - 공공기관 연동
  /localization         # 언어지원 관리
```

### 4.2 Backend API 구조

```python
# FastAPI 마이크로서비스 구조
/backend
  /services
    /user-service          # 사용자 서비스
      /api
        auth.py
        profile.py
        preferences.py
    
    /product-service       # 상품 서비스
      /api
        products.py
        categories.py
        search.py
    
    /transaction-service   # 거래 서비스
      /api
        orders.py
        cart.py
        checkout.py
    
    /payment-service       # 결제 서비스
      /api
        payments.py
        refunds.py
        settlements.py
    
    /blockchain-service    # 블록체인 서비스
      /api
        transactions.py
        verification.py
        smart_contracts.py
    
    /admin-service         # 관리자 서비스
      /api
        /transaction
          transaction_management.py
          payment_management.py
        /member
          member_management.py
          auth_management.py
        /reward
          label_management.py
          reward_management.py
        /compliance
          regulation_management.py
          privacy_management.py
        /standards
          hs_code_management.py
          haccp_management.py
          origin_management.py
          nutrition_management.py
          allergen_management.py
          mfds_management.py       # 식약처
          agri_code_management.py  # 농식품코드
        /data
          food_data_management.py
          origin_data_management.py
          code_data_management.py
          blockchain_data_management.py
          payment_data_management.py
          member_data_management.py
          rating_data_management.py
          labeling_data_management.py
          structured_data_management.py
          unstructured_data_management.py
          preprocessing_management.py
        /security
          security_management.py
          auth_management.py
          certificate_management.py
        /integration
          api_integration_management.py
          payment_integration_management.py
          blockchain_integration_management.py
          public_api_management.py
        /localization
          language_management.py
          translation_management.py
          content_management.py
    
    /notification-service  # 알림 서비스
    /analytics-service     # 분석 서비스
    /recommendation-service # 추천 서비스
```

---

## 5. 핵심 기능 모듈 상세

### 5.1 거래/결제 관리

```python
# /admin/api/transaction/transaction_management.py
from fastapi import APIRouter, Depends
from typing import List, Optional
from datetime import datetime

router = APIRouter()

class TransactionManagement:
    """거래 관리 모듈"""
    
    @router.get("/admin/transactions")
    async def get_transactions(
        status: Optional[str] = None,
        date_from: Optional[datetime] = None,
        date_to: Optional[datetime] = None,
        page: int = 1,
        limit: int = 50
    ):
        """전체 거래 조회"""
        pass
    
    @router.get("/admin/transactions/{transaction_id}")
    async def get_transaction_detail(transaction_id: str):
        """거래 상세 조회"""
        pass
    
    @router.put("/admin/transactions/{transaction_id}/status")
    async def update_transaction_status(
        transaction_id: str,
        status: str,
        reason: Optional[str] = None
    ):
        """거래 상태 변경"""
        pass
    
    @router.get("/admin/transactions/statistics")
    async def get_transaction_statistics(
        period: str = 'daily'  # daily, weekly, monthly
    ):
        """거래 통계"""
        return {
            "total_transactions": 0,
            "total_amount": 0,
            "success_rate": 0,
            "average_amount": 0,
            "by_status": {},
            "by_payment_method": {}
        }
```

### 5.2 회원 관리

```python
# /admin/api/member/member_management.py
class MemberManagement:
    """회원 관리 모듈"""
    
    @router.get("/admin/members")
    async def get_members(
        role: Optional[str] = None,  # buyer, seller, admin
        status: Optional[str] = None,  # active, inactive, suspended
        search: Optional[str] = None,
        page: int = 1,
        limit: int = 50
    ):
        """회원 목록 조회"""
        pass
    
    @router.get("/admin/members/{member_id}")
    async def get_member_detail(member_id: int):
        """회원 상세 정보"""
        pass
    
    @router.put("/admin/members/{member_id}/status")
    async def update_member_status(
        member_id: int,
        status: str,
        reason: Optional[str] = None
    ):
        """회원 상태 변경 (정지/해제)"""
        pass
    
    @router.get("/admin/members/{member_id}/activities")
    async def get_member_activities(member_id: int):
        """회원 활동 이력"""
        pass
    
    @router.get("/admin/members/statistics")
    async def get_member_statistics():
        """회원 통계"""
        return {
            "total_members": 0,
            "new_members_today": 0,
            "active_members": 0,
            "by_role": {},
            "by_country": {},
            "retention_rate": 0
        }
```

### 5.3 권한 관리 (RBAC)

```python
# /admin/api/member/auth_management.py
class RoleBasedAccessControl:
    """역할 기반 접근 제어"""
    
    # 역할 정의
    ROLES = {
        'super_admin': {
            'name': '최고 관리자',
            'permissions': ['*']  # 모든 권한
        },
        'admin': {
            'name': '관리자',
            'permissions': [
                'transaction.read', 'transaction.update',
                'member.read', 'member.update',
                'product.manage', 'data.manage'
            ]
        },
        'moderator': {
            'name': '운영자',
            'permissions': [
                'transaction.read', 'member.read',
                'product.read', 'content.moderate'
            ]
        },
        'seller': {
            'name': '판매자',
            'permissions': [
                'product.create', 'product.update',
                'order.read', 'order.update'
            ]
        },
        'buyer': {
            'name': '구매자',
            'permissions': [
                'product.read', 'order.create',
                'review.create'
            ]
        }
    }
    
    @router.get("/admin/roles")
    async def get_roles():
        """역할 목록 조회"""
        return ROLES
    
    @router.post("/admin/roles")
    async def create_role(role_data: RoleCreate):
        """역할 생성"""
        pass
    
    @router.put("/admin/members/{member_id}/roles")
    async def assign_role(member_id: int, role: str):
        """회원에게 역할 할당"""
        pass
    
    @router.get("/admin/permissions")
    async def get_permissions():
        """권한 목록 조회"""
        pass
```

### 5.4 라벨/리워드 관리

```python
# /admin/api/reward/label_management.py
class LabelManagement:
    """라벨 관리"""
    
    @router.get("/admin/labels")
    async def get_labels():
        """라벨 목록"""
        return {
            "quality_labels": ["프리미엄", "인증", "베스트셀러"],
            "certification_labels": ["HACCP", "유기농", "친환경"],
            "origin_labels": ["국산", "수입"],
            "custom_labels": []
        }
    
    @router.post("/admin/labels")
    async def create_label(label: LabelCreate):
        """라벨 생성"""
        pass
    
    @router.post("/admin/products/{product_id}/labels")
    async def assign_label(product_id: int, label_id: int):
        """상품에 라벨 할당"""
        pass

class RewardManagement:
    """리워드 관리"""
    
    @router.get("/admin/rewards")
    async def get_rewards():
        """리워드 프로그램 목록"""
        pass
    
    @router.post("/admin/rewards")
    async def create_reward(reward: RewardCreate):
        """리워드 프로그램 생성"""
        pass
    
    @router.get("/admin/rewards/statistics")
    async def get_reward_statistics():
        """리워드 통계"""
        pass
```

### 5.5 표준정보 관리

```python
# /admin/api/standards/hs_code_management.py
class StandardsManagement:
    """표준 정보 관리"""
    
    # HS Code 관리
    @router.get("/admin/standards/hs-codes")
    async def get_hs_codes():
        pass
    
    @router.post("/admin/standards/hs-codes")
    async def create_hs_code():
        pass
    
    @router.put("/admin/standards/hs-codes/{code}")
    async def update_hs_code():
        pass
    
    # HACCP 관리
    @router.get("/admin/standards/haccp")
    async def get_haccp_certifications():
        pass
    
    # 원산지 관리
    @router.get("/admin/standards/origins")
    async def get_origins():
        pass
    
    # 영양성분 관리
    @router.get("/admin/standards/nutrition")
    async def get_nutrition_standards():
        pass
    
    # 알러지 식품 관리
    @router.get("/admin/standards/allergens")
    async def get_allergens():
        """알러지 유발 식품 목록"""
        return {
            "allergens": [
                "우유", "계란", "밀", "대두", "땅콩",
                "견과류", "생선", "갑각류", "조개류",
                "메밀", "복숭아", "토마토"
            ]
        }
    
    # 식약처 정보 관리
    @router.get("/admin/standards/mfds")
    async def get_mfds_data():
        """식품의약품안전처 연동 데이터"""
        pass
    
    # 농식품 코드 관리
    @router.get("/admin/standards/agri-codes")
    async def get_agri_codes():
        """농식품 분류 코드"""
        pass
```

### 5.6 데이터 관리 (정형/비정형)

```python
# /admin/api/data/structured_data_management.py
class DataManagement:
    """데이터 관리"""
    
    # 정형 데이터 관리
    @router.get("/admin/data/structured")
    async def get_structured_data(
        type: str,  # food, origin, code, transaction, payment, member, rating
        page: int = 1,
        limit: int = 100
    ):
        """정형 데이터 조회"""
        pass
    
    # 비정형 데이터 관리
    @router.get("/admin/data/unstructured")
    async def get_unstructured_data(
        type: str,  # images, videos, documents, logs
        page: int = 1,
        limit: int = 100
    ):
        """비정형 데이터 조회"""
        pass
    
    # 데이터 전처리
    @router.post("/admin/data/preprocessing")
    async def run_preprocessing(
        data_type: str,
        operations: List[str]  # clean, normalize, transform, validate
    ):
        """데이터 전처리 실행"""
        pass
    
    # 블록체인 데이터
    @router.get("/admin/data/blockchain")
    async def get_blockchain_data():
        """블록체인 거래 데이터"""
        pass
    
    # 라벨링 데이터
    @router.get("/admin/data/labeling")
    async def get_labeling_data():
        """라벨링된 데이터 (AI 학습용)"""
        pass
```

### 5.7 보안/인증 관리

```python
# /admin/api/security/security_management.py
class SecurityManagement:
    """보안 관리"""
    
    @router.get("/admin/security/logs")
    async def get_security_logs(
        event_type: Optional[str] = None,
        severity: Optional[str] = None
    ):
        """보안 로그 조회"""
        pass
    
    @router.get("/admin/security/threats")
    async def get_threats():
        """보안 위협 탐지"""
        pass
    
    @router.get("/admin/security/certificates")
    async def get_certificates():
        """SSL/TLS 인증서 관리"""
        pass
    
    @router.post("/admin/security/2fa")
    async def enable_2fa(member_id: int):
        """2단계 인증 활성화"""
        pass
    
    @router.get("/admin/security/api-keys")
    async def get_api_keys():
        """API 키 관리"""
        pass
```

### 5.8 연계 관리

```python
# /admin/api/integration/api_integration_management.py
class IntegrationManagement:
    """연계 관리"""
    
    # 외부 API 연동 관리
    @router.get("/admin/integrations/apis")
    async def get_api_integrations():
        """외부 API 연동 목록"""
        return {
            "integrations": [
                {
                    "name": "식품안전나라",
                    "status": "active",
                    "last_sync": "2025-11-19T10:00:00",
                    "success_rate": 99.8
                },
                {
                    "name": "관세청",
                    "status": "active",
                    "last_sync": "2025-11-19T09:30:00",
                    "success_rate": 99.5
                }
            ]
        }
    
    # 결제 연동 관리
    @router.get("/admin/integrations/payments")
    async def get_payment_integrations():
        """결제 시스템 연동"""
        pass
    
    # 블록체인 연동 관리
    @router.get("/admin/integrations/blockchain")
    async def get_blockchain_integration():
        """블록체인 네트워크 연동"""
        pass
    
    # 공공기관 연동 관리
    @router.get("/admin/integrations/public")
    async def get_public_integrations():
        """공공기관 API 연동"""
        pass
    
    @router.post("/admin/integrations/sync")
    async def manual_sync(integration_id: int):
        """수동 동기화"""
        pass
```

### 5.9 언어지원 관리

```python
# /admin/api/localization/language_management.py
class LocalizationManagement:
    """언어지원 관리"""
    
    @router.get("/admin/localization/languages")
    async def get_supported_languages():
        """지원 언어 목록"""
        return {
            "languages": [
                {"code": "ko", "name": "한국어", "enabled": True},
                {"code": "en", "name": "English", "enabled": True},
                {"code": "zh", "name": "中文", "enabled": True},
                {"code": "ja", "name": "日本語", "enabled": True},
                {"code": "vi", "name": "Tiếng Việt", "enabled": False},
                {"code": "es", "name": "Español", "enabled": False}
            ]
        }
    
    @router.get("/admin/localization/translations")
    async def get_translations(
        language: str,
        key: Optional[str] = None
    ):
        """번역 데이터 조회"""
        pass
    
    @router.put("/admin/localization/translations")
    async def update_translation(
        language: str,
        key: str,
        value: str
    ):
        """번역 데이터 수정"""
        pass
    
    @router.post("/admin/localization/auto-translate")
    async def auto_translate(
        source_lang: str,
        target_lang: str,
        keys: List[str]
    ):
        """자동 번역 (AI 기반)"""
        pass
    
    @router.get("/admin/localization/coverage")
    async def get_translation_coverage():
        """번역 커버리지"""
        return {
            "ko": 100.0,
            "en": 98.5,
            "zh": 95.0,
            "ja": 92.3
        }
```

---

## 6. 데이터베이스 스키마 (플랫폼 확장)

### 6.1 핵심 테이블 구조

```sql
-- 회원 테이블
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(50) DEFAULT 'buyer',
    status VARCHAR(20) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 거래 테이블
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    buyer_id INTEGER REFERENCES users(id),
    seller_id INTEGER REFERENCES users(id),
    product_id INTEGER REFERENCES products(id),
    amount DECIMAL(15,2),
    status VARCHAR(50),
    blockchain_tx_hash VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 결제 테이블
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    transaction_id INTEGER REFERENCES transactions(id),
    payment_method VARCHAR(50),
    amount DECIMAL(15,2),
    status VARCHAR(50),
    pg_transaction_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 권한 테이블
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    role VARCHAR(50),
    resource VARCHAR(100),
    action VARCHAR(50),
    allowed BOOLEAN DEFAULT TRUE
);

-- 라벨 테이블
CREATE TABLE labels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    type VARCHAR(50),
    description TEXT,
    icon_url VARCHAR(255)
);

-- 리워드 테이블
CREATE TABLE rewards (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    points INTEGER DEFAULT 0,
    level VARCHAR(50),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 데이터 전처리 로그
CREATE TABLE data_preprocessing_logs (
    id SERIAL PRIMARY KEY,
    data_type VARCHAR(50),
    operation VARCHAR(50),
    status VARCHAR(50),
    records_processed INTEGER,
    errors INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);
```

---

## 7. 배포 아키텍처

### 7.1 Kubernetes 기반 배포

```yaml
# k8s-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: spicyjump-platform
spec:
  replicas: 3
  selector:
    matchLabels:
      app: spicyjump
  template:
    metadata:
      labels:
        app: spicyjump
    spec:
      containers:
      - name: frontend
        image: spicyjump/frontend:latest
        ports:
        - containerPort: 3000
      - name: backend
        image: spicyjump/backend:latest
        ports:
        - containerPort: 8000
```

---

## 8. 모니터링 & 운영

### 8.1 대시보드 KPI

```python
# 관리자 대시보드 주요 지표
class DashboardKPI:
    metrics = {
        "실시간 사용자": "현재 접속 중인 사용자 수",
        "일일 거래액": "금일 총 거래 금액",
        "거래 성공률": "성공한 거래 비율",
        "평균 응답시간": "API 평균 응답 시간",
        "신규 회원": "금일 가입한 회원 수",
        "활성 판매자": "활동 중인 판매자 수",
        "상품 등록": "금일 등록된 상품 수",
        "고객 만족도": "평균 평점",
        "시스템 상태": "서버 헬스 체크"
    }
```

---

**문서 관리**
- 작성자: 장재훈
- 버전: 2.0 (플랫폼 전환)
- 최종 업데이트: 2025-11-19
- 다음 리뷰: 주요 기능 구현 완료 시


