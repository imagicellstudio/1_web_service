# 인터페이스 설계서 (Phase 1 MVP)

## 문서 정보
- 작성일: 2025-11-19
- 버전: 1.0 (Phase 1 MVP)
- 목적: 시스템 간 통신 규격 정의

---

## 1. 인터페이스 개요

### 1.1 인터페이스 구성도

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   Frontend  │◄───────►│   Backend   │◄───────►│  Database   │
│  (React)    │  REST   │   (Flask)   │   SQL   │(PostgreSQL) │
└─────────────┘         └──────┬──────┘         └─────────────┘
                               │
                    ┌──────────┼──────────┐
                    │          │          │
                    ▼          ▼          ▼
              ┌─────────┐ ┌───────┐ ┌─────────┐
              │ Stripe  │ │  S3   │ │  Email  │
              │   PG    │ │ CDN   │ │ Service │
              └─────────┘ └───────┘ └─────────┘
```

### 1.2 인터페이스 목록

| ID | 인터페이스명 | 송신 | 수신 | 프로토콜 | 비고 |
|---|------------|------|------|---------|------|
| IF-01 | Frontend-Backend API | Frontend | Backend | HTTPS/REST | 주 통신 |
| IF-02 | Backend-Database | Backend | PostgreSQL | TCP/SQL | 데이터 조회/저장 |
| IF-03 | Backend-Stripe | Backend | Stripe | HTTPS/REST | 결제 처리 |
| IF-04 | Backend-S3 | Backend | AWS S3 | HTTPS | 이미지 저장 |
| IF-05 | Backend-Email | Backend | Email Service | SMTP | 이메일 발송 |

---

## 2. Frontend ↔ Backend 인터페이스

### 2.1 기본 규격

#### 프로토콜
- **Protocol**: HTTPS
- **Method**: GET, POST, PUT, DELETE
- **Format**: JSON
- **Encoding**: UTF-8

#### 공통 Request Header
```http
Content-Type: application/json; charset=UTF-8
Accept: application/json
Accept-Language: ko,en;q=0.9
Authorization: Bearer {access_token}
X-Client-Version: 1.0.0
X-Device-Type: web|mobile|tablet
```

#### 공통 Response Header
```http
Content-Type: application/json; charset=UTF-8
X-Request-ID: 550e8400-e29b-41d4-a716-446655440000
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-Response-Time: 245ms
```

### 2.2 인증 흐름

```mermaid
sequenceDiagram
    participant F as Frontend
    participant B as Backend
    participant DB as Database
    
    F->>B: POST /auth/login {email, password}
    B->>DB: SELECT user WHERE email=?
    DB-->>B: user data
    B->>B: Verify password
    B->>B: Generate JWT token
    B-->>F: {access_token, refresh_token}
    
    Note over F: Store tokens in localStorage
    
    F->>B: GET /products (with Bearer token)
    B->>B: Verify token
    B->>DB: SELECT products
    DB-->>B: products data
    B-->>F: {products}
```

### 2.3 데이터 형식 표준

#### 날짜/시간
```json
{
  "created_at": "2025-11-19T10:00:00Z",
  "updated_at": "2025-11-19T10:00:00+09:00"
}
```
- **형식**: ISO 8601
- **Timezone**: UTC (Z) 또는 Offset (+09:00)

#### 금액
```json
{
  "price": 15.00,
  "currency": "USD"
}
```
- **형식**: Decimal (소수점 2자리)
- **통화**: ISO 4217 코드 (USD, KRW, EUR 등)

#### 이미지 URL
```json
{
  "images": [
    "https://cdn.spicyjump.com/products/123/image1.jpg",
    "https://cdn.spicyjump.com/products/123/image2.jpg"
  ]
}
```
- **형식**: HTTPS URL 배열
- **CDN**: CloudFront 또는 S3

---

## 3. Backend ↔ Database 인터페이스

### 3.1 연결 규격

#### Connection String
```python
DATABASE_URL = "postgresql://user:password@localhost:5432/spicyjump_mvp"

# SQLAlchemy 설정
SQLALCHEMY_DATABASE_URI = DATABASE_URL
SQLALCHEMY_POOL_SIZE = 10
SQLALCHEMY_MAX_OVERFLOW = 20
SQLALCHEMY_POOL_TIMEOUT = 30
SQLALCHEMY_POOL_RECYCLE = 3600
```

#### Connection Pool
- **Pool Size**: 10개 (기본)
- **Max Overflow**: 20개 (최대)
- **Timeout**: 30초
- **Recycle**: 3600초 (1시간)

### 3.2 쿼리 패턴

#### SELECT (조회)
```python
# 단건 조회
user = db.session.query(User).filter(User.id == user_id).first()

# 목록 조회 (페이징)
products = db.session.query(Product)\
    .filter(Product.status == 'published')\
    .offset((page - 1) * limit)\
    .limit(limit)\
    .all()
```

#### INSERT (생성)
```python
new_product = Product(
    seller_id=seller_id,
    name=name,
    price=price
)
db.session.add(new_product)
db.session.commit()
```

#### UPDATE (수정)
```python
product = db.session.query(Product).filter(Product.id == product_id).first()
product.price = new_price
product.updated_at = datetime.utcnow()
db.session.commit()
```

#### DELETE (삭제)
```python
db.session.query(Product).filter(Product.id == product_id).delete()
db.session.commit()
```

### 3.3 트랜잭션 관리

```python
# 트랜잭션 예시 (주문 생성)
try:
    # 1. 주문 생성
    order = Order(...)
    db.session.add(order)
    
    # 2. 주문 항목 추가
    for item in items:
        order_item = OrderItem(...)
        db.session.add(order_item)
        
        # 3. 재고 감소
        product = db.session.query(Product).filter(Product.id == item.product_id).first()
        product.stock_quantity -= item.quantity
    
    # 4. 커밋
    db.session.commit()
    
except Exception as e:
    # 롤백
    db.session.rollback()
    raise e
```

---

## 4. Backend ↔ Stripe 인터페이스

### 4.1 결제 흐름

```mermaid
sequenceDiagram
    participant F as Frontend
    participant B as Backend
    participant S as Stripe
    
    F->>B: POST /payments {order_id}
    B->>S: Create Payment Intent
    S-->>B: {client_secret, payment_id}
    B-->>F: {checkout_url}
    
    F->>S: Confirm Payment (redirect)
    S->>B: Webhook: payment_intent.succeeded
    B->>B: Update order status
    B-->>S: 200 OK
```

### 4.2 Stripe API 호출

#### Payment Intent 생성
```python
import stripe

stripe.api_key = "sk_test_..."

# 결제 생성
intent = stripe.PaymentIntent.create(
    amount=3300,  # $33.00 (cents)
    currency="usd",
    metadata={
        "order_id": "1",
        "order_number": "ORD-20251119-0001"
    }
)

# Response
{
    "id": "pi_3ABC123",
    "client_secret": "pi_3ABC123_secret_xyz",
    "amount": 3300,
    "currency": "usd",
    "status": "requires_payment_method"
}
```

#### Webhook 처리
```python
@app.route('/payments/callback', methods=['POST'])
def stripe_webhook():
    payload = request.data
    sig_header = request.headers.get('Stripe-Signature')
    
    try:
        event = stripe.Webhook.construct_event(
            payload, sig_header, webhook_secret
        )
        
        if event['type'] == 'payment_intent.succeeded':
            payment_intent = event['data']['object']
            order_id = payment_intent['metadata']['order_id']
            
            # 주문 상태 업데이트
            update_order_status(order_id, 'paid')
            
        return jsonify({'success': True}), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 400
```

### 4.3 에러 처리

| Stripe Error | HTTP Status | 처리 방법 |
|--------------|-------------|---------|
| card_declined | 402 | 결제 실패 알림 |
| insufficient_funds | 402 | 잔액 부족 알림 |
| expired_card | 402 | 카드 만료 알림 |
| incorrect_cvc | 402 | CVC 오류 알림 |
| rate_limit | 429 | 재시도 (exponential backoff) |

---

## 5. Backend ↔ S3 인터페이스

### 5.1 이미지 업로드 흐름

```mermaid
sequenceDiagram
    participant F as Frontend
    participant B as Backend
    participant S3 as AWS S3
    
    F->>B: POST /upload/presigned-url
    B->>S3: Generate presigned URL
    S3-->>B: {presigned_url, fields}
    B-->>F: {upload_url}
    
    F->>S3: PUT {file} to presigned_url
    S3-->>F: 200 OK
    F->>B: POST /products {image_url}
```

### 5.2 S3 설정

```python
import boto3

s3_client = boto3.client(
    's3',
    aws_access_key_id='AKIAIOSFODNN7EXAMPLE',
    aws_secret_access_key='wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY',
    region_name='us-west-2'
)

BUCKET_NAME = 'spicyjump-products'
CDN_URL = 'https://cdn.spicyjump.com'
```

### 5.3 Presigned URL 생성

```python
def generate_presigned_url(file_name, file_type, expires_in=3600):
    """
    Presigned URL 생성 (직접 업로드용)
    """
    key = f"products/{uuid.uuid4()}/{file_name}"
    
    presigned_url = s3_client.generate_presigned_url(
        'put_object',
        Params={
            'Bucket': BUCKET_NAME,
            'Key': key,
            'ContentType': file_type
        },
        ExpiresIn=expires_in
    )
    
    return {
        'upload_url': presigned_url,
        'file_url': f"{CDN_URL}/{key}"
    }
```

### 5.4 직접 업로드

```python
def upload_file_to_s3(file_path, file_name):
    """
    Backend에서 직접 업로드
    """
    key = f"products/{uuid.uuid4()}/{file_name}"
    
    s3_client.upload_file(
        file_path,
        BUCKET_NAME,
        key,
        ExtraArgs={
            'ContentType': 'image/jpeg',
            'ACL': 'public-read'
        }
    )
    
    return f"{CDN_URL}/{key}"
```

---

## 6. Backend ↔ Email 인터페이스

### 6.1 이메일 발송 흐름

```mermaid
sequenceDiagram
    participant B as Backend
    participant E as Email Service
    participant U as User
    
    B->>E: Send Email (SMTP)
    E->>U: Email delivered
    E-->>B: 250 OK
```

### 6.2 SMTP 설정

```python
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# 설정
SMTP_HOST = 'smtp.gmail.com'
SMTP_PORT = 587
SMTP_USER = 'noreply@spicyjump.com'
SMTP_PASSWORD = 'app_password'
```

### 6.3 이메일 템플릿

#### 회원가입 환영 이메일
```python
def send_welcome_email(user_email, user_name):
    msg = MIMEMultipart('alternative')
    msg['Subject'] = 'Welcome to SpicyJump!'
    msg['From'] = SMTP_USER
    msg['To'] = user_email
    
    html = f"""
    <html>
      <body>
        <h1>Welcome, {user_name}!</h1>
        <p>Thank you for joining SpicyJump.</p>
      </body>
    </html>
    """
    
    msg.attach(MIMEText(html, 'html'))
    
    with smtplib.SMTP(SMTP_HOST, SMTP_PORT) as server:
        server.starttls()
        server.login(SMTP_USER, SMTP_PASSWORD)
        server.send_message(msg)
```

#### 주문 확인 이메일
```python
def send_order_confirmation_email(order):
    subject = f"Order Confirmation - {order.order_number}"
    
    html = f"""
    <html>
      <body>
        <h1>Order Confirmed</h1>
        <p>Order Number: {order.order_number}</p>
        <p>Total: ${order.total}</p>
        <p>Status: {order.status}</p>
      </body>
    </html>
    """
    
    send_email(order.buyer.email, subject, html)
```

---

## 7. 데이터 검증

### 7.1 Backend 검증 규칙

```python
from marshmallow import Schema, fields, validate

class ProductSchema(Schema):
    """상품 데이터 검증"""
    name = fields.Str(required=True, validate=validate.Length(min=1, max=500))
    name_en = fields.Str(validate=validate.Length(max=500))
    price = fields.Decimal(required=True, validate=validate.Range(min=0))
    currency = fields.Str(validate=validate.OneOf(['USD', 'KRW', 'EUR']))
    stock_quantity = fields.Int(validate=validate.Range(min=0))
    category_id = fields.Int()

class UserRegistrationSchema(Schema):
    """회원가입 검증"""
    email = fields.Email(required=True)
    password = fields.Str(
        required=True,
        validate=validate.Regexp(r'^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$')
    )
    name = fields.Str(required=True, validate=validate.Length(min=2, max=200))
    role = fields.Str(validate=validate.OneOf(['buyer', 'seller']))
```

### 7.2 Frontend 검증 규칙

```javascript
// React Hook Form + Yup
import * as yup from 'yup';

const productSchema = yup.object({
  name: yup.string()
    .required('상품명은 필수입니다')
    .max(500, '상품명은 500자 이내여야 합니다'),
  price: yup.number()
    .required('가격은 필수입니다')
    .min(0, '가격은 0 이상이어야 합니다'),
  stock_quantity: yup.number()
    .required('재고는 필수입니다')
    .min(0, '재고는 0 이상이어야 합니다')
});

const loginSchema = yup.object({
  email: yup.string()
    .required('이메일은 필수입니다')
    .email('올바른 이메일 형식이 아닙니다'),
  password: yup.string()
    .required('비밀번호는 필수입니다')
    .min(8, '비밀번호는 8자 이상이어야 합니다')
});
```

---

## 8. 에러 처리

### 8.1 Frontend 에러 처리

```javascript
// API 호출 래퍼
async function apiCall(endpoint, options) {
  try {
    const response = await fetch(endpoint, options);
    const data = await response.json();
    
    if (!response.ok) {
      throw new ApiError(data.message, response.status, data.error);
    }
    
    return data;
    
  } catch (error) {
    if (error instanceof ApiError) {
      // 비즈니스 에러
      if (error.status === 401) {
        // 로그아웃 처리
        logout();
      } else if (error.status === 422) {
        // 검증 에러 표시
        showValidationErrors(error.errors);
      }
    } else {
      // 네트워크 에러
      showError('Network error. Please try again.');
    }
    
    throw error;
  }
}

class ApiError extends Error {
  constructor(message, status, details) {
    super(message);
    this.status = status;
    this.details = details;
  }
}
```

### 8.2 Backend 에러 처리

```python
from flask import jsonify

class APIError(Exception):
    """API 에러 베이스 클래스"""
    def __init__(self, message, status_code=400, payload=None):
        super().__init__()
        self.message = message
        self.status_code = status_code
        self.payload = payload

    def to_dict(self):
        rv = dict(self.payload or ())
        rv['success'] = False
        rv['message'] = self.message
        return rv

# 에러 핸들러
@app.errorhandler(APIError)
def handle_api_error(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response

# 사용 예시
@app.route('/products/<int:product_id>')
def get_product(product_id):
    product = db.session.query(Product).filter(Product.id == product_id).first()
    
    if not product:
        raise APIError('Product not found', status_code=404)
    
    return jsonify({'success': True, 'data': product.to_dict()})
```

---

## 9. 보안

### 9.1 HTTPS 강제
```python
from flask_talisman import Talisman

# HTTPS 강제
Talisman(app, force_https=True)
```

### 9.2 CORS 설정
```python
from flask_cors import CORS

CORS(app, resources={
    r"/v1/*": {
        "origins": ["https://spicyjump.com", "https://www.spicyjump.com"],
        "methods": ["GET", "POST", "PUT", "DELETE"],
        "allow_headers": ["Content-Type", "Authorization"]
    }
})
```

### 9.3 Rate Limiting
```python
from flask_limiter import Limiter

limiter = Limiter(
    app,
    key_func=lambda: request.headers.get('Authorization', 'anonymous'),
    default_limits=["1000 per day", "100 per hour"]
)

@app.route('/auth/login', methods=['POST'])
@limiter.limit("5 per minute")
def login():
    # 로그인 로직
    pass
```

---

## 10. 모니터링

### 10.1 로깅

```python
import logging

# 로깅 설정
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s [%(levelname)s] %(name)s: %(message)s',
    handlers=[
        logging.FileHandler('app.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)

# 사용 예시
@app.route('/products', methods=['POST'])
def create_product():
    logger.info(f"Creating product: {request.json}")
    
    try:
        product = Product(**request.json)
        db.session.add(product)
        db.session.commit()
        
        logger.info(f"Product created: id={product.id}")
        return jsonify({'success': True})
        
    except Exception as e:
        logger.error(f"Error creating product: {str(e)}")
        raise
```

### 10.2 성능 모니터링

```python
import time

@app.before_request
def before_request():
    request.start_time = time.time()

@app.after_request
def after_request(response):
    duration = time.time() - request.start_time
    response.headers['X-Response-Time'] = f"{duration * 1000:.2f}ms"
    
    logger.info(f"{request.method} {request.path} - {response.status_code} - {duration * 1000:.2f}ms")
    
    return response
```

---

## 부록

### A. 인터페이스 명세서 샘플

| 항목 | 내용 |
|-----|------|
| IF ID | IF-01 |
| IF명 | Frontend-Backend 상품 조회 API |
| 송신 시스템 | Frontend (React) |
| 수신 시스템 | Backend (Flask) |
| 프로토콜 | HTTPS/REST |
| Method | GET |
| URL | /v1/products/{product_id} |
| Request | Path: product_id (integer) |
| Response | JSON: {success, data: {product}} |
| 에러 코드 | 404: Product not found |
| 타임아웃 | 30초 |
| 재시도 | 3회 (exponential backoff) |

---

**문서 관리**
- 작성자: [담당자명]
- 최종 업데이트: 2025-11-19
- 관련 문서: API 명세서, 아키텍처 정의서


