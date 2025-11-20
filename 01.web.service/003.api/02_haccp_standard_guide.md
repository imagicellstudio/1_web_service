# HACCP 인증 및 표시 가이드

## 1. HACCP 개요

### 1.1 정의
**HACCP** (Hazard Analysis and Critical Control Point)
- 해썹, 식품안전관리인증기준
- 식품의 원재료부터 제조, 가공, 보존, 유통, 조리 단계까지 위해요소를 분석하고 중요관리점을 정하여 집중 관리하는 과학적인 위생관리체계

### 1.2 법적 근거
- 식품위생법 제48조 (식품안전관리인증기준)
- 축산물위생관리법 제9조

### 1.3 의무 적용 대상
```
2021년 기준 의무 적용 식품:
- 어묵류
- 냉동수산식품 (어류, 연체류, 조미가공품)
- 냉동식품 (피자, 만두, 면류)
- 빙과류
- 레토르트식품
- 김치류
- 배추김치
```

---

## 2. HACCP 인증 절차

### 2.1 인증 신청
1. HACCP 교육 이수 (영업자 또는 종업원)
2. HACCP 팀 구성
3. 제품 및 공정 설명
4. 위해요소 분석
5. 중요관리점(CCP) 결정
6. 인증 신청

### 2.2 인증 심사
- 서류 심사
- 현장 심사
- 시정조치
- 인증서 발급

### 2.3 유효기간
- **일반업소**: 3년
- **HACCP 적용업소**: 갱신 심사 필요

---

## 3. HACCP 인증 정보 DB 구조

### 3.1 테이블 설계
```sql
CREATE TABLE haccp_certifications (
    id SERIAL PRIMARY KEY,
    company_id INTEGER NOT NULL,
    certification_number VARCHAR(50) UNIQUE NOT NULL,
    certification_type VARCHAR(20) NOT NULL,  -- 'food', 'livestock'
    certification_agency VARCHAR(100) NOT NULL,
    product_category VARCHAR(100),
    product_name TEXT,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'active',  -- 'active', 'expired', 'suspended'
    certificate_file_url TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE INDEX idx_haccp_number ON haccp_certifications(certification_number);
CREATE INDEX idx_haccp_company ON haccp_certifications(company_id);
CREATE INDEX idx_haccp_expiry ON haccp_certifications(expiry_date);
```

---

## 4. API 구현

### 4.1 HACCP 인증 조회
```python
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from datetime import date

router = APIRouter()

class HACCPCertification(BaseModel):
    id: int
    company_name: str
    certification_number: str
    certification_type: str
    certification_agency: str
    product_category: str
    product_name: str
    issue_date: date
    expiry_date: date
    status: str
    is_valid: bool

@router.get("/api/v1/haccp/{certification_number}")
async def get_haccp_certification(certification_number: str):
    """HACCP 인증 정보 조회"""
    cert = await db.get_haccp_by_number(certification_number)
    
    if not cert:
        raise HTTPException(status_code=404, detail="인증 정보를 찾을 수 없습니다")
    
    # 유효성 검증
    is_valid = (
        cert.status == 'active' and
        cert.expiry_date >= date.today()
    )
    
    return HACCPCertification(
        **cert.dict(),
        is_valid=is_valid
    )

@router.get("/api/v1/haccp/company/{company_id}")
async def get_company_haccp(company_id: int):
    """업체의 모든 HACCP 인증 조회"""
    certs = await db.get_haccp_by_company(company_id)
    return certs

@router.get("/api/v1/haccp/search")
async def search_haccp(
    company_name: str = None,
    product_name: str = None,
    status: str = 'active'
):
    """HACCP 인증 검색"""
    results = await db.search_haccp(
        company_name=company_name,
        product_name=product_name,
        status=status
    )
    return results
```

---

## 5. 제품 페이지 표시

### 5.1 HACCP 배지 컴포넌트 (React)
```typescript
// HACCPBadge.tsx
interface HACCPBadgeProps {
  certificationNumber: string;
  isValid: boolean;
  expiryDate: string;
}

export const HACCPBadge: React.FC<HACCPBadgeProps> = ({
  certificationNumber,
  isValid,
  expiryDate
}) => {
  return (
    <div className={`haccp-badge ${isValid ? 'valid' : 'expired'}`}>
      <img src="/images/haccp-logo.png" alt="HACCP 인증" />
      <div className="haccp-info">
        <span className="haccp-label">HACCP 인증</span>
        <span className="haccp-number">{certificationNumber}</span>
        <span className="haccp-expiry">
          유효기간: {new Date(expiryDate).toLocaleDateString('ko-KR')}
        </span>
      </div>
      {isValid && (
        <div className="haccp-verified">
          <CheckIcon /> 인증 유효
        </div>
      )}
    </div>
  );
};
```

### 5.2 스타일링 (CSS)
```css
.haccp-badge {
  display: flex;
  align-items: center;
  padding: 12px;
  border: 2px solid #4CAF50;
  border-radius: 8px;
  background: #f9f9f9;
  margin: 16px 0;
}

.haccp-badge.expired {
  border-color: #999;
  opacity: 0.6;
}

.haccp-badge img {
  width: 60px;
  height: 60px;
  margin-right: 12px;
}

.haccp-info {
  display: flex;
  flex-direction: column;
}

.haccp-label {
  font-weight: bold;
  color: #4CAF50;
  font-size: 14px;
}

.haccp-number {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.haccp-expiry {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}

.haccp-verified {
  margin-left: auto;
  display: flex;
  align-items: center;
  color: #4CAF50;
  font-weight: bold;
}
```

---

## 6. 자동 갱신 알림

### 6.1 만료 예정 알림
```python
from datetime import datetime, timedelta
import asyncio

async def check_expiring_certifications():
    """만료 예정 HACCP 인증 확인 (30일 전)"""
    threshold_date = datetime.now().date() + timedelta(days=30)
    
    expiring_certs = await db.query(
        """
        SELECT c.email, h.certification_number, h.expiry_date, co.name
        FROM haccp_certifications h
        JOIN companies co ON h.company_id = co.id
        JOIN company_users cu ON co.id = cu.company_id
        JOIN users u ON cu.user_id = u.id
        WHERE h.expiry_date <= $1 
        AND h.expiry_date >= $2
        AND h.status = 'active'
        """,
        threshold_date,
        datetime.now().date()
    )
    
    for cert in expiring_certs:
        await send_expiry_notification(
            email=cert['email'],
            company_name=cert['name'],
            cert_number=cert['certification_number'],
            expiry_date=cert['expiry_date']
        )

# 일일 크론잡
@app.on_event("startup")
async def schedule_cert_check():
    scheduler = AsyncIOScheduler()
    scheduler.add_job(
        check_expiring_certifications,
        'cron',
        hour=9,  # 매일 오전 9시
        minute=0
    )
    scheduler.start()
```

---

## 7. 다국어 지원

### 7.1 HACCP 번역
```json
{
  "ko": {
    "haccp": {
      "title": "HACCP 인증",
      "certified": "인증 제품",
      "valid": "인증 유효",
      "expired": "인증 만료",
      "number": "인증번호",
      "expiry": "유효기간",
      "agency": "인증기관",
      "description": "식품안전관리인증기준"
    }
  },
  "en": {
    "haccp": {
      "title": "HACCP Certification",
      "certified": "Certified Product",
      "valid": "Valid Certification",
      "expired": "Expired",
      "number": "Certification No.",
      "expiry": "Valid Until",
      "agency": "Certifying Body",
      "description": "Hazard Analysis Critical Control Point"
    }
  },
  "zh": {
    "haccp": {
      "title": "HACCP认证",
      "certified": "认证产品",
      "valid": "有效认证",
      "expired": "已过期",
      "number": "认证编号",
      "expiry": "有效期至",
      "agency": "认证机构",
      "description": "危害分析与关键控制点"
    }
  },
  "ja": {
    "haccp": {
      "title": "HACCP認証",
      "certified": "認証製品",
      "valid": "有効な認証",
      "expired": "期限切れ",
      "number": "認証番号",
      "expiry": "有効期限",
      "agency": "認証機関",
      "description": "危害分析重要管理点"
    }
  }
}
```

---

**문서 관리**
- 작성자: [담당자명]
- 최종 업데이트: 2025-11-19



