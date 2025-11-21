# Java Spring Boot 기반 API 명세서 (Phase 1 MVP)

**문서 버전**: v2.0 (Java 전환)  
**작성일**: 2025-11-20  
**Base URL**: `https://api.xlcfi.com`  
**프로토콜**: HTTPS  
**인증**: JWT Bearer Token  
**Backend**: Java Spring Boot 3.2

---

## 1. API 개요

### 1.1 엔드포인트 구조

```
/api/v1/auth          # 인증/인가
/api/v1/members       # 회원 관리
/api/v1/products      # 상품 관리
/api/v1/categories    # 카테고리
/api/v1/cart          # 장바구니
/api/v1/orders        # 주문 관리
/api/v1/payments      # 결제 관리
/api/v1/reviews       # 리뷰/평가
/api/v1/admin         # 관리자
```

### 1.2 공통 사항

#### Base URL
```
Production:  https://api.xlcfi.com
Staging:     https://api-staging.xlcfi.com
Development: http://localhost:8080
```

#### Request Headers
```http
Content-Type: application/json
Accept: application/json
Accept-Language: ko-KR,en-US (다국어 지원)
Authorization: Bearer {access_token} (인증 필요 시)
X-Request-ID: {uuid} (요청 추적용, 선택)
```

#### 표준 응답 형식 (ApiResponse<T>)

```java
// 성공 응답
{
  "success": true,
  "data": T,
  "message": "Success message",
  "timestamp": "2025-11-20T10:00:00Z"
}

// 에러 응답
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "Error message",
    "details": {}
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### HTTP Status Codes
- `200 OK`: 성공
- `201 Created`: 생성 성공
- `204 No Content`: 성공 (응답 본문 없음)
- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 중복/충돌
- `422 Unprocessable Entity`: 유효성 검증 실패
- `429 Too Many Requests`: Rate Limit 초과
- `500 Internal Server Error`: 서버 오류

---

## 2. 인증 API (Auth Controller)

### 2.1 회원가입

#### Endpoint
```http
POST /api/v1/auth/register
```

#### Request Body (MemberRegisterRequest)
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "USER",
  "language": "ko"
}
```

#### Request Fields
| 필드 | 타입 | 필수 | 설명 | 제약 |
|------|------|------|------|------|
| email | String | Y | 이메일 | @Email, 최대 255자 |
| password | String | Y | 비밀번호 | @Pattern(8-20자, 영문+숫자+특수문자) |
| name | String | Y | 이름 | @NotBlank, 2-100자 |
| phone | String | N | 전화번호 | @Pattern(010-XXXX-XXXX) |
| role | Enum | N | 역할 | USER(기본), SELLER |
| language | String | N | 언어 | ko(기본), en |

#### Response (201 Created) - AuthResponse
```json
{
  "success": true,
  "data": {
    "member": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "USER",
      "language": "ko",
      "status": "ACTIVE",
      "createdAt": "2025-11-20T10:00:00Z"
    },
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "message": "회원가입이 완료되었습니다."
}
```

#### Error Response (400 Bad Request) - ValidationErrorResponse
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 값이 유효하지 않습니다.",
    "details": {
      "email": "이미 사용 중인 이메일입니다.",
      "password": "비밀번호는 8자 이상이어야 합니다."
    }
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### Java Controller 예시
```java
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "유효성 검증 실패"),
        @ApiResponse(responseCode = "409", description = "이메일 중복")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> register(
        @Valid @RequestBody MemberRegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원가입이 완료되었습니다."));
    }
}
```

---

### 2.2 로그인

#### Endpoint
```http
POST /api/v1/auth/login
```

#### Request Body (LoginRequest)
```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

#### Response (200 OK) - AuthResponse
```json
{
  "success": true,
  "data": {
    "member": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "USER"
    },
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  },
  "message": "로그인 성공"
}
```

#### Error Response (401 Unauthorized)
```json
{
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "이메일 또는 비밀번호가 올바르지 않습니다."
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

---

### 2.3 토큰 갱신

#### Endpoint
```http
POST /api/v1/auth/refresh
```

#### Request Body (TokenRefreshRequest)
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### Response (200 OK) - TokenResponse
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600
  }
}
```

---

### 2.4 로그아웃

#### Endpoint
```http
POST /api/v1/auth/logout
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Response (200 OK)
```json
{
  "success": true,
  "message": "로그아웃되었습니다."
}
```

---

## 3. 회원 API (Member Controller)

### 3.1 내 정보 조회

#### Endpoint
```http
GET /api/v1/members/me
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Response (200 OK) - MemberResponse
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "USER",
    "language": "ko",
    "status": "ACTIVE",
    "profileImage": "https://s3.../profile.jpg",
    "createdAt": "2025-11-20T10:00:00Z",
    "updatedAt": "2025-11-20T10:00:00Z"
  }
}
```

---

### 3.2 내 정보 수정

#### Endpoint
```http
PUT /api/v1/members/me
```

#### Request Body (MemberUpdateRequest)
```json
{
  "name": "홍길동",
  "phone": "010-9999-8888",
  "language": "en"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "홍길동",
    "phone": "010-9999-8888",
    "language": "en",
    "updatedAt": "2025-11-20T11:00:00Z"
  },
  "message": "회원 정보가 수정되었습니다."
}
```

---

## 4. 상품 API (Product Controller)

### 4.1 상품 목록 조회 (페이징)

#### Endpoint
```http
GET /api/v1/products
```

#### Query Parameters
| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|---------|------|------|------|--------|
| page | Integer | N | 페이지 번호 (0부터 시작) | 0 |
| size | Integer | N | 페이지 크기 | 20 |
| sort | String | N | 정렬 기준 | createdAt,desc |
| categoryId | Long | N | 카테고리 ID | - |
| keyword | String | N | 검색어 | - |
| minPrice | BigDecimal | N | 최소 가격 | - |
| maxPrice | BigDecimal | N | 최대 가격 | - |
| status | Enum | N | 상품 상태 | ACTIVE |

#### Request Example
```http
GET /api/v1/products?page=0&size=20&categoryId=5&sort=price,asc
```

#### Response (200 OK) - PageResponse<ProductResponse>
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "배추김치 1kg",
        "nameEn": "Cabbage Kimchi 1kg",
        "description": "정성껏 담근 배추김치",
        "price": 15000,
        "currency": "KRW",
        "stockQuantity": 100,
        "images": [
          "https://s3.../image1.jpg",
          "https://s3.../image2.jpg"
        ],
        "ratingAverage": 4.5,
        "reviewCount": 10,
        "seller": {
          "id": 2,
          "name": "김판매",
          "sellerRating": 4.8
        },
        "category": {
          "id": 5,
          "name": "배추김치",
          "nameEn": "Cabbage Kimchi"
        },
        "status": "ACTIVE",
        "createdAt": "2025-11-20T10:00:00Z"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      }
    },
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true,
    "numberOfElements": 20,
    "empty": false
  }
}
```

#### Java Service 예시
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Page<ProductResponse> getProducts(ProductSearchCondition condition, Pageable pageable) {
        Page<Product> products = productRepository.searchProducts(condition, pageable);
        return products.map(ProductResponse::from);
    }
}
```

---

### 4.2 상품 상세 조회

#### Endpoint
```http
GET /api/v1/products/{productId}
```

#### Path Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| productId | Long | 상품 ID |

#### Response (200 OK) - ProductDetailResponse
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg",
    "nameEn": "Cabbage Kimchi 1kg",
    "description": "정성껏 담근 배추김치입니다...",
    "descriptionEn": "Traditionally fermented...",
    "price": 15000,
    "currency": "KRW",
    "stockQuantity": 100,
    "images": [
      "https://s3.../image1.jpg",
      "https://s3.../image2.jpg"
    ],
    "status": "ACTIVE",
    "viewCount": 150,
    "ratingAverage": 4.5,
    "reviewCount": 10,
    "seller": {
      "id": 2,
      "name": "김판매",
      "email": "seller@example.com",
      "sellerRating": 4.8,
      "totalSales": 1000
    },
    "category": {
      "id": 5,
      "name": "배추김치",
      "nameEn": "Cabbage Kimchi",
      "parentId": 1
    },
    "origin": {
      "country": "KR",
      "region": "전라남도",
      "producer": "김치명인"
    },
    "certifications": [
      {
        "type": "HACCP",
        "certNumber": "HACCP-2024-001",
        "validUntil": "2025-12-31"
      }
    ],
    "createdAt": "2025-11-20T10:00:00Z",
    "updatedAt": "2025-11-20T10:00:00Z"
  }
}
```

#### Error Response (404 Not Found)
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품을 찾을 수 없습니다.",
    "details": {
      "productId": 999
    }
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

---

### 4.3 상품 등록 (판매자)

#### Endpoint
```http
POST /api/v1/products
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: SELLER
```

#### Request Body (ProductCreateRequest)
```json
{
  "categoryId": 5,
  "name": "배추김치 1kg",
  "nameEn": "Cabbage Kimchi 1kg",
  "description": "정성껏 담근 배추김치",
  "descriptionEn": "Traditionally fermented",
  "price": 15000,
  "currency": "KRW",
  "stockQuantity": 100,
  "images": [
    "https://s3.../image1.jpg",
    "https://s3.../image2.jpg"
  ],
  "origin": {
    "country": "KR",
    "region": "전라남도",
    "producer": "김치명인"
  },
  "status": "ACTIVE"
}
```

#### Validation
```java
@Data
@NoArgsConstructor
public class ProductCreateRequest {
    
    @NotNull(message = "카테고리는 필수입니다.")
    private Long categoryId;
    
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 255, message = "상품명은 255자를 초과할 수 없습니다.")
    private String name;
    
    @NotNull(message = "가격은 필수입니다.")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다.")
    private BigDecimal price;
    
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private Integer stockQuantity;
    
    @Size(max = 10, message = "이미지는 최대 10개까지 등록 가능합니다.")
    private List<String> images;
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg",
    "price": 15000,
    "status": "ACTIVE",
    "createdAt": "2025-11-20T10:00:00Z"
  },
  "message": "상품이 등록되었습니다."
}
```

---

### 4.4 상품 수정 (판매자)

#### Endpoint
```http
PUT /api/v1/products/{productId}
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: SELLER (본인 상품만)
```

#### Request Body (ProductUpdateRequest)
```json
{
  "price": 14000,
  "stockQuantity": 150,
  "status": "ACTIVE"
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "price": 14000,
    "stockQuantity": 150,
    "updatedAt": "2025-11-20T11:00:00Z"
  },
  "message": "상품이 수정되었습니다."
}
```

---

### 4.5 상품 삭제 (판매자)

#### Endpoint
```http
DELETE /api/v1/products/{productId}
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: SELLER (본인 상품만) 또는 ADMIN
```

#### Response (204 No Content)
```
(응답 본문 없음)
```

---

## 5. 주문 API (Order Controller)

### 5.1 주문 생성

#### Endpoint
```http
POST /api/v1/orders
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Request Body (OrderCreateRequest)
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "shippingAddress": {
    "recipientName": "홍길동",
    "phone": "010-1234-5678",
    "address": "서울시 강남구 테헤란로 123",
    "addressDetail": "456호",
    "zipcode": "06000",
    "country": "KR"
  },
  "buyerNote": "문 앞에 놓아주세요"
}
```

#### Response (201 Created) - OrderResponse
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD-20251120-0001",
    "buyerId": 3,
    "sellerId": 2,
    "subtotal": 30000,
    "shippingFee": 3000,
    "total": 33000,
    "currency": "KRW",
    "status": "PENDING",
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "배추김치 1kg",
        "quantity": 2,
        "unitPrice": 15000,
        "subtotal": 30000
      }
    ],
    "shippingAddress": {
      "recipientName": "홍길동",
      "phone": "010-1234-5678",
      "address": "서울시 강남구 테헤란로 123",
      "addressDetail": "456호",
      "zipcode": "06000"
    },
    "createdAt": "2025-11-20T10:00:00Z"
  },
  "message": "주문이 생성되었습니다."
}
```

#### Java Entity 예시
```java
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;
    
    @Embedded
    private ShippingAddress shippingAddress;
    
    @Column(columnDefinition = "TEXT")
    private String buyerNote;
    
    // 비즈니스 로직
    public static Order create(Member member, List<OrderItem> items, ShippingAddress address) {
        Order order = new Order();
        order.member = member;
        order.orderNumber = generateOrderNumber();
        order.items = items;
        order.shippingAddress = address;
        order.status = OrderStatus.PENDING;
        order.calculateAmounts();
        return order;
    }
    
    private void calculateAmounts() {
        this.subtotal = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.shippingFee = calculateShippingFee();
        this.total = subtotal.add(shippingFee);
    }
    
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 중인 주문만 확정할 수 있습니다.");
        }
        this.status = OrderStatus.CONFIRMED;
    }
    
    public void cancel() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }
}
```

---

### 5.2 주문 목록 조회

#### Endpoint
```http
GET /api/v1/orders
```

#### Query Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| status | OrderStatus | 주문 상태 필터 |
| page | Integer | 페이지 번호 |
| size | Integer | 페이지 크기 |
| sort | String | 정렬 기준 |

#### Response (200 OK) - PageResponse<OrderSummaryResponse>
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "orderNumber": "ORD-20251120-0001",
        "total": 33000,
        "currency": "KRW",
        "status": "DELIVERED",
        "itemsCount": 1,
        "createdAt": "2025-11-20T10:00:00Z",
        "deliveredAt": "2025-11-21T15:00:00Z"
      }
    ],
    "totalElements": 50,
    "totalPages": 3,
    "number": 0,
    "size": 20
  }
}
```

---

## 6. 결제 API (Payment Controller)

### 6.1 결제 시작 (토스페이먼츠)

#### Endpoint
```http
POST /api/v1/payments
```

#### Request Body (PaymentRequest)
```json
{
  "orderId": 1,
  "paymentMethod": "CARD",
  "successUrl": "https://xlcfi.com/payment/success",
  "failUrl": "https://xlcfi.com/payment/fail"
}
```

#### Response (201 Created) - PaymentResponse
```json
{
  "success": true,
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "amount": 33000,
    "currency": "KRW",
    "status": "PENDING",
    "paymentKey": "toss_payment_key_123",
    "checkoutUrl": "https://payment.toss.im/...",
    "createdAt": "2025-11-20T10:00:00Z"
  }
}
```

---

### 6.2 결제 승인 (Callback)

#### Endpoint
```http
POST /api/v1/payments/confirm
```

#### Request Body (PaymentConfirmRequest)
```json
{
  "paymentKey": "toss_payment_key_123",
  "orderId": "ORD-20251120-0001",
  "amount": 33000
}
```

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "paymentId": 1,
    "status": "COMPLETED",
    "approvedAt": "2025-11-20T10:05:00Z"
  },
  "message": "결제가 완료되었습니다."
}
```

#### Java Service 예시
```java
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final TossPaymentClient tossClient;
    
    public PaymentResponse confirmPayment(PaymentConfirmRequest request) {
        // 1. 결제 정보 조회
        Payment payment = paymentRepository.findByPaymentKey(request.getPaymentKey())
            .orElseThrow(() -> new PaymentNotFoundException("결제 정보를 찾을 수 없습니다."));
        
        // 2. 금액 검증
        if (!payment.getAmount().equals(request.getAmount())) {
            throw new PaymentAmountMismatchException("결제 금액이 일치하지 않습니다.");
        }
        
        // 3. PG사 승인 요청
        TossPaymentConfirmResponse tossResponse = tossClient.confirm(request);
        
        // 4. 결제 상태 업데이트
        payment.complete(tossResponse.getApprovedAt());
        
        // 5. 주문 상태 업데이트
        orderService.confirmOrder(payment.getOrder().getId());
        
        // 6. 이벤트 발행 (재고 차감, 알림 등)
        eventPublisher.publishEvent(new PaymentCompletedEvent(payment));
        
        return PaymentResponse.from(payment);
    }
}
```

---

## 7. 리뷰 API (Review Controller)

### 7.1 리뷰 작성

#### Endpoint
```http
POST /api/v1/reviews
```

#### Authorization
```http
Authorization: Bearer {access_token}
```

#### Request Body (ReviewCreateRequest)
```json
{
  "productId": 1,
  "orderId": 1,
  "rating": 5,
  "title": "정말 맛있어요!",
  "content": "전통 방식으로 담근 김치라 그런지 맛이 일품입니다.",
  "images": [
    "https://s3.../review1.jpg"
  ]
}
```

#### Validation
```java
@Data
@NoArgsConstructor
public class ReviewCreateRequest {
    
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
    
    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;
    
    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5 이하여야 합니다.")
    private Integer rating;
    
    @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 10, max = 1000, message = "내용은 10자 이상 1000자 이하여야 합니다.")
    private String content;
    
    @Size(max = 5, message = "이미지는 최대 5개까지 등록 가능합니다.")
    private List<String> images;
}
```

#### Response (201 Created)
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "rating": 5,
    "title": "정말 맛있어요!",
    "content": "전통 방식으로 담근 김치라...",
    "isVerifiedPurchase": true,
    "createdAt": "2025-11-20T10:00:00Z"
  },
  "message": "리뷰가 등록되었습니다."
}
```

---

### 7.2 상품별 리뷰 목록

#### Endpoint
```http
GET /api/v1/products/{productId}/reviews
```

#### Query Parameters
| 파라미터 | 타입 | 설명 |
|---------|------|------|
| page | Integer | 페이지 번호 |
| size | Integer | 페이지 크기 |
| rating | Integer | 평점 필터 (1-5) |
| sort | String | 정렬 (latest, rating) |

#### Response (200 OK)
```json
{
  "success": true,
  "data": {
    "reviews": [
      {
        "id": 1,
        "member": {
          "id": 3,
          "name": "홍길동",
          "profileImage": "https://s3.../profile.jpg"
        },
        "rating": 5,
        "title": "정말 맛있어요!",
        "content": "전통 방식으로 담근 김치라...",
        "images": [
          "https://s3.../review1.jpg"
        ],
        "isVerifiedPurchase": true,
        "helpfulCount": 10,
        "createdAt": "2025-11-20T10:00:00Z"
      }
    ],
    "summary": {
      "averageRating": 4.5,
      "totalReviews": 50,
      "ratingDistribution": {
        "5": 30,
        "4": 15,
        "3": 3,
        "2": 1,
        "1": 1
      }
    },
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

## 8. 관리자 API (Admin Controller)

### 8.1 대시보드 통계

#### Endpoint
```http
GET /api/v1/admin/dashboard
```

#### Authorization
```http
Authorization: Bearer {access_token}
Role: ADMIN
```

#### Response (200 OK) - DashboardResponse
```json
{
  "success": true,
  "data": {
    "today": {
      "sales": 1500000,
      "orders": 45,
      "newMembers": 12,
      "reviews": 8
    },
    "thisMonth": {
      "sales": 45000000,
      "orders": 1200,
      "newMembers": 350,
      "reviews": 240
    },
    "topProducts": [
      {
        "id": 1,
        "name": "배추김치 1kg",
        "salesCount": 150,
        "revenue": 2250000
      }
    ],
    "recentOrders": [
      {
        "id": 1,
        "orderNumber": "ORD-20251120-0001",
        "memberName": "홍길동",
        "total": 33000,
        "status": "PENDING",
        "createdAt": "2025-11-20T10:00:00Z"
      }
    ]
  }
}
```

---

## 9. 공통 DTO 및 Enum

### 9.1 OrderStatus (주문 상태)
```java
public enum OrderStatus {
    PENDING("대기"),
    CONFIRMED("확인"),
    PREPARING("준비중"),
    SHIPPED("배송중"),
    DELIVERED("배송완료"),
    CANCELLED("취소"),
    REFUNDED("환불");
    
    private final String description;
}
```

### 9.2 PaymentStatus (결제 상태)
```java
public enum PaymentStatus {
    PENDING("대기"),
    COMPLETED("완료"),
    FAILED("실패"),
    CANCELLED("취소"),
    REFUNDED("환불");
    
    private final String description;
}
```

### 9.3 MemberRole (회원 역할)
```java
public enum MemberRole {
    USER("일반회원"),
    SELLER("판매자"),
    ADMIN("관리자");
    
    private final String description;
}
```

---

## 10. 에러 코드 정의

### 10.1 ErrorCode Enum
```java
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    
    // 인증/인가 (1xxx)
    INVALID_CREDENTIALS("E1001", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("E1002", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    INVALID_TOKEN("E1003", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("E1004", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("E1005", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    
    // 상품 (2xxx)
    PRODUCT_NOT_FOUND("E2001", "상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INSUFFICIENT_STOCK("E2002", "재고가 부족합니다.", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_AVAILABLE("E2003", "판매 중인 상품이 아닙니다.", HttpStatus.BAD_REQUEST),
    
    // 주문 (3xxx)
    ORDER_NOT_FOUND("E3001", "주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_CANCEL_ORDER("E3002", "주문을 취소할 수 없습니다.", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_CONFIRMED("E3003", "이미 확인된 주문입니다.", HttpStatus.BAD_REQUEST),
    
    // 결제 (4xxx)
    PAYMENT_FAILED("E4001", "결제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH("E4002", "결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND("E4003", "결제 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    
    // 리뷰 (5xxx)
    DUPLICATE_REVIEW("E5001", "이미 리뷰를 작성하셨습니다.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("E5002", "리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_PURCHASED_PRODUCT("E5003", "구매한 상품만 리뷰를 작성할 수 있습니다.", HttpStatus.BAD_REQUEST),
    
    // 공통 (9xxx)
    VALIDATION_ERROR("E9001", "입력 값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR("E9999", "서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
```

---

## 11. Rate Limiting

### 11.1 제한 정책 (Bucket4j + Redis)
```yaml
rate-limit:
  anonymous: 100 requests / 15분
  user: 1000 requests / 15분
  seller: 2000 requests / 15분
  admin: 무제한
```

### 11.2 응답 헤더
```http
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 995
X-RateLimit-Reset: 1700000000
```

### 11.3 초과 시 응답 (429 Too Many Requests)
```json
{
  "success": false,
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "요청 한도를 초과했습니다.",
    "details": {
      "retryAfter": 900
    }
  },
  "timestamp": "2025-11-20T10:00:00Z"
}
```

---

## 12. API 테스트 예시

### 12.1 cURL 예시
```bash
# 로그인
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass123"}'

# 상품 목록 조회
curl -X GET "http://localhost:8080/api/v1/products?page=0&size=20"

# 상품 등록 (인증 필요)
curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"김치","price":15000,"categoryId":5}'
```

### 12.2 Java RestTemplate 예시
```java
@Service
@RequiredArgsConstructor
public class ApiClient {
    
    private final RestTemplate restTemplate;
    
    public ProductResponse getProduct(Long productId) {
        String url = "http://localhost:8080/api/v1/products/" + productId;
        
        ResponseEntity<ApiResponse<ProductResponse>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<ApiResponse<ProductResponse>>() {}
        );
        
        return response.getBody().getData();
    }
}
```

---

**문서 작성**: AI Assistant  
**검토 필요**: Backend 개발자, API 설계자  
**다음 업데이트**: Phase 1 구현 완료 시

