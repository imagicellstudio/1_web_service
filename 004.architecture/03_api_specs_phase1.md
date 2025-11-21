# API 명세서 (Phase 1 MVP)

## 문서 정보
- 작성일: 2025-11-19
- 버전: 1.0 (Phase 1 MVP)
- Base URL: `https://api.spicyjump.com/v1`
- 프로토콜: HTTPS
- 인증: JWT Bearer Token

---

## 1. API 개요

### 1.1 엔드포인트 구조

```
/v1/auth          # 인증
/v1/products      # 상품
/v1/cart          # 장바구니
/v1/orders        # 주문
/v1/payments      # 결제
/v1/reviews       # 리뷰
/v1/users         # 사용자
/v1/categories    # 카테고리
```

### 1.2 공통 사항

#### Base URL
```
Production: https://api.spicyjump.com/v1
Staging:    https://api-staging.spicyjump.com/v1
Development: http://localhost:8000/v1
```

#### Request Headers
```http
Content-Type: application/json
Accept: application/json
Accept-Language: ko,en (optional)
Authorization: Bearer {access_token} (인증 필요 시)
```

#### Response Format
```json
{
  "success": true|false,
  "data": {},
  "message": "Success message",
  "errors": []
}
```

#### HTTP Status Codes
- `200 OK`: 성공
- `201 Created`: 생성 성공
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `422 Unprocessable Entity`: 유효성 검증 실패
- `500 Internal Server Error`: 서버 오류

---

## 2. 인증 API

### 2.1 회원가입

#### Endpoint
```http
POST /v1/auth/register
```

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "Hong Gildong",
  "phone": "010-1234-5678",
  "role": "buyer",
  "language": "ko"
}
```

#### Request Fields
| 필드 | 타입 | 필수 | 설명 | 제약 |
|------|------|------|------|------|
| email | string | Y | 이메일 | 유효한 이메일 형식 |
| password | string | Y | 비밀번호 | 8자 이상, 영문+숫자+특수문자 |
| name | string | Y | 이름 | 2-200자 |
| phone | string | N | 전화번호 | |
| role | string | N | 역할 | buyer(기본), seller |
| language | string | N | 언어 | ko(기본), en |

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "Hong Gildong",
      "role": "buyer",
      "language": "ko",
      "created_at": "2025-11-19T10:00:00Z"
    },
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "Bearer",
    "expires_in": 3600
  },
  "message": "Registration successful"
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    {
      "field": "email",
      "message": "Email already exists"
    },
    {
      "field": "password",
      "message": "Password must be at least 8 characters"
    }
  ]
}
```

---

### 2.2 로그인

#### Endpoint
```http
POST /v1/auth/login
```

#### Request Body
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "Hong Gildong",
      "role": "buyer"
    },
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "refresh_token": "eyJhbGciOiJIUzI1NiIs...",
    "token_type": "Bearer",
    "expires_in": 3600
  }
}
```

#### Error Response (401 Unauthorized)
```json
{
  "success": false,
  "message": "Invalid credentials"
}
```

---

### 2.3 로그아웃

#### Endpoint
```http
POST /v1/auth/logout
```

#### Request Headers
```http
Authorization: Bearer {access_token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

### 2.4 토큰 갱신

#### Endpoint
```http
POST /v1/auth/refresh
```

#### Request Body
```json
{
  "refresh_token": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIs...",
    "expires_in": 3600
  }
}
```

---

## 3. 상품 API

### 3.1 상품 목록 조회

#### Endpoint
```http
GET /v1/products
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|---------|------|------|------|--------|
| page | integer | N | 페이지 번호 | 1 |
| limit | integer | N | 페이지 크기 | 20 |
| category_id | integer | N | 카테고리 ID | - |
| search | string | N | 검색어 | - |
| sort | string | N | 정렬 기준 | created_at |
| order | string | N | 정렬 순서 | desc |
| min_price | float | N | 최소 가격 | - |
| max_price | float | N | 최대 가격 | - |

#### Request Example
```http
GET /v1/products?page=1&limit=20&category_id=5&sort=price&order=asc
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "배추김치 1kg",
        "name_en": "Cabbage Kimchi 1kg",
        "description": "정성껏 담근 배추김치",
        "price": 15.00,
        "currency": "USD",
        "stock_quantity": 100,
        "images": [
          "https://s3.../image1.jpg",
          "https://s3.../image2.jpg"
        ],
        "rating_average": 4.5,
        "review_count": 10,
        "seller": {
          "id": 2,
          "name": "John Kim"
        },
        "category": {
          "id": 5,
          "name": "배추김치",
          "name_en": "Cabbage Kimchi"
        },
        "created_at": "2025-11-19T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 100,
      "total_pages": 5
    }
  }
}
```

---

### 3.2 상품 상세 조회

#### Endpoint
```http
GET /v1/products/{product_id}
```

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| product_id | integer | 상품 ID |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg",
    "name_en": "Cabbage Kimchi 1kg",
    "description": "정성껏 담근 배추김치입니다...",
    "description_en": "Traditionally fermented...",
    "price": 15.00,
    "currency": "USD",
    "stock_quantity": 100,
    "images": [
      "https://s3.../image1.jpg",
      "https://s3.../image2.jpg"
    ],
    "status": "published",
    "view_count": 150,
    "rating_average": 4.5,
    "review_count": 10,
    "seller": {
      "id": 2,
      "name": "John Kim",
      "email": "seller@example.com"
    },
    "category": {
      "id": 5,
      "name": "배추김치",
      "name_en": "Cabbage Kimchi"
    },
    "created_at": "2025-11-19T10:00:00Z",
    "updated_at": "2025-11-19T10:00:00Z"
  }
}
```

#### Error Response (404 Not Found)
```json
{
  "success": false,
  "message": "Product not found"
}
```

---

### 3.3 상품 등록 (판매자)

#### Endpoint
```http
POST /v1/products
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: seller
```

#### Request Body
```json
{
  "category_id": 5,
  "name": "배추김치 1kg",
  "name_en": "Cabbage Kimchi 1kg",
  "description": "정성껏 담근 배추김치",
  "description_en": "Traditionally fermented",
  "price": 15.00,
  "currency": "USD",
  "stock_quantity": 100,
  "images": [
    "https://s3.../image1.jpg",
    "https://s3.../image2.jpg"
  ],
  "status": "published"
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg",
    "price": 15.00,
    "status": "published",
    "created_at": "2025-11-19T10:00:00Z"
  },
  "message": "Product created successfully"
}
```

---

### 3.4 상품 수정 (판매자)

#### Endpoint
```http
PUT /v1/products/{product_id}
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: seller (본인 상품만)
```

#### Request Body
```json
{
  "price": 14.00,
  "stock_quantity": 150
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "price": 14.00,
    "stock_quantity": 150,
    "updated_at": "2025-11-19T11:00:00Z"
  },
  "message": "Product updated successfully"
}
```

---

### 3.5 상품 삭제 (판매자)

#### Endpoint
```http
DELETE /v1/products/{product_id}
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: seller (본인 상품만) 또는 admin
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Product deleted successfully"
}
```

---

## 4. 장바구니 API

### 4.1 장바구니 조회

#### Endpoint
```http
GET /v1/cart
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "items": [
      {
        "id": 1,
        "product": {
          "id": 1,
          "name": "배추김치 1kg",
          "price": 15.00,
          "currency": "USD",
          "image": "https://s3.../image1.jpg",
          "stock_quantity": 100
        },
        "quantity": 2,
        "subtotal": 30.00,
        "added_at": "2025-11-19T10:00:00Z"
      }
    ],
    "total": 30.00,
    "currency": "USD",
    "item_count": 1
  }
}
```

---

### 4.2 장바구니 추가

#### Endpoint
```http
POST /v1/cart/items
```

#### Request Body
```json
{
  "product_id": 1,
  "quantity": 2
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "product_id": 1,
    "quantity": 2,
    "added_at": "2025-11-19T10:00:00Z"
  },
  "message": "Added to cart"
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "Insufficient stock"
}
```

---

### 4.3 장바구니 수량 변경

#### Endpoint
```http
PUT /v1/cart/items/{item_id}
```

#### Request Body
```json
{
  "quantity": 3
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "quantity": 3,
    "subtotal": 45.00
  }
}
```

---

### 4.4 장바구니 항목 삭제

#### Endpoint
```http
DELETE /v1/cart/items/{item_id}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Item removed from cart"
}
```

---

## 5. 주문 API

### 5.1 주문 생성

#### Endpoint
```http
POST /v1/orders
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Request Body
```json
{
  "items": [
    {
      "product_id": 1,
      "quantity": 2
    }
  ],
  "shipping_address": {
    "name": "Hong Gildong",
    "phone": "010-1234-5678",
    "address": "123 Main St, Seoul",
    "city": "Seoul",
    "zipcode": "06000",
    "country": "KR"
  },
  "buyer_note": "Please deliver after 6 PM"
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "order_number": "ORD-20251119-0001",
    "buyer_id": 3,
    "seller_id": 2,
    "subtotal": 30.00,
    "shipping_fee": 3.00,
    "total": 33.00,
    "currency": "USD",
    "status": "pending",
    "items": [
      {
        "product_id": 1,
        "product_name": "배추김치 1kg",
        "quantity": 2,
        "unit_price": 15.00,
        "subtotal": 30.00
      }
    ],
    "shipping_address": {
      "name": "Hong Gildong",
      "phone": "010-1234-5678",
      "address": "123 Main St, Seoul",
      "zipcode": "06000"
    },
    "created_at": "2025-11-19T10:00:00Z"
  },
  "message": "Order created successfully"
}
```

---

### 5.2 주문 목록 조회

#### Endpoint
```http
GET /v1/orders
```

#### Query Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| status | string | 주문 상태 필터 |
| page | integer | 페이지 번호 |
| limit | integer | 페이지 크기 |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "orders": [
      {
        "id": 1,
        "order_number": "ORD-20251119-0001",
        "total": 33.00,
        "currency": "USD",
        "status": "delivered",
        "items_count": 1,
        "created_at": "2025-11-19T10:00:00Z",
        "delivered_at": "2025-11-20T15:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 50
    }
  }
}
```

---

### 5.3 주문 상세 조회

#### Endpoint
```http
GET /v1/orders/{order_id}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "order_number": "ORD-20251119-0001",
    "buyer": {
      "id": 3,
      "name": "Hong Gildong",
      "email": "buyer@example.com"
    },
    "seller": {
      "id": 2,
      "name": "John Kim"
    },
    "items": [
      {
        "id": 1,
        "product_id": 1,
        "product_name": "배추김치 1kg",
        "quantity": 2,
        "unit_price": 15.00,
        "subtotal": 30.00
      }
    ],
    "subtotal": 30.00,
    "shipping_fee": 3.00,
    "total": 33.00,
    "currency": "USD",
    "status": "delivered",
    "shipping_address": {
      "name": "Hong Gildong",
      "phone": "010-1234-5678",
      "address": "123 Main St, Seoul",
      "zipcode": "06000"
    },
    "payment": {
      "id": 1,
      "amount": 33.00,
      "status": "completed",
      "payment_method": "stripe",
      "paid_at": "2025-11-19T10:05:00Z"
    },
    "created_at": "2025-11-19T10:00:00Z",
    "confirmed_at": "2025-11-19T10:10:00Z",
    "shipped_at": "2025-11-19T14:00:00Z",
    "delivered_at": "2025-11-20T15:00:00Z"
  }
}
```

---

## 6. 결제 API

### 6.1 결제 시작

#### Endpoint
```http
POST /v1/payments
```

#### Request Body
```json
{
  "order_id": 1,
  "payment_method": "stripe",
  "return_url": "https://spicyjump.com/payment/success",
  "cancel_url": "https://spicyjump.com/payment/cancel"
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "payment_id": 1,
    "order_id": 1,
    "amount": 33.00,
    "currency": "USD",
    "status": "pending",
    "checkout_url": "https://checkout.stripe.com/...",
    "created_at": "2025-11-19T10:00:00Z"
  }
}
```

---

### 6.2 결제 콜백 (Webhook)

#### Endpoint
```http
POST /v1/payments/callback
```

#### Request Body (Stripe 예시)
```json
{
  "type": "payment_intent.succeeded",
  "data": {
    "object": {
      "id": "pi_3ABC123",
      "amount": 3300,
      "currency": "usd",
      "status": "succeeded"
    }
  }
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "Payment processed"
}
```

---

## 7. 리뷰 API

### 7.1 리뷰 목록 조회

#### Endpoint
```http
GET /v1/products/{product_id}/reviews
```

#### Query Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| page | integer | 페이지 번호 |
| limit | integer | 페이지 크기 |
| rating | integer | 평점 필터 (1-5) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "reviews": [
      {
        "id": 1,
        "user": {
          "id": 3,
          "name": "Hong Gildong"
        },
        "rating": 5,
        "title": "Great kimchi!",
        "content": "Very authentic taste...",
        "images": [
          "https://s3.../review1.jpg"
        ],
        "is_verified_purchase": true,
        "created_at": "2025-11-20T10:00:00Z"
      }
    ],
    "pagination": {
      "page": 1,
      "limit": 20,
      "total": 50
    },
    "summary": {
      "average_rating": 4.5,
      "total_reviews": 50,
      "rating_distribution": {
        "5": 30,
        "4": 15,
        "3": 3,
        "2": 1,
        "1": 1
      }
    }
  }
}
```

---

### 7.2 리뷰 작성

#### Endpoint
```http
POST /v1/reviews
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Request Body
```json
{
  "product_id": 1,
  "rating": 5,
  "title": "Great kimchi!",
  "content": "Very authentic taste. Highly recommend!",
  "images": [
    "https://s3.../review1.jpg"
  ]
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "product_id": 1,
    "rating": 5,
    "title": "Great kimchi!",
    "content": "Very authentic taste...",
    "created_at": "2025-11-20T10:00:00Z"
  },
  "message": "Review posted successfully"
}
```

#### Error Response (400 Bad Request)
```json
{
  "success": false,
  "message": "You have already reviewed this product"
}
```

---

## 8. 카테고리 API

### 8.1 카테고리 목록

#### Endpoint
```http
GET /v1/categories
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "categories": [
      {
        "id": 1,
        "name": "김치/반찬",
        "name_en": "Kimchi & Side Dishes",
        "children": [
          {
            "id": 5,
            "name": "배추김치",
            "name_en": "Cabbage Kimchi"
          },
          {
            "id": 6,
            "name": "깍두기",
            "name_en": "Radish Kimchi"
          }
        ]
      }
    ]
  }
}
```

---

## 9. 에러 코드

### 9.1 표준 에러 코드

| 코드 | 메시지 | 설명 |
|------|--------|------|
| E1001 | Invalid credentials | 잘못된 로그인 정보 |
| E1002 | Email already exists | 이메일 중복 |
| E1003 | Invalid token | 유효하지 않은 토큰 |
| E1004 | Token expired | 토큰 만료 |
| E2001 | Product not found | 상품 없음 |
| E2002 | Insufficient stock | 재고 부족 |
| E3001 | Order not found | 주문 없음 |
| E3002 | Cannot cancel order | 주문 취소 불가 |
| E4001 | Payment failed | 결제 실패 |
| E5001 | Duplicate review | 중복 리뷰 |

### 9.2 에러 응답 예시
```json
{
  "success": false,
  "error": {
    "code": "E2002",
    "message": "Insufficient stock",
    "details": {
      "product_id": 1,
      "requested": 10,
      "available": 5
    }
  }
}
```

---

## 10. Rate Limiting

### 10.1 제한 정책
- 인증 없음: 100 requests / 15분
- 인증 사용자: 1000 requests / 15분
- 판매자: 2000 requests / 15분

### 10.2 응답 헤더
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1637000000
```

### 10.3 초과 시 응답 (429 Too Many Requests)
```json
{
  "success": false,
  "message": "Rate limit exceeded",
  "retry_after": 900
}
```

---

## 11. Pagination

### 11.1 요청 파라미터
```
page: 페이지 번호 (1부터 시작)
limit: 페이지 크기 (기본 20, 최대 100)
```

### 11.2 응답 형식
```json
{
  "data": [...],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "total_pages": 5
  }
}
```

---

## 부록

### A. Postman Collection
```
[설계 완료 후 Postman Collection 링크 제공]
```

### B. API 테스트 샘플
```bash
# 로그인
curl -X POST https://api.spicyjump.com/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'

# 상품 목록 조회
curl -X GET https://api.spicyjump.com/v1/products?page=1&limit=20

# 상품 등록 (인증 필요)
curl -X POST https://api.spicyjump.com/v1/products \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"김치","price":15.00}'
```

---

**문서 관리**
- 작성자: [담당자명]
- 최종 업데이트: 2025-11-19
- API 버전: v1
- 다음 리뷰: Phase 1 구현 완료 시


