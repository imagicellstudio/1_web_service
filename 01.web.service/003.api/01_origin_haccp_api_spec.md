# 식품 원산지 및 HACCP API 연동 명세서

## 문서 정보
- 작성일: 2025-11-19
- 버전: 1.0
- 목적: 외부 식품 관련 API 연동 가이드

---

## 1. 식품안전나라 API

### 1.1 개요
- **제공기관**: 식품의약품안전처
- **API 포털**: https://www.foodsafetykorea.go.kr/api/
- **인증방식**: API Key
- **응답형식**: XML, JSON

### 1.2 API Key 발급
1. 식품안전나라 회원가입
2. 오픈API > 인증키 신청
3. 승인 후 API Key 발급 (1-3영업일)

### 1.3 주요 API 목록

#### 1.3.1 식품영양성분 조회
```
GET /I2570/v1/json/{API_KEY}/{START_IDX}/{END_IDX}/{FOOD_NAME}

파라미터:
- API_KEY: 발급받은 인증키
- START_IDX: 시작 인덱스 (1부터 시작)
- END_IDX: 종료 인덱스 (최대 1000)
- FOOD_NAME: 식품명 (URL 인코딩)

응답 예시:
{
  "I2570": {
    "total_count": "1",
    "row": [
      {
        "DESC_KOR": "쌀밥",
        "SERVING_SIZE": "210",
        "NUTR_CONT1": "312",  // 에너지(kcal)
        "NUTR_CONT2": "7.0",  // 단백질(g)
        "NUTR_CONT3": "0.7",  // 지방(g)
        "NUTR_CONT4": "67.9", // 탄수화물(g)
        "NUTR_CONT5": "0"     // 당류(g)
      }
    ]
  }
}
```

#### 1.3.2 원산지 정보 조회
```
GET /I0750/v1/json/{API_KEY}/{START_IDX}/{END_IDX}/{PRDLST_NM}

파라미터:
- PRDLST_NM: 제품명

응답:
{
  "I0750": {
    "row": [
      {
        "PRDLST_NM": "김치",
        "RAWMTRL_NM": "배추",
        "ORIGIN_NM": "국산",
        "INDUTY_NM": "식품제조업"
      }
    ]
  }
}
```

#### 1.3.3 HACCP 인증업체 조회
```
GET /I0360/v1/json/{API_KEY}/{START_IDX}/{END_IDX}/{BSSH_NM}

파라미터:
- BSSH_NM: 업체명

응답:
{
  "I0360": {
    "row": [
      {
        "BSSH_NM": "대상주식회사",
        "INDUTY_NM": "식품제조가공업",
        "ADDR": "서울특별시...",
        "PRMS_DT": "2020-01-01",  // 인증일자
        "VALID_PD": "2023-12-31"  // 유효기간
      }
    ]
  }
}
```

#### 1.3.4 수입식품 정보 조회
```
GET /I0290/v1/json/{API_KEY}/{START_IDX}/{END_IDX}/{PRDLST_NM}

응답:
{
  "I0290": {
    "row": [
      {
        "PRDLST_NM": "초콜릿",
        "EXPRT_CNTRY_NM": "벨기에",
        "PRMS_DT": "2023-01-01",
        "PRDLST_DCNM": "카카오 함유",
        "BSSH_NM": "수입업체명"
      }
    ]
  }
}
```

---

## 2. 관세청 API

### 2.1 개요
- **제공기관**: 관세청
- **API 포털**: https://unipass.customs.go.kr/
- **인증방식**: API Key
- **응답형식**: XML, JSON

### 2.2 주요 API

#### 2.2.1 HS Code 조회
```
GET /csp-pt/openapi/getTariffInfo
  ?serviceKey={API_KEY}
  &hsCode={HS_CODE}

응답:
{
  "result": {
    "hsCode": "1006.30",
    "description": "정미",
    "tariffRate": "513%",
    "unit": "KG"
  }
}
```

#### 2.2.2 관세율 조회
```
GET /csp-pt/openapi/getTariffRate
  ?serviceKey={API_KEY}
  &hsCode={HS_CODE}
  &nationCode={NATION}

응답:
{
  "tariffInfo": {
    "basicRate": "8%",
    "ftaRate": "0%",
    "ftaName": "한-미 FTA"
  }
}
```

---

## 3. Backend API 설계

### 3.1 API 구조

```python
# FastAPI 구조
/api/v1/
  /foods/
    /{food_id}/nutrition    # 영양정보
    /{food_id}/origin       # 원산지
    /{food_id}/haccp        # HACCP 인증
  /hs-codes/
    /{hs_code}             # HS Code 조회
    /search                # HS Code 검색
  /tariffs/
    /{hs_code}             # 관세율 조회
```

### 3.2 데이터 캐싱 전략

```python
# Redis 캐싱
from functools import lru_cache
import redis
import json

redis_client = redis.Redis(host='localhost', port=6379, db=0)

def get_food_nutrition(food_name: str):
    cache_key = f"nutrition:{food_name}"
    
    # 캐시 확인
    cached = redis_client.get(cache_key)
    if cached:
        return json.loads(cached)
    
    # 외부 API 호출
    data = call_foodsafety_api(food_name)
    
    # 캐시 저장 (24시간)
    redis_client.setex(cache_key, 86400, json.dumps(data))
    
    return data
```

### 3.3 에러 처리

```python
class FoodSafetyAPIError(Exception):
    pass

async def safe_api_call(url: str, params: dict):
    try:
        response = await http_client.get(url, params=params)
        response.raise_for_status()
        return response.json()
    except httpx.HTTPStatusError as e:
        if e.response.status_code == 429:
            # Rate limit 초과
            raise FoodSafetyAPIError("API 호출 한도 초과")
        raise
    except httpx.RequestError as e:
        # 네트워크 오류
        raise FoodSafetyAPIError(f"API 연결 실패: {str(e)}")
```

---

## 4. 실무 구현 예제

### 4.1 식품 정보 통합 조회

```python
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import httpx

app = FastAPI()

class FoodInfo(BaseModel):
    name: str
    nutrition: dict
    origin: dict
    haccp: dict
    hs_code: str

@app.get("/api/v1/foods/{food_id}/complete")
async def get_complete_food_info(food_id: int):
    """식품의 모든 정보를 통합하여 조회"""
    
    # DB에서 기본 정보 조회
    food = await db.get_food(food_id)
    if not food:
        raise HTTPException(status_code=404, detail="식품을 찾을 수 없습니다")
    
    # 병렬로 외부 API 호출
    async with httpx.AsyncClient() as client:
        nutrition_task = get_nutrition_info(client, food.name)
        origin_task = get_origin_info(client, food.name)
        haccp_task = get_haccp_info(client, food.manufacturer)
        
        nutrition, origin, haccp = await asyncio.gather(
            nutrition_task, origin_task, haccp_task
        )
    
    return FoodInfo(
        name=food.name,
        nutrition=nutrition,
        origin=origin,
        haccp=haccp,
        hs_code=food.hs_code
    )

async def get_nutrition_info(client, food_name):
    """식품안전나라 API - 영양정보"""
    url = f"{FOODSAFETY_BASE_URL}/I2570/v1/json/{API_KEY}/1/100/{food_name}"
    response = await client.get(url)
    return response.json()

async def get_origin_info(client, food_name):
    """식품안전나라 API - 원산지"""
    url = f"{FOODSAFETY_BASE_URL}/I0750/v1/json/{API_KEY}/1/100/{food_name}"
    response = await client.get(url)
    return response.json()

async def get_haccp_info(client, manufacturer):
    """식품안전나라 API - HACCP"""
    url = f"{FOODSAFETY_BASE_URL}/I0360/v1/json/{API_KEY}/1/100/{manufacturer}"
    response = await client.get(url)
    return response.json()
```

### 4.2 HS Code 기반 관세 계산

```python
@app.get("/api/v1/tariffs/calculate")
async def calculate_tariff(
    hs_code: str,
    country: str,
    value: float,
    fta: str = None
):
    """관세 계산"""
    
    # 관세청 API에서 세율 조회
    tariff_rate = await get_tariff_rate(hs_code, country, fta)
    
    # 관세 계산
    tariff_amount = value * (tariff_rate / 100)
    
    return {
        "hs_code": hs_code,
        "country": country,
        "value": value,
        "tariff_rate": tariff_rate,
        "tariff_amount": tariff_amount,
        "total": value + tariff_amount,
        "fta": fta
    }
```

---

## 5. 환경 설정

### 5.1 환경 변수 (.env)
```
# 식품안전나라 API
FOODSAFETY_API_KEY=your_api_key_here
FOODSAFETY_BASE_URL=https://openapi.foodsafetykorea.go.kr/api

# 관세청 API
CUSTOMS_API_KEY=your_api_key_here
CUSTOMS_BASE_URL=https://unipass.customs.go.kr/csp-pt/openapi

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_DB=0

# API 설정
API_RATE_LIMIT=1000  # 시간당 호출 제한
API_TIMEOUT=30       # 타임아웃 (초)
```

### 5.2 설정 파일 (config.py)
```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    foodsafety_api_key: str
    foodsafety_base_url: str
    customs_api_key: str
    customs_base_url: str
    redis_host: str = "localhost"
    redis_port: int = 6379
    api_rate_limit: int = 1000
    api_timeout: int = 30
    
    class Config:
        env_file = ".env"

settings = Settings()
```

---

## 6. 테스트

### 6.1 단위 테스트
```python
import pytest
from unittest.mock import patch, AsyncMock

@pytest.mark.asyncio
async def test_get_nutrition_info():
    """영양정보 조회 테스트"""
    mock_response = {
        "I2570": {
            "row": [{
                "DESC_KOR": "쌀밥",
                "NUTR_CONT1": "312"
            }]
        }
    }
    
    with patch('httpx.AsyncClient.get') as mock_get:
        mock_get.return_value.json.return_value = mock_response
        
        result = await get_nutrition_info(client, "쌀밥")
        
        assert result["I2570"]["row"][0]["DESC_KOR"] == "쌀밥"
```

### 6.2 통합 테스트
```python
@pytest.mark.asyncio
async def test_complete_food_info():
    """통합 식품 정보 조회 테스트"""
    response = await client.get("/api/v1/foods/1/complete")
    
    assert response.status_code == 200
    data = response.json()
    assert "nutrition" in data
    assert "origin" in data
    assert "haccp" in data
```

---

## 7. 모니터링 및 로깅

### 7.1 로깅 설정
```python
import logging

logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

async def get_nutrition_info(client, food_name):
    logger.info(f"Fetching nutrition info for: {food_name}")
    try:
        result = await call_api(client, food_name)
        logger.info(f"Successfully fetched nutrition info for: {food_name}")
        return result
    except Exception as e:
        logger.error(f"Failed to fetch nutrition info: {str(e)}")
        raise
```

### 7.2 API 호출 모니터링
```python
from prometheus_client import Counter, Histogram

api_calls = Counter('api_calls_total', 'Total API calls', ['service', 'status'])
api_duration = Histogram('api_duration_seconds', 'API call duration', ['service'])

@api_duration.labels(service='foodsafety').time()
async def call_foodsafety_api(endpoint, params):
    try:
        response = await client.get(endpoint, params=params)
        api_calls.labels(service='foodsafety', status='success').inc()
        return response.json()
    except Exception as e:
        api_calls.labels(service='foodsafety', status='error').inc()
        raise
```

---

## 부록

### A. API 제한사항
- **식품안전나라**: 시간당 1000회
- **관세청**: 일일 10000회
- **동시 연결**: 최대 5개

### B. 문의처
- 식품안전나라: 1577-1255
- 관세청: 125

---

**문서 관리**
- 작성자: 장재훈
- 검토자: [검토자명]
- 최종 업데이트: 2025-11-19



