# 아키텍처 정의서 (Phase 1 MVP)

## 문서 정보
- 작성일: 2025-11-19
- 버전: 1.0 (Phase 1 MVP)
- 목적: 시스템 구성 및 배포 전략 정의

---

## 1. 시스템 아키텍처 개요

### 1.1 전체 시스템 구성도

```
┌─────────────────────────────────────────────────────────────────┐
│                         Internet / Users                         │
└──────────────────────────┬──────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────────┐
│                      CloudFlare CDN / WAF                        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
           ┌───────────────┴───────────────┐
           │                               │
           ▼                               ▼
┌──────────────────────┐        ┌──────────────────────┐
│   Frontend (S3)      │        │   Backend (EC2)      │
│   - React SPA        │        │   - Flask API        │
│   - Next.js SSR      │        │   - Gunicorn         │
└──────────────────────┘        └──────┬───────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    │                  │                  │
                    ▼                  ▼                  ▼
         ┌──────────────────┐  ┌─────────────┐  ┌──────────────┐
         │ PostgreSQL (RDS) │  │  Redis      │  │  S3 (Images) │
         │ - Primary DB     │  │  - Cache    │  │  - CDN       │
         └──────────────────┘  └─────────────┘  └──────────────┘
                    │
                    ▼
         ┌──────────────────┐
         │ RDS Read Replica │
         │ (Optional)       │
         └──────────────────┘
```

### 1.2 아키텍처 원칙

1. **단순성**: 복잡도 최소화, 표준 기술 스택 사용
2. **확장성**: 수평 확장 가능한 구조
3. **보안성**: HTTPS, JWT, WAF 적용
4. **가용성**: 자동 백업, 모니터링
5. **비용 효율**: 초기 비용 최소화, 사용량 기반 확장

---

## 2. 계층별 아키텍처

### 2.1 프레젠테이션 계층 (Frontend)

```
┌─────────────────────────────────────────┐
│         Frontend Application            │
├─────────────────────────────────────────┤
│  Components Layer                       │
│  - Header, Footer, ProductCard, etc.    │
├─────────────────────────────────────────┤
│  State Management                       │
│  - Redux Toolkit / Zustand              │
├─────────────────────────────────────────┤
│  API Client Layer                       │
│  - Axios / Fetch API                    │
├─────────────────────────────────────────┤
│  Routing                                │
│  - React Router                         │
└─────────────────────────────────────────┘
```

#### 기술 스택
- **Framework**: React 18 + Next.js 14
- **Language**: TypeScript
- **State**: Redux Toolkit 또는 Zustand
- **Styling**: Tailwind CSS + CSS Modules
- **HTTP Client**: Axios
- **Form**: React Hook Form + Yup

#### 디렉토리 구조
```
frontend/
├── public/
│   ├── images/
│   └── locales/
├── src/
│   ├── components/
│   │   ├── common/
│   │   ├── layout/
│   │   └── features/
│   ├── pages/
│   ├── hooks/
│   ├── store/
│   ├── api/
│   ├── utils/
│   ├── types/
│   └── styles/
├── package.json
└── next.config.js
```

---

### 2.2 애플리케이션 계층 (Backend)

```
┌─────────────────────────────────────────┐
│         Backend Application             │
├─────────────────────────────────────────┤
│  API Layer (Flask Routes)               │
│  - /auth, /products, /orders, etc.      │
├─────────────────────────────────────────┤
│  Business Logic Layer                   │
│  - Services, Validators                 │
├─────────────────────────────────────────┤
│  Data Access Layer                      │
│  - Models, Repositories                 │
├─────────────────────────────────────────┤
│  Infrastructure Layer                   │
│  - Database, Cache, Storage             │
└─────────────────────────────────────────┘
```

#### 기술 스택
- **Framework**: Flask 3.0
- **Language**: Python 3.11+
- **ORM**: SQLAlchemy 2.0
- **Migration**: Alembic
- **Validation**: Marshmallow
- **Authentication**: Flask-JWT-Extended
- **CORS**: Flask-CORS
- **Server**: Gunicorn + Nginx

#### 디렉토리 구조
```
backend/
├── app/
│   ├── __init__.py
│   ├── routes/
│   │   ├── auth.py
│   │   ├── products.py
│   │   ├── orders.py
│   │   └── ...
│   ├── services/
│   │   ├── auth_service.py
│   │   ├── product_service.py
│   │   └── ...
│   ├── models/
│   │   ├── user.py
│   │   ├── product.py
│   │   └── ...
│   ├── schemas/
│   │   ├── user_schema.py
│   │   └── ...
│   ├── utils/
│   └── config.py
├── migrations/
├── tests/
├── requirements.txt
└── wsgi.py
```

---

### 2.3 데이터 계층

```
┌─────────────────────────────────────────┐
│         PostgreSQL (Primary)            │
│  - All tables                           │
│  - Write operations                     │
└──────────────┬──────────────────────────┘
               │ Replication
               ▼
┌─────────────────────────────────────────┐
│      PostgreSQL (Read Replica)          │
│  - Read-only                            │
│  - SELECT queries (optional)            │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│            Redis Cache                  │
│  - Session storage                      │
│  - API response cache                   │
│  - Rate limiting                        │
└─────────────────────────────────────────┘
```

---

## 3. 인프라 아키텍처

### 3.1 AWS 구성도

```
┌────────────────────────────────────────────────────────────┐
│                        AWS Cloud                           │
│  ┌──────────────────────────────────────────────────────┐ │
│  │                   VPC (10.0.0.0/16)                  │ │
│  │                                                      │ │
│  │  ┌────────────────────┐  ┌────────────────────┐    │ │
│  │  │  Public Subnet     │  │  Private Subnet    │    │ │
│  │  │  (10.0.1.0/24)     │  │  (10.0.2.0/24)     │    │ │
│  │  │                    │  │                    │    │ │
│  │  │  ┌──────────────┐  │  │  ┌──────────────┐ │    │ │
│  │  │  │   ALB        │  │  │  │   EC2        │ │    │ │
│  │  │  │   (Flask)    │──┼──┼─►│   Backend    │ │    │ │
│  │  │  └──────────────┘  │  │  └──────┬───────┘ │    │ │
│  │  │                    │  │         │         │    │ │
│  │  └────────────────────┘  │         │         │    │ │
│  │                          │         ▼         │    │ │
│  │                          │  ┌──────────────┐ │    │ │
│  │                          │  │  RDS         │ │    │ │
│  │                          │  │  PostgreSQL  │ │    │ │
│  │                          │  └──────────────┘ │    │ │
│  │                          │                    │    │ │
│  │                          │  ┌──────────────┐ │    │ │
│  │                          │  │  ElastiCache │ │    │ │
│  │                          │  │  Redis       │ │    │ │
│  │                          │  └──────────────┘ │    │ │
│  │                          └────────────────────┘    │ │
│  └──────────────────────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────┐         ┌──────────────┐               │
│  │   S3         │         │  CloudFront  │               │
│  │   (Static)   │◄────────│   (CDN)      │               │
│  └──────────────┘         └──────────────┘               │
└────────────────────────────────────────────────────────────┘
```

### 3.2 인프라 구성 요소

#### 3.2.1 컴퓨팅

| 서비스 | 타입 | 사양 | 용도 |
|--------|------|------|------|
| EC2 | t3.medium | 2 vCPU, 4GB RAM | Backend API 서버 |
| ALB | Application Load Balancer | - | 로드 밸런싱 |

**Auto Scaling 정책**:
- Min: 1 instance
- Max: 3 instances
- Target CPU: 70%

#### 3.2.2 데이터베이스

| 서비스 | 타입 | 사양 | 용도 |
|--------|------|------|------|
| RDS PostgreSQL | db.t3.medium | 2 vCPU, 4GB RAM | Primary DB |
| RDS Read Replica | db.t3.medium | 2 vCPU, 4GB RAM | Read-only (Optional) |
| ElastiCache Redis | cache.t3.micro | 2 vCPU, 0.5GB RAM | Cache & Session |

**백업 정책**:
- 자동 백업: 매일 03:00 (KST)
- 보관 기간: 7일
- Multi-AZ: Enabled (Production)

#### 3.2.3 스토리지

| 서비스 | 용도 | 정책 |
|--------|------|------|
| S3 (Frontend) | React/Next.js 정적 파일 | Public Read |
| S3 (Images) | 상품/리뷰 이미지 | CloudFront CDN |
| S3 (Backup) | DB 백업 | Private, Lifecycle: 30일 |

#### 3.2.4 네트워크

```
Internet Gateway
       │
       ▼
┌─────────────┐
│     ALB     │ (Public)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   EC2       │ (Private)
│   Backend   │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│     RDS     │ (Private)
└─────────────┘
```

**Security Groups**:
```
ALB Security Group:
  Inbound: 443 (HTTPS) from 0.0.0.0/0

EC2 Security Group:
  Inbound: 8000 (Flask) from ALB SG

RDS Security Group:
  Inbound: 5432 (PostgreSQL) from EC2 SG

Redis Security Group:
  Inbound: 6379 from EC2 SG
```

---

## 4. 배포 아키텍처

### 4.1 CI/CD 파이프라인

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   GitHub    │─────►│  GitHub     │─────►│    AWS      │
│   (Code)    │ Push │  Actions    │ SSH  │   EC2       │
└─────────────┘      └──────┬──────┘      └─────────────┘
                            │
                            ▼
                     ┌─────────────┐
                     │   Tests     │
                     │   - Pytest  │
                     │   - ESLint  │
                     └─────────────┘
```

#### 4.1.1 Frontend 배포

```yaml
# .github/workflows/deploy-frontend.yml
name: Deploy Frontend

on:
  push:
    branches: [main]
    paths:
      - 'frontend/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: |
          cd frontend
          npm ci
      
      - name: Build
        run: |
          cd frontend
          npm run build
        env:
          NEXT_PUBLIC_API_URL: ${{ secrets.API_URL }}
      
      - name: Deploy to S3
        run: |
          aws s3 sync frontend/out s3://spicyjump-frontend --delete
        env:
          AWS_ACCESS_KEY_ID: ${{ secrets.AWS_ACCESS_KEY_ID }}
          AWS_SECRET_ACCESS_KEY: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
      
      - name: Invalidate CloudFront
        run: |
          aws cloudfront create-invalidation --distribution-id ${{ secrets.CLOUDFRONT_ID }} --paths "/*"
```

#### 4.1.2 Backend 배포

```yaml
# .github/workflows/deploy-backend.yml
name: Deploy Backend

on:
  push:
    branches: [main]
    paths:
      - 'backend/**'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run tests
        run: |
          cd backend
          pip install -r requirements.txt
          pytest
      
      - name: Deploy to EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ubuntu
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd /var/www/spicyjump
            git pull origin main
            source venv/bin/activate
            pip install -r requirements.txt
            alembic upgrade head
            sudo systemctl restart gunicorn
```

---

## 5. 보안 아키텍처

### 5.1 인증 흐름

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant DB as Database
    
    U->>F: 로그인 시도
    F->>B: POST /auth/login
    B->>DB: 사용자 조회
    DB-->>B: 사용자 정보
    B->>B: 비밀번호 검증 (bcrypt)
    B->>B: JWT 토큰 생성
    B-->>F: {access_token, refresh_token}
    F->>F: localStorage 저장
    
    Note over F,B: 이후 모든 요청
    
    F->>B: API 요청 (Bearer token)
    B->>B: JWT 검증
    B-->>F: 응답
```

### 5.2 보안 계층

```
┌─────────────────────────────────────────┐
│  Layer 1: Network Security              │
│  - CloudFlare WAF                       │
│  - DDoS Protection                      │
└──────────────┬──────────────────────────┘
               ▼
┌─────────────────────────────────────────┐
│  Layer 2: Transport Security            │
│  - HTTPS/TLS 1.3                        │
│  - Certificate: Let's Encrypt           │
└──────────────┬──────────────────────────┘
               ▼
┌─────────────────────────────────────────┐
│  Layer 3: Application Security          │
│  - JWT Authentication                   │
│  - CSRF Protection                      │
│  - Input Validation                     │
└──────────────┬──────────────────────────┘
               ▼
┌─────────────────────────────────────────┐
│  Layer 4: Data Security                 │
│  - Password Hashing (bcrypt)            │
│  - Database Encryption at rest          │
│  - Sensitive data masking               │
└─────────────────────────────────────────┘
```

### 5.3 보안 설정

#### Backend 보안 설정
```python
# config.py
class Config:
    # JWT
    JWT_SECRET_KEY = os.environ.get('JWT_SECRET_KEY')
    JWT_ACCESS_TOKEN_EXPIRES = timedelta(hours=1)
    JWT_REFRESH_TOKEN_EXPIRES = timedelta(days=30)
    
    # CORS
    CORS_ORIGINS = ['https://spicyjump.com', 'https://www.spicyjump.com']
    
    # Security Headers
    SECURITY_HEADERS = {
        'Strict-Transport-Security': 'max-age=31536000; includeSubDomains',
        'X-Content-Type-Options': 'nosniff',
        'X-Frame-Options': 'DENY',
        'X-XSS-Protection': '1; mode=block',
        'Content-Security-Policy': "default-src 'self'"
    }
    
    # Rate Limiting
    RATELIMIT_STORAGE_URL = 'redis://localhost:6379'
```

---

## 6. 모니터링 아키텍처

### 6.1 모니터링 스택

```
┌─────────────────────────────────────────┐
│         Application Logs                │
│  - Backend: Python logging              │
│  - Frontend: console.error              │
└──────────────┬──────────────────────────┘
               ▼
┌─────────────────────────────────────────┐
│         CloudWatch Logs                 │
│  - Log aggregation                      │
│  - Log retention: 30 days               │
└──────────────┬──────────────────────────┘
               ▼
┌─────────────────────────────────────────┐
│         CloudWatch Metrics              │
│  - CPU, Memory, Disk                    │
│  - Custom metrics                       │
└──────────────┬──────────────────────────┘
               ▼
┌─────────────────────────────────────────┐
│         CloudWatch Alarms               │
│  - Email notifications                  │
│  - SMS alerts (critical)                │
└─────────────────────────────────────────┘
```

### 6.2 주요 모니터링 지표

#### 인프라 지표
| 지표 | 임계값 | 알림 |
|------|--------|------|
| CPU 사용률 | > 80% | Warning |
| 메모리 사용률 | > 85% | Warning |
| 디스크 사용률 | > 90% | Critical |
| Network In/Out | > 1GB/s | Info |

#### 애플리케이션 지표
| 지표 | 임계값 | 알림 |
|------|--------|------|
| API 응답 시간 | > 1s | Warning |
| Error Rate | > 5% | Critical |
| 동시 접속자 | > 1000 | Info |
| DB Connection Pool | > 80% | Warning |

#### 비즈니스 지표
- 회원가입 수 (일별)
- 주문 수 (시간별)
- 결제 성공률
- 상품 조회수

---

## 7. 성능 최적화

### 7.1 캐싱 전략

```python
# Redis 캐싱
from flask_caching import Cache

cache = Cache(config={
    'CACHE_TYPE': 'redis',
    'CACHE_REDIS_URL': 'redis://localhost:6379/0',
    'CACHE_DEFAULT_TIMEOUT': 300
})

# 상품 목록 캐싱 (5분)
@app.route('/products')
@cache.cached(timeout=300, query_string=True)
def get_products():
    products = db.session.query(Product).all()
    return jsonify(products)

# 상품 상세 캐싱 (10분)
@app.route('/products/<int:product_id>')
@cache.cached(timeout=600)
def get_product(product_id):
    product = db.session.query(Product).get(product_id)
    return jsonify(product)
```

### 7.2 데이터베이스 최적화

```sql
-- 인덱스 최적화
CREATE INDEX CONCURRENTLY idx_products_category_status 
    ON products(category_id, status) 
    WHERE status = 'published';

-- 쿼리 최적화 (N+1 문제 해결)
-- Bad
products = db.session.query(Product).all()
for product in products:
    print(product.seller.name)  # N+1 queries

-- Good (Eager Loading)
products = db.session.query(Product)\
    .options(joinedload(Product.seller))\
    .all()
```

### 7.3 Frontend 최적화

```javascript
// Code Splitting
const ProductDetail = lazy(() => import('./pages/ProductDetail'));

// Image Optimization
<Image
  src="/products/kimchi.jpg"
  width={600}
  height={400}
  loading="lazy"
  placeholder="blur"
/>

// API Response Caching
const { data } = useQuery(['products'], fetchProducts, {
  staleTime: 5 * 60 * 1000, // 5분
  cacheTime: 10 * 60 * 1000 // 10분
});
```

---

## 8. 백업 및 복구

### 8.1 백업 전략

#### RDS 자동 백업
```
- 백업 주기: 매일 03:00 (KST)
- 보관 기간: 7일
- 백업 타입: Automated Snapshot
- Multi-AZ: Enabled (Production)
```

#### 수동 백업
```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"

# Database backup
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME -F c -f $BACKUP_DIR/db_backup_$DATE.dump

# Upload to S3
aws s3 cp $BACKUP_DIR/db_backup_$DATE.dump s3://spicyjump-backups/

# Delete old backups (> 30 days)
find $BACKUP_DIR -name "db_backup_*.dump" -mtime +30 -delete
```

### 8.2 복구 절차

#### 데이터베이스 복구
```bash
# 1. RDS 스냅샷에서 복구
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier spicyjump-restored \
    --db-snapshot-identifier rds:spicyjump-2025-11-19-03-00

# 2. 수동 백업에서 복구
pg_restore -h $DB_HOST -U $DB_USER -d $DB_NAME -c db_backup_20251119.dump
```

---

## 9. 비용 추정 (월간)

### 9.1 AWS 비용

| 서비스 | 사양 | 시간 | 월 비용 (USD) |
|--------|------|------|--------------|
| EC2 (t3.medium) | 2대 | 730h | $60 |
| RDS (db.t3.medium) | 1대 | 730h | $70 |
| ElastiCache (cache.t3.micro) | 1대 | 730h | $15 |
| ALB | 1대 | - | $20 |
| S3 (100GB) | - | - | $3 |
| CloudFront (500GB) | - | - | $40 |
| Data Transfer | 500GB | - | $45 |
| **Total** | | | **$253** |

### 9.2 비용 최적화 방안

1. **Reserved Instances**: 1년 예약 시 40% 할인
2. **S3 Lifecycle**: 30일 후 Glacier로 이동
3. **CloudFront**: 캐시 Hit Rate 향상
4. **Auto Scaling**: 사용량에 따라 자동 조절

---

## 부록

### A. 환경 변수 관리

```bash
# .env.production
# Database
DATABASE_URL=postgresql://user:pass@rds-host:5432/spicyjump

# Redis
REDIS_URL=redis://elasticache-host:6379/0

# JWT
JWT_SECRET_KEY=your-secret-key-here

# AWS
AWS_ACCESS_KEY_ID=AKIAIOSFODNN7EXAMPLE
AWS_SECRET_ACCESS_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
AWS_S3_BUCKET=spicyjump-products
AWS_REGION=us-west-2

# Stripe
STRIPE_SECRET_KEY=sk_live_...
STRIPE_WEBHOOK_SECRET=whsec_...

# Email
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=noreply@spicyjump.com
SMTP_PASSWORD=app-password
```

---

**문서 관리**
- 작성자: [담당자명]
- 최종 업데이트: 2025-11-19
- 관련 문서: 인프라 구성도, 배포 가이드


