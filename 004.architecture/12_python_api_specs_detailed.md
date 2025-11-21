# Python Flask API 상세 명세서
# 데이터 분석 및 ML 서비스

**문서 버전**: v1.0  
**작성일**: 2025-11-20  
**프로젝트**: K-Food 거래 플랫폼 (XLCfi)  
**Base URL**: `https://api.spicyjump.com/v1/analytics`

---

## 목차

1. [API 개요](#1-api-개요)
2. [분석 서비스 (Analytics Service)](#2-분석-서비스)
3. [추천 서비스 (Recommendation Service)](#3-추천-서비스)
4. [ML 서비스 (ML Service)](#4-ml-서비스)
5. [이미지 처리 서비스 (Image Service)](#5-이미지-처리-서비스)
6. [리포트 서비스 (Report Service)](#6-리포트-서비스)
7. [ETL 서비스 (ETL Service)](#7-etl-서비스)

---

## 1. API 개요

### 1.1 서비스 구조

```
Python Services
├── Analytics Service (Port: 5001)    # 데이터 분석
├── Recommendation Service (Port: 5002) # 추천 시스템
├── ML Service (Port: 5003)            # ML 모델
├── Image Service (Port: 5004)         # 이미지 처리
├── Report Service (Port: 5005)        # 리포트 생성
└── ETL Service (Port: 5006)           # 데이터 ETL
```

### 1.2 공통 사항

#### Request Headers
```http
Content-Type: application/json
Accept: application/json
X-Internal-Token: {internal_service_token}
X-Request-ID: {unique_request_id}
```

#### Response Format (Flask 표준)
```json
{
  "success": true,
  "data": {},
  "message": "Success",
  "timestamp": "2025-11-20T10:00:00Z",
  "request_id": "req-123-abc"
}
```

#### Error Response Format
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {}
  },
  "timestamp": "2025-11-20T10:00:00Z",
  "request_id": "req-123-abc"
}
```

### 1.3 인증 방식

Python 서비스는 **내부 서비스 전용**이므로 JWT 기반 내부 인증을 사용합니다.

```python
# Python - 내부 인증 데코레이터
from functools import wraps
from flask import request, jsonify
import jwt

INTERNAL_SECRET = os.getenv('INTERNAL_JWT_SECRET')

def require_internal_auth(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        token = request.headers.get('X-Internal-Token', '')
        
        if not token:
            return jsonify({
                'success': False,
                'error': {'code': 'MISSING_TOKEN', 'message': 'Internal token required'}
            }), 401
        
        try:
            jwt.decode(token, INTERNAL_SECRET, algorithms=['HS256'])
        except jwt.InvalidTokenError:
            return jsonify({
                'success': False,
                'error': {'code': 'INVALID_TOKEN', 'message': 'Invalid internal token'}
            }), 401
        
        return f(*args, **kwargs)
    
    return decorated
```

---

## 2. 분석 서비스 (Analytics Service)

**Base Path**: `/v1/analytics`  
**Port**: 5001  
**책임**: 매출 분석, 사용자 행동 분석, 대시보드 데이터

### 2.1 대시보드 통계

#### Endpoint
```http
GET /v1/analytics/dashboard
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| period | string | N | 기간 (day, week, month, year) |
| start_date | date | N | 시작일 (YYYY-MM-DD) |
| end_date | date | N | 종료일 (YYYY-MM-DD) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "period": "month",
    "date_range": {
      "start": "2025-10-21",
      "end": "2025-11-20"
    },
    "revenue": {
      "total": 50000.00,
      "currency": "USD",
      "growth_rate": 15.5,
      "daily_average": 1666.67,
      "trend": "increasing"
    },
    "orders": {
      "total": 1000,
      "completed": 900,
      "cancelled": 40,
      "pending": 60,
      "conversion_rate": 90.0,
      "average_order_value": 50.00
    },
    "users": {
      "total_active": 500,
      "new_users": 50,
      "returning_users": 450,
      "retention_rate": 75.0
    },
    "products": {
      "total_sold": 2000,
      "unique_products": 150,
      "average_rating": 4.5
    },
    "daily_trend": [
      {
        "date": "2025-11-20",
        "revenue": 2000.00,
        "orders": 40,
        "users": 30
      }
    ],
    "top_categories": [
      {
        "category_id": 1,
        "category_name": "김치/반찬",
        "sales_count": 500,
        "revenue": 7500.00
      }
    ]
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### Python Flask Implementation
```python
from flask import Flask, request, jsonify
from sqlalchemy import create_engine, text
import pandas as pd
from datetime import datetime, timedelta
from typing import Dict, Any

app = Flask(__name__)
engine = create_engine(DATABASE_URL)

@app.route('/v1/analytics/dashboard', methods=['GET'])
@require_internal_auth
def get_dashboard_stats():
    """대시보드 통계 데이터 생성"""
    period = request.args.get('period', 'month')
    start_date, end_date = get_date_range(period)
    
    # 매출 분석
    revenue_stats = calculate_revenue_stats(start_date, end_date)
    
    # 주문 분석
    order_stats = calculate_order_stats(start_date, end_date)
    
    # 사용자 분석
    user_stats = calculate_user_stats(start_date, end_date)
    
    # 상품 분석
    product_stats = calculate_product_stats(start_date, end_date)
    
    # 일별 트렌드
    daily_trend = calculate_daily_trend(start_date, end_date)
    
    # 인기 카테고리
    top_categories = get_top_categories(start_date, end_date)
    
    return jsonify({
        'success': True,
        'data': {
            'period': period,
            'date_range': {
                'start': start_date.isoformat(),
                'end': end_date.isoformat()
            },
            'revenue': revenue_stats,
            'orders': order_stats,
            'users': user_stats,
            'products': product_stats,
            'daily_trend': daily_trend,
            'top_categories': top_categories
        },
        'timestamp': datetime.utcnow().isoformat()
    }), 200

def calculate_revenue_stats(start_date, end_date) -> Dict[str, Any]:
    """매출 통계 계산"""
    query = text("""
        SELECT 
            SUM(total) as total_revenue,
            COUNT(*) as order_count,
            AVG(total) as avg_order_value,
            currency
        FROM orders
        WHERE created_at BETWEEN :start_date AND :end_date
        AND status = 'DELIVERED'
        GROUP BY currency
    """)
    
    with engine.connect() as conn:
        result = conn.execute(query, {
            'start_date': start_date,
            'end_date': end_date
        }).fetchone()
    
    if not result:
        return {
            'total': 0.0,
            'growth_rate': 0.0,
            'daily_average': 0.0,
            'trend': 'stable'
        }
    
    total_revenue = float(result.total_revenue or 0)
    
    # 이전 기간 매출 (성장률 계산)
    prev_period_revenue = get_previous_period_revenue(start_date, end_date)
    growth_rate = calculate_growth_rate(total_revenue, prev_period_revenue)
    
    # 일평균 매출
    days = (end_date - start_date).days
    daily_average = total_revenue / days if days > 0 else 0
    
    # 트렌드 분석
    trend = 'increasing' if growth_rate > 5 else 'decreasing' if growth_rate < -5 else 'stable'
    
    return {
        'total': round(total_revenue, 2),
        'currency': result.currency or 'USD',
        'growth_rate': round(growth_rate, 2),
        'daily_average': round(daily_average, 2),
        'trend': trend
    }

def calculate_order_stats(start_date, end_date) -> Dict[str, Any]:
    """주문 통계 계산"""
    query = text("""
        SELECT 
            status,
            COUNT(*) as count,
            SUM(total) as revenue
        FROM orders
        WHERE created_at BETWEEN :start_date AND :end_date
        GROUP BY status
    """)
    
    with engine.connect() as conn:
        results = conn.execute(query, {
            'start_date': start_date,
            'end_date': end_date
        }).fetchall()
    
    stats = {
        'total': 0,
        'completed': 0,
        'cancelled': 0,
        'pending': 0,
        'conversion_rate': 0.0,
        'average_order_value': 0.0
    }
    
    total_revenue = 0.0
    
    for row in results:
        status = row.status
        count = row.count
        revenue = float(row.revenue or 0)
        
        stats['total'] += count
        total_revenue += revenue
        
        if status == 'DELIVERED':
            stats['completed'] = count
        elif status == 'CANCELLED':
            stats['cancelled'] = count
        elif status in ['PENDING', 'CONFIRMED', 'PREPARING', 'SHIPPED']:
            stats['pending'] += count
    
    # 전환율 계산
    if stats['total'] > 0:
        stats['conversion_rate'] = round((stats['completed'] / stats['total']) * 100, 2)
    
    # 평균 주문 금액
    if stats['total'] > 0:
        stats['average_order_value'] = round(total_revenue / stats['total'], 2)
    
    return stats

def calculate_daily_trend(start_date, end_date) -> list:
    """일별 트렌드 계산"""
    query = text("""
        SELECT 
            DATE(created_at) as date,
            SUM(total) as revenue,
            COUNT(*) as orders,
            COUNT(DISTINCT buyer_id) as users
        FROM orders
        WHERE created_at BETWEEN :start_date AND :end_date
        AND status = 'DELIVERED'
        GROUP BY DATE(created_at)
        ORDER BY date
    """)
    
    with engine.connect() as conn:
        results = conn.execute(query, {
            'start_date': start_date,
            'end_date': end_date
        }).fetchall()
    
    return [
        {
            'date': row.date.isoformat(),
            'revenue': round(float(row.revenue), 2),
            'orders': row.orders,
            'users': row.users
        }
        for row in results
    ]

def get_top_categories(start_date, end_date, limit=10) -> list:
    """인기 카테고리 조회"""
    query = text("""
        SELECT 
            c.id as category_id,
            c.name as category_name,
            COUNT(oi.id) as sales_count,
            SUM(oi.subtotal) as revenue
        FROM categories c
        JOIN products p ON c.id = p.category_id
        JOIN order_items oi ON p.id = oi.product_id
        JOIN orders o ON oi.order_id = o.id
        WHERE o.created_at BETWEEN :start_date AND :end_date
        AND o.status = 'DELIVERED'
        GROUP BY c.id, c.name
        ORDER BY revenue DESC
        LIMIT :limit
    """)
    
    with engine.connect() as conn:
        results = conn.execute(query, {
            'start_date': start_date,
            'end_date': end_date,
            'limit': limit
        }).fetchall()
    
    return [
        {
            'category_id': row.category_id,
            'category_name': row.category_name,
            'sales_count': row.sales_count,
            'revenue': round(float(row.revenue), 2)
        }
        for row in results
    ]
```

---

### 2.2 사용자 행동 분석

#### Endpoint
```http
GET /v1/analytics/users/{user_id}/behavior
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| user_id | integer | 사용자 ID |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "user_id": 3,
    "behavior_score": 85.5,
    "purchase_history": {
      "total_orders": 10,
      "total_spent": 500.00,
      "average_order_value": 50.00,
      "last_purchase_date": "2025-11-15T10:00:00Z",
      "purchase_frequency": "high"
    },
    "favorite_categories": [
      {
        "category_id": 1,
        "category_name": "김치/반찬",
        "purchase_count": 5
      }
    ],
    "browsing_patterns": {
      "most_viewed_category": "김치/반찬",
      "average_session_duration": 180,
      "total_page_views": 100,
      "search_keywords": ["김치", "고추장", "라면"]
    },
    "engagement": {
      "reviews_written": 5,
      "average_rating_given": 4.5,
      "wishlist_items": 3,
      "cart_abandonment_rate": 20.0
    },
    "churn_prediction": {
      "risk_level": "low",
      "probability": 0.15,
      "last_activity_days": 5
    },
    "next_best_actions": [
      {
        "action": "recommend_products",
        "category": "김치/반찬",
        "priority": "high"
      },
      {
        "action": "send_promotion",
        "discount_percentage": 10,
        "priority": "medium"
      }
    ]
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### Python Implementation with ML
```python
from sklearn.ensemble import RandomForestClassifier
import numpy as np

@app.route('/v1/analytics/users/<int:user_id>/behavior', methods=['GET'])
@require_internal_auth
def get_user_behavior(user_id):
    """사용자 행동 분석"""
    
    # 구매 이력 조회
    purchase_history = get_purchase_history(user_id)
    
    # 선호 카테고리 분석
    favorite_categories = get_favorite_categories(user_id)
    
    # 브라우징 패턴 분석
    browsing_patterns = analyze_browsing_patterns(user_id)
    
    # 참여도 분석
    engagement = calculate_engagement_metrics(user_id)
    
    # 이탈 예측 (ML 모델)
    churn_prediction = predict_churn(user_id)
    
    # 행동 점수 계산
    behavior_score = calculate_behavior_score(
        purchase_history,
        engagement,
        churn_prediction
    )
    
    # 다음 액션 추천
    next_best_actions = recommend_next_actions(
        user_id,
        behavior_score,
        churn_prediction
    )
    
    return jsonify({
        'success': True,
        'data': {
            'user_id': user_id,
            'behavior_score': round(behavior_score, 2),
            'purchase_history': purchase_history,
            'favorite_categories': favorite_categories,
            'browsing_patterns': browsing_patterns,
            'engagement': engagement,
            'churn_prediction': churn_prediction,
            'next_best_actions': next_best_actions
        },
        'timestamp': datetime.utcnow().isoformat()
    }), 200

def predict_churn(user_id: int) -> Dict[str, Any]:
    """이탈 예측 (ML 모델)"""
    # 특징 추출
    features = extract_user_features(user_id)
    
    # 사전 학습된 모델 로드
    model = load_churn_model()
    
    # 예측
    churn_probability = model.predict_proba([features])[0][1]
    
    # 위험도 분류
    if churn_probability < 0.3:
        risk_level = 'low'
    elif churn_probability < 0.6:
        risk_level = 'medium'
    else:
        risk_level = 'high'
    
    # 마지막 활동 일수
    last_activity_days = get_days_since_last_activity(user_id)
    
    return {
        'risk_level': risk_level,
        'probability': round(float(churn_probability), 2),
        'last_activity_days': last_activity_days
    }

def extract_user_features(user_id: int) -> np.ndarray:
    """사용자 특징 추출"""
    query = text("""
        SELECT 
            COUNT(DISTINCT o.id) as order_count,
            COALESCE(SUM(o.total), 0) as total_spent,
            COALESCE(AVG(o.total), 0) as avg_order_value,
            COUNT(DISTINCT r.id) as review_count,
            COALESCE(AVG(r.rating), 0) as avg_rating_given,
            EXTRACT(DAY FROM (NOW() - MAX(o.created_at))) as days_since_last_order
        FROM users u
        LEFT JOIN orders o ON u.id = o.buyer_id AND o.status = 'DELIVERED'
        LEFT JOIN reviews r ON u.id = r.user_id
        WHERE u.id = :user_id
        GROUP BY u.id
    """)
    
    with engine.connect() as conn:
        result = conn.execute(query, {'user_id': user_id}).fetchone()
    
    if not result:
        return np.zeros(6)
    
    return np.array([
        result.order_count or 0,
        float(result.total_spent or 0),
        float(result.avg_order_value or 0),
        result.review_count or 0,
        float(result.avg_rating_given or 0),
        result.days_since_last_order or 0
    ])
```

---

### 2.3 판매 트렌드 예측

#### Endpoint
```http
GET /v1/analytics/sales/forecast
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| days | integer | N | 예측 일수 (기본: 30) |
| product_id | integer | N | 특정 상품 예측 |
| category_id | integer | N | 특정 카테고리 예측 |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "forecast_period": {
      "start_date": "2025-11-21",
      "end_date": "2025-12-20",
      "days": 30
    },
    "historical_data": [
      {
        "date": "2025-10-21",
        "actual_sales": 1500.00,
        "order_count": 30
      }
    ],
    "forecast": [
      {
        "date": "2025-11-21",
        "predicted_sales": 1600.00,
        "predicted_orders": 32,
        "confidence_lower": 1400.00,
        "confidence_upper": 1800.00,
        "confidence_level": 0.95
      }
    ],
    "metrics": {
      "total_predicted_sales": 48000.00,
      "average_daily_sales": 1600.00,
      "growth_rate": 6.67,
      "model_accuracy": 0.92
    },
    "insights": [
      {
        "type": "trend",
        "message": "판매가 지속적으로 증가할 것으로 예상됩니다",
        "confidence": "high"
      },
      {
        "type": "seasonality",
        "message": "주말에 판매가 20% 증가하는 패턴이 있습니다",
        "confidence": "medium"
      }
    ]
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### Python Implementation with Time Series
```python
from statsmodels.tsa.holtwinters import ExponentialSmoothing
from statsmodels.tsa.arima.model import ARIMA
import pandas as pd

@app.route('/v1/analytics/sales/forecast', methods=['GET'])
@require_internal_auth
def forecast_sales():
    """판매 예측 (시계열 분석)"""
    days = int(request.args.get('days', 30))
    product_id = request.args.get('product_id', type=int)
    category_id = request.args.get('category_id', type=int)
    
    # 과거 데이터 조회
    historical_data = get_historical_sales(
        product_id=product_id,
        category_id=category_id,
        days=90  # 90일 과거 데이터
    )
    
    # 시계열 예측
    forecast_data = perform_time_series_forecast(historical_data, days)
    
    # 메트릭 계산
    metrics = calculate_forecast_metrics(forecast_data)
    
    # 인사이트 생성
    insights = generate_forecast_insights(historical_data, forecast_data)
    
    return jsonify({
        'success': True,
        'data': {
            'forecast_period': {
                'start_date': (datetime.now() + timedelta(days=1)).date().isoformat(),
                'end_date': (datetime.now() + timedelta(days=days)).date().isoformat(),
                'days': days
            },
            'historical_data': historical_data,
            'forecast': forecast_data,
            'metrics': metrics,
            'insights': insights
        },
        'timestamp': datetime.utcnow().isoformat()
    }), 200

def perform_time_series_forecast(historical_data: list, forecast_days: int) -> list:
    """시계열 예측 수행"""
    # DataFrame 생성
    df = pd.DataFrame(historical_data)
    df['date'] = pd.to_datetime(df['date'])
    df.set_index('date', inplace=True)
    
    # 시계열 데이터
    sales_series = df['actual_sales']
    
    # Holt-Winters 지수 평활법 적용
    model = ExponentialSmoothing(
        sales_series,
        trend='add',
        seasonal='add',
        seasonal_periods=7  # 주간 계절성
    )
    fitted_model = model.fit()
    
    # 예측
    forecast = fitted_model.forecast(steps=forecast_days)
    
    # 신뢰 구간 계산 (단순화)
    std_error = sales_series.std()
    confidence_interval = 1.96 * std_error  # 95% 신뢰구간
    
    # 결과 포맷팅
    forecast_data = []
    for i, (date, predicted_sales) in enumerate(forecast.items()):
        forecast_data.append({
            'date': date.isoformat(),
            'predicted_sales': round(float(predicted_sales), 2),
            'predicted_orders': int(predicted_sales / 50),  # 평균 주문 금액 가정
            'confidence_lower': round(float(predicted_sales - confidence_interval), 2),
            'confidence_upper': round(float(predicted_sales + confidence_interval), 2),
            'confidence_level': 0.95
        })
    
    return forecast_data

def generate_forecast_insights(historical_data: list, forecast_data: list) -> list:
    """예측 인사이트 생성"""
    insights = []
    
    # 트렌드 분석
    historical_avg = np.mean([d['actual_sales'] for d in historical_data])
    forecast_avg = np.mean([d['predicted_sales'] for d in forecast_data])
    growth_rate = ((forecast_avg - historical_avg) / historical_avg) * 100
    
    if growth_rate > 5:
        insights.append({
            'type': 'trend',
            'message': f'판매가 {growth_rate:.1f}% 증가할 것으로 예상됩니다',
            'confidence': 'high'
        })
    elif growth_rate < -5:
        insights.append({
            'type': 'trend',
            'message': f'판매가 {abs(growth_rate):.1f}% 감소할 것으로 예상됩니다',
            'confidence': 'high'
        })
    
    # 계절성 패턴 분석
    df = pd.DataFrame(historical_data)
    df['date'] = pd.to_datetime(df['date'])
    df['day_of_week'] = df['date'].dt.dayofweek
    
    weekend_avg = df[df['day_of_week'].isin([5, 6])]['actual_sales'].mean()
    weekday_avg = df[~df['day_of_week'].isin([5, 6])]['actual_sales'].mean()
    
    if weekend_avg > weekday_avg * 1.2:
        insights.append({
            'type': 'seasonality',
            'message': '주말에 판매가 증가하는 패턴이 있습니다',
            'confidence': 'medium'
        })
    
    return insights
```

---

## 3. 추천 서비스 (Recommendation Service)

**Base Path**: `/v1/recommendations`  
**Port**: 5002  
**책임**: 상품 추천, 개인화 추천

### 3.1 개인화 상품 추천

#### Endpoint
```http
GET /v1/recommendations/users/{user_id}/products
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| limit | integer | N | 추천 개수 (기본: 10) |
| algorithm | string | N | 알고리즘 (collaborative, content_based, hybrid) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "user_id": 3,
    "algorithm": "hybrid",
    "recommendations": [
      {
        "product_id": 5,
        "product_name": "깍두기 500g",
        "product_name_en": "Radish Kimchi 500g",
        "score": 0.95,
        "reason": "이전 구매 패턴 기반",
        "category_id": 6,
        "price": 12.00,
        "rating_average": 4.7,
        "image_url": "https://s3.../image.jpg"
      }
    ],
    "metadata": {
      "total_recommendations": 10,
      "confidence_score": 0.88,
      "generated_at": "2025-11-20T10:00:00Z",
      "model_version": "v1.2.0"
    }
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### Python Implementation with Collaborative Filtering
```python
from sklearn.neighbors import NearestNeighbors
from scipy.sparse import csr_matrix
import numpy as np

@app.route('/v1/recommendations/users/<int:user_id>/products', methods=['GET'])
@require_internal_auth
def recommend_products(user_id):
    """사용자 맞춤 상품 추천"""
    limit = int(request.args.get('limit', 10))
    algorithm = request.args.get('algorithm', 'hybrid')
    
    if algorithm == 'collaborative':
        recommendations = collaborative_filtering(user_id, limit)
    elif algorithm == 'content_based':
        recommendations = content_based_filtering(user_id, limit)
    else:  # hybrid
        collab_recs = collaborative_filtering(user_id, limit // 2)
        content_recs = content_based_filtering(user_id, limit // 2)
        recommendations = merge_recommendations(collab_recs, content_recs)
    
    # 상품 상세 정보 가져오기 (Java Product Service 호출)
    product_details = fetch_product_details([r['product_id'] for r in recommendations])
    
    # 결과 병합
    for rec in recommendations:
        product = product_details.get(rec['product_id'], {})
        rec.update({
            'product_name': product.get('name'),
            'product_name_en': product.get('name_en'),
            'price': product.get('price'),
            'rating_average': product.get('rating_average'),
            'image_url': product.get('images', [{}])[0].get('url')
        })
    
    return jsonify({
        'success': True,
        'data': {
            'user_id': user_id,
            'algorithm': algorithm,
            'recommendations': recommendations,
            'metadata': {
                'total_recommendations': len(recommendations),
                'confidence_score': calculate_confidence(recommendations),
                'generated_at': datetime.utcnow().isoformat(),
                'model_version': 'v1.2.0'
            }
        },
        'timestamp': datetime.utcnow().isoformat()
    }), 200

def collaborative_filtering(user_id: int, limit: int) -> list:
    """협업 필터링 추천"""
    # 사용자-상품 매트릭스 생성
    user_item_matrix = build_user_item_matrix()
    
    # 사용자 벡터 추출
    user_vector = user_item_matrix[user_id]
    
    # KNN 모델로 유사 사용자 찾기
    knn = NearestNeighbors(n_neighbors=20, metric='cosine')
    knn.fit(user_item_matrix)
    
    distances, indices = knn.kneighbors([user_vector], n_neighbors=20)
    similar_users = indices[0][1:]  # 자기 자신 제외
    
    # 유사 사용자들이 구매한 상품 수집
    recommended_products = {}
    
    for similar_user_id in similar_users:
        products = get_user_purchased_products(similar_user_id)
        for product_id, rating in products.items():
            if product_id not in get_user_purchased_products(user_id):
                if product_id not in recommended_products:
                    recommended_products[product_id] = []
                recommended_products[product_id].append(rating)
    
    # 점수 계산 (평균 평점)
    recommendations = []
    for product_id, ratings in recommended_products.items():
        score = np.mean(ratings)
        recommendations.append({
            'product_id': product_id,
            'score': round(float(score), 2),
            'reason': '유사한 사용자들이 구매한 상품'
        })
    
    # 점수 기준 정렬
    recommendations.sort(key=lambda x: x['score'], reverse=True)
    
    return recommendations[:limit]

def content_based_filtering(user_id: int, limit: int) -> list:
    """컨텐츠 기반 추천"""
    # 사용자가 구매한 상품 조회
    purchased_products = get_user_purchased_products(user_id)
    
    if not purchased_products:
        # 구매 이력이 없으면 인기 상품 추천
        return get_popular_products(limit)
    
    # 구매한 상품들의 특징 추출
    product_features = []
    for product_id in purchased_products.keys():
        features = extract_product_features(product_id)
        product_features.append(features)
    
    # 평균 특징 벡터 계산
    avg_features = np.mean(product_features, axis=0)
    
    # 전체 상품과 유사도 계산
    all_products = get_all_products()
    similarities = []
    
    for product in all_products:
        if product['id'] not in purchased_products:
            product_vector = extract_product_features(product['id'])
            similarity = cosine_similarity([avg_features], [product_vector])[0][0]
            similarities.append({
                'product_id': product['id'],
                'score': round(float(similarity), 2),
                'reason': '이전 구매 패턴과 유사'
            })
    
    # 유사도 기준 정렬
    similarities.sort(key=lambda x: x['score'], reverse=True)
    
    return similarities[:limit]

def extract_product_features(product_id: int) -> np.ndarray:
    """상품 특징 벡터 추출"""
    query = text("""
        SELECT 
            p.category_id,
            p.price,
            p.rating_average,
            COUNT(DISTINCT oi.order_id) as popularity
        FROM products p
        LEFT JOIN order_items oi ON p.id = oi.product_id
        WHERE p.id = :product_id
        GROUP BY p.id, p.category_id, p.price, p.rating_average
    """)
    
    with engine.connect() as conn:
        result = conn.execute(query, {'product_id': product_id}).fetchone()
    
    if not result:
        return np.zeros(4)
    
    # 특징 정규화
    return np.array([
        result.category_id / 100.0,  # 카테고리 정규화
        float(result.price) / 100.0,  # 가격 정규화
        float(result.rating_average) / 5.0,  # 평점 정규화
        result.popularity / 1000.0  # 인기도 정규화
    ])

def fetch_product_details(product_ids: list) -> dict:
    """Java Product Service에서 상품 정보 가져오기"""
    import requests
    
    url = f"{JAVA_PRODUCT_SERVICE_URL}/api/internal/products/batch"
    headers = {
        'X-Internal-Token': generate_internal_token(),
        'Content-Type': 'application/json'
    }
    
    try:
        response = requests.post(
            url,
            json={'product_ids': product_ids},
            headers=headers,
            timeout=5
        )
        response.raise_for_status()
        
        products = response.json().get('data', {}).get('products', [])
        return {p['id']: p for p in products}
    
    except requests.exceptions.RequestException as e:
        logger.error(f"Failed to fetch product details: {e}")
        return {}
```

---

### 3.2 연관 상품 추천

#### Endpoint
```http
GET /v1/recommendations/products/{product_id}/related
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "product_id": 1,
    "related_products": [
      {
        "product_id": 7,
        "product_name": "고추장 500g",
        "similarity_score": 0.85,
        "relation_type": "frequently_bought_together"
      }
    ]
  }
}
```

---

## 4. ML 서비스 (ML Service)

**Base Path**: `/v1/ml`  
**Port**: 5003  
**책임**: ML 모델 훈련, 예측, 평가

### 4.1 가격 최적화 예측

#### Endpoint
```http
POST /v1/ml/pricing/optimize
```

#### Request Body
```json
{
  "product_id": 1,
  "current_price": 15.00,
  "target_metric": "revenue",
  "constraints": {
    "min_price": 12.00,
    "max_price": 20.00
  }
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "product_id": 1,
    "current_price": 15.00,
    "optimal_price": 16.50,
    "predicted_impact": {
      "revenue_increase": 12.5,
      "sales_volume_change": -5.0,
      "profit_increase": 15.0
    },
    "price_elasticity": -1.2,
    "confidence": 0.87
  }
}
```

---

### 4.2 리뷰 감정 분석

#### Endpoint
```http
POST /v1/ml/sentiment/analyze
```

#### Request Body
```json
{
  "review_id": 1,
  "text": "정말 맛있어요! 배송도 빠르고 친절하네요.",
  "language": "ko"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "review_id": 1,
    "sentiment": "positive",
    "confidence": 0.95,
    "scores": {
      "positive": 0.95,
      "neutral": 0.04,
      "negative": 0.01
    },
    "keywords": [
      {"word": "맛있다", "importance": 0.8},
      {"word": "빠르다", "importance": 0.6},
      {"word": "친절하다", "importance": 0.7}
    ],
    "aspects": {
      "taste": {"sentiment": "positive", "score": 0.9},
      "delivery": {"sentiment": "positive", "score": 0.85},
      "service": {"sentiment": "positive", "score": 0.9}
    }
  }
}
```

#### Python Implementation with Transformers
```python
from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification
from konlpy.tag import Okt

# 모델 로드 (서버 시작 시)
sentiment_analyzer = pipeline(
    "sentiment-analysis",
    model="beomi/KcELECTRA-base-v2022",
    tokenizer="beomi/KcELECTRA-base-v2022"
)
okt = Okt()

@app.route('/v1/ml/sentiment/analyze', methods=['POST'])
@require_internal_auth
def analyze_sentiment():
    """리뷰 감정 분석"""
    data = request.get_json()
    review_id = data.get('review_id')
    text = data.get('text')
    language = data.get('language', 'ko')
    
    # 감정 분석
    sentiment_result = sentiment_analyzer(text)[0]
    
    # 키워드 추출
    keywords = extract_keywords(text, language)
    
    # 측면 기반 감정 분석 (ABSA)
    aspects = analyze_aspects(text, language)
    
    # 감정 레이블 변환
    sentiment_label = map_sentiment_label(sentiment_result['label'])
    
    return jsonify({
        'success': True,
        'data': {
            'review_id': review_id,
            'sentiment': sentiment_label,
            'confidence': round(sentiment_result['score'], 2),
            'scores': calculate_sentiment_scores(sentiment_result),
            'keywords': keywords,
            'aspects': aspects
        },
        'timestamp': datetime.utcnow().isoformat()
    }), 200

def extract_keywords(text: str, language: str = 'ko', top_n: int = 10) -> list:
    """키워드 추출"""
    if language == 'ko':
        # 형태소 분석
        nouns = okt.nouns(text)
        
        # 빈도 계산
        from collections import Counter
        word_counts = Counter(nouns)
        
        # 상위 키워드
        keywords = []
        for word, count in word_counts.most_common(top_n):
            if len(word) > 1:  # 한 글자 제외
                keywords.append({
                    'word': word,
                    'importance': min(count / len(nouns), 1.0)
                })
        
        return keywords
    
    return []

def analyze_aspects(text: str, language: str = 'ko') -> dict:
    """측면 기반 감정 분석 (ABSA - Aspect-Based Sentiment Analysis)"""
    aspects = {
        'taste': {'keywords': ['맛', '맛있다', '맛없다', '맛이', '풍미']},
        'delivery': {'keywords': ['배송', '배달', '빠르다', '늦다', '도착']},
        'service': {'keywords': ['친절', '서비스', '응대', '고객', '직원']},
        'quality': {'keywords': ['품질', '신선', '상태', '좋다', '나쁘다']},
        'packaging': {'keywords': ['포장', '박스', '깨지다', '손상']}
    }
    
    results = {}
    
    for aspect, config in aspects.items():
        # 해당 측면 관련 문장 추출
        aspect_sentences = []
        for keyword in config['keywords']:
            if keyword in text:
                # 키워드가 포함된 문장 추출
                sentences = text.split('.')
                for sentence in sentences:
                    if keyword in sentence:
                        aspect_sentences.append(sentence.strip())
        
        if aspect_sentences:
            # 측면별 감정 분석
            aspect_text = ' '.join(aspect_sentences)
            sentiment_result = sentiment_analyzer(aspect_text)[0]
            
            results[aspect] = {
                'sentiment': map_sentiment_label(sentiment_result['label']),
                'score': round(sentiment_result['score'], 2)
            }
    
    return results

def map_sentiment_label(label: str) -> str:
    """감정 레이블 매핑"""
    label_mapping = {
        'LABEL_0': 'negative',
        'LABEL_1': 'neutral',
        'LABEL_2': 'positive',
        'negative': 'negative',
        'neutral': 'neutral',
        'positive': 'positive'
    }
    return label_mapping.get(label, 'neutral')
```

---

## 5. 이미지 처리 서비스 (Image Service)

**Base Path**: `/v1/images`  
**Port**: 5004  
**책임**: 이미지 분석, OCR, 품질 검증

### 5.1 이미지 품질 검증

#### Endpoint
```http
POST /v1/images/validate
```

#### Request Body
```json
{
  "image_url": "https://s3.../product.jpg",
  "checks": ["resolution", "brightness", "blur", "content"]
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "is_valid": true,
    "quality_score": 0.85,
    "checks": {
      "resolution": {
        "passed": true,
        "width": 1920,
        "height": 1080,
        "min_required": 800
      },
      "brightness": {
        "passed": true,
        "level": 0.65,
        "optimal_range": [0.4, 0.8]
      },
      "blur": {
        "passed": true,
        "score": 0.92,
        "threshold": 0.7
      },
      "content": {
        "passed": true,
        "detected_objects": ["food", "container"],
        "confidence": 0.89
      }
    },
    "recommendations": []
  }
}
```

#### Python Implementation with OpenCV
```python
import cv2
import numpy as np
from PIL import Image
import requests
from io import BytesIO

@app.route('/v1/images/validate', methods=['POST'])
@require_internal_auth
def validate_image():
    """이미지 품질 검증"""
    data = request.get_json()
    image_url = data.get('image_url')
    checks = data.get('checks', ['resolution', 'brightness', 'blur', 'content'])
    
    # 이미지 다운로드
    image_data = download_image(image_url)
    if image_data is None:
        return jsonify({
            'success': False,
            'error': {'code': 'IMAGE_DOWNLOAD_FAILED', 'message': '이미지를 다운로드할 수 없습니다'}
        }), 400
    
    # 검증 수행
    results = {}
    recommendations = []
    
    if 'resolution' in checks:
        results['resolution'] = check_resolution(image_data)
        if not results['resolution']['passed']:
            recommendations.append('이미지 해상도를 800px 이상으로 올려주세요')
    
    if 'brightness' in checks:
        results['brightness'] = check_brightness(image_data)
        if not results['brightness']['passed']:
            recommendations.append('이미지가 너무 어둡거나 밝습니다')
    
    if 'blur' in checks:
        results['blur'] = check_blur(image_data)
        if not results['blur']['passed']:
            recommendations.append('이미지가 흐릿합니다. 선명한 이미지를 사용해주세요')
    
    if 'content' in checks:
        results['content'] = check_content(image_data)
    
    # 전체 품질 점수 계산
    quality_score = calculate_quality_score(results)
    is_valid = all(r.get('passed', False) for r in results.values())
    
    return jsonify({
        'success': True,
        'data': {
            'is_valid': is_valid,
            'quality_score': round(quality_score, 2),
            'checks': results,
            'recommendations': recommendations
        },
        'timestamp': datetime.utcnow().isoformat()
    }), 200

def download_image(url: str) -> np.ndarray:
    """이미지 다운로드"""
    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        
        image = Image.open(BytesIO(response.content))
        image_array = np.array(image)
        
        # BGR로 변환 (OpenCV 포맷)
        if len(image_array.shape) == 2:
            image_array = cv2.cvtColor(image_array, cv2.COLOR_GRAY2BGR)
        elif image_array.shape[2] == 4:
            image_array = cv2.cvtColor(image_array, cv2.COLOR_RGBA2BGR)
        elif image_array.shape[2] == 3:
            image_array = cv2.cvtColor(image_array, cv2.COLOR_RGB2BGR)
        
        return image_array
    
    except Exception as e:
        logger.error(f"Image download failed: {e}")
        return None

def check_resolution(image: np.ndarray) -> dict:
    """해상도 확인"""
    height, width = image.shape[:2]
    min_required = 800
    
    return {
        'passed': min(width, height) >= min_required,
        'width': width,
        'height': height,
        'min_required': min_required
    }

def check_brightness(image: np.ndarray) -> dict:
    """밝기 확인"""
    # 그레이스케일 변환
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    
    # 평균 밝기 계산
    brightness = np.mean(gray) / 255.0
    
    # 최적 범위
    optimal_min = 0.4
    optimal_max = 0.8
    
    return {
        'passed': optimal_min <= brightness <= optimal_max,
        'level': round(float(brightness), 2),
        'optimal_range': [optimal_min, optimal_max]
    }

def check_blur(image: np.ndarray) -> dict:
    """흐림 확인 (Laplacian Variance)"""
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    
    # Laplacian 연산자로 엣지 검출
    laplacian = cv2.Laplacian(gray, cv2.CV_64F)
    variance = laplacian.var()
    
    # 정규화 (0-1 범위)
    normalized_score = min(variance / 1000.0, 1.0)
    
    threshold = 0.7
    
    return {
        'passed': normalized_score >= threshold,
        'score': round(float(normalized_score), 2),
        'threshold': threshold
    }

def check_content(image: np.ndarray) -> dict:
    """컨텐츠 확인 (객체 탐지)"""
    # 여기서는 간단한 예시만 제공
    # 실제로는 YOLO나 다른 객체 탐지 모델 사용
    return {
        'passed': True,
        'detected_objects': ['food', 'container'],
        'confidence': 0.89
    }

def calculate_quality_score(results: dict) -> float:
    """품질 점수 계산"""
    scores = []
    
    if 'resolution' in results:
        scores.append(1.0 if results['resolution']['passed'] else 0.5)
    
    if 'brightness' in results:
        brightness_level = results['brightness']['level']
        optimal_center = 0.6
        brightness_score = 1.0 - min(abs(brightness_level - optimal_center) / 0.4, 1.0)
        scores.append(brightness_score)
    
    if 'blur' in results:
        scores.append(results['blur']['score'])
    
    if 'content' in results:
        scores.append(results['content']['confidence'])
    
    return np.mean(scores) if scores else 0.0
```

---

### 5.2 OCR (영양성분표 추출)

#### Endpoint
```http
POST /v1/images/ocr
```

#### Request Body
```json
{
  "image_url": "https://s3.../nutrition_label.jpg",
  "extract_type": "nutrition_facts"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "text": "영양성분표\n열량 50kcal\n나트륨 500mg\n...",
    "nutrition_facts": {
      "calories": 50,
      "sodium": 500,
      "carbohydrates": 10,
      "protein": 2,
      "fat": 1
    },
    "confidence": 0.92
  }
}
```

---

## 6. 리포트 서비스 (Report Service)

**Base Path**: `/v1/reports`  
**Port**: 5005  
**책임**: PDF/Excel 리포트 생성

### 6.1 월간 매출 리포트 생성

#### Endpoint
```http
POST /v1/reports/sales/monthly
```

#### Request Body
```json
{
  "year": 2025,
  "month": 11,
  "format": "pdf",
  "include_charts": true
}
```

#### Response (202 Accepted)
```json
{
  "success": true,
  "data": {
    "report_id": "RPT-20251120-0001",
    "status": "processing",
    "estimated_completion": "2025-11-20T10:05:00Z"
  }
}
```

---

## 7. ETL 서비스 (ETL Service)

**Base Path**: `/v1/etl`  
**Port**: 5006  
**책임**: 데이터 추출, 변환, 적재

### 7.1 데이터 동기화

#### Endpoint
```http
POST /v1/etl/sync
```

#### Request Body
```json
{
  "source": "postgresql",
  "target": "elasticsearch",
  "entity": "products",
  "mode": "incremental"
}
```

#### Response (202 Accepted)
```json
{
  "success": true,
  "data": {
    "job_id": "ETL-20251120-0001",
    "status": "started",
    "records_to_sync": 1000
  }
}
```

---

## 부록

### A. Python 공통 유틸리티

```python
# utils/response.py
from flask import jsonify
from datetime import datetime

def success_response(data, message="Success", status=200):
    """성공 응답 생성"""
    return jsonify({
        'success': True,
        'data': data,
        'message': message,
        'timestamp': datetime.utcnow().isoformat()
    }), status

def error_response(code, message, details=None, status=400):
    """에러 응답 생성"""
    error_data = {
        'success': False,
        'error': {
            'code': code,
            'message': message
        },
        'timestamp': datetime.utcnow().isoformat()
    }
    
    if details:
        error_data['error']['details'] = details
    
    return jsonify(error_data), status
```

---

**문서 작성**: AI Assistant  
**최종 업데이트**: 2025-11-20  
**다음 단계**: API 간 통신 시퀀스 다이어그램 작성

