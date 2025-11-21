# Java Spring Boot API 상세 명세서
# 메인 비즈니스 로직 서비스

**문서 버전**: v1.0  
**작성일**: 2025-11-20  
**프로젝트**: K-Food 거래 플랫폼 (XLCfi)  
**Base URL**: `https://api.spicyjump.com/v1`

---

## 목차

1. [API 개요](#1-api-개요)
2. [인증 서비스 (Auth Service)](#2-인증-서비스)
3. [상품 서비스 (Product Service)](#3-상품-서비스)
4. [주문 서비스 (Order Service)](#4-주문-서비스)
5. [결제 서비스 (Payment Service)](#5-결제-서비스)
6. [리뷰 서비스 (Review Service)](#6-리뷰-서비스)
7. [관리자 서비스 (Admin Service)](#7-관리자-서비스)
8. [블록체인 서비스 (Blockchain Service)](#8-블록체인-서비스)

---

## 1. API 개요

### 1.1 서비스 구조

```
API Gateway (Port: 8080)
├── Auth Service (Port: 8081)
├── Product Service (Port: 8082)
├── Order Service (Port: 8083)
├── Payment Service (Port: 8084)
├── Review Service (Port: 8085)
├── Admin Service (Port: 8086)
└── Blockchain Service (Port: 8087)
```

### 1.2 공통 사항

#### Request Headers
```http
Content-Type: application/json
Accept: application/json
Accept-Language: ko,en
Authorization: Bearer {access_token}
X-API-Version: 1.0
X-Request-ID: {unique_request_id}
```

#### Response Format (Spring Boot 표준)
```json
{
  "timestamp": "2025-11-20T10:00:00Z",
  "status": 200,
  "success": true,
  "data": {},
  "message": "Success",
  "path": "/v1/products",
  "requestId": "req-123-abc"
}
```

#### Error Response Format
```json
{
  "timestamp": "2025-11-20T10:00:00Z",
  "status": 400,
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": [
      {
        "field": "email",
        "message": "Email is required",
        "rejectedValue": null
      }
    ]
  },
  "path": "/v1/auth/register",
  "requestId": "req-123-abc"
}
```

### 1.3 HTTP Status Codes

| Code | 의미 | 사용 |
|------|------|------|
| 200 | OK | 성공 |
| 201 | Created | 리소스 생성 |
| 204 | No Content | 성공 (응답 없음) |
| 400 | Bad Request | 잘못된 요청 |
| 401 | Unauthorized | 인증 실패 |
| 403 | Forbidden | 권한 없음 |
| 404 | Not Found | 리소스 없음 |
| 409 | Conflict | 리소스 충돌 |
| 422 | Unprocessable Entity | 유효성 검증 실패 |
| 429 | Too Many Requests | Rate Limit 초과 |
| 500 | Internal Server Error | 서버 오류 |
| 503 | Service Unavailable | 서비스 일시 중단 |

---

## 2. 인증 서비스 (Auth Service)

**Base Path**: `/v1/auth`  
**Port**: 8081  
**책임**: 회원 가입, 로그인, 토큰 관리

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
  "name": "홍길동",
  "phone": "010-1234-5678",
  "role": "BUYER",
  "language": "ko",
  "marketingConsent": true
}
```

#### Request Validation Rules

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| email | String | Y | 이메일 형식, 중복 불가 |
| password | String | Y | 8-50자, 영문+숫자+특수문자 |
| name | String | Y | 2-200자 |
| phone | String | N | 전화번호 형식 (국가별) |
| role | Enum | N | BUYER(기본), SELLER |
| language | Enum | N | ko(기본), en |
| marketingConsent | Boolean | N | 마케팅 수신 동의 |

#### Response (201 Created)
```json
{
  "timestamp": "2025-11-20T10:00:00Z",
  "status": 201,
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "BUYER",
      "status": "ACTIVE",
      "language": "ko",
      "createdAt": "2025-11-20T10:00:00Z"
    },
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
      "tokenType": "Bearer",
      "expiresIn": 3600,
      "refreshExpiresIn": 2592000
    }
  },
  "message": "회원가입이 완료되었습니다"
}
```

#### Error Responses

**Email Duplicate (409 Conflict)**
```json
{
  "status": 409,
  "success": false,
  "error": {
    "code": "EMAIL_ALREADY_EXISTS",
    "message": "이미 사용 중인 이메일입니다",
    "details": [{
      "field": "email",
      "message": "user@example.com은 이미 등록되어 있습니다"
    }]
  }
}
```

**Validation Error (422 Unprocessable Entity)**
```json
{
  "status": 422,
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "입력 값이 유효하지 않습니다",
    "details": [
      {
        "field": "password",
        "message": "비밀번호는 8자 이상이어야 합니다",
        "rejectedValue": "pass"
      },
      {
        "field": "email",
        "message": "올바른 이메일 형식이 아닙니다",
        "rejectedValue": "invalid-email"
      }
    ]
  }
}
```

#### Java Controller Example
```java
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "회원가입이 완료되었습니다"));
    }
}
```

#### Java DTO Example
```java
@Data
@Builder
public class RegisterRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
        message = "비밀번호는 8자 이상, 영문+숫자+특수문자를 포함해야 합니다"
    )
    private String password;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 200, message = "이름은 2-200자여야 합니다")
    private String name;
    
    @Pattern(regexp = "^01[0-9]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
    private String phone;
    
    private UserRole role = UserRole.BUYER;
    
    private Language language = Language.KO;
    
    private Boolean marketingConsent = false;
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
  "password": "SecurePass123!",
  "rememberMe": false
}
```

#### Response (200 OK)
```json
{
  "timestamp": "2025-11-20T10:00:00Z",
  "status": 200,
  "success": true,
  "data": {
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동",
      "role": "BUYER",
      "status": "ACTIVE"
    },
    "tokens": {
      "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
      "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
      "tokenType": "Bearer",
      "expiresIn": 3600
    }
  },
  "message": "로그인 성공"
}
```

#### Error Response (401 Unauthorized)
```json
{
  "status": 401,
  "success": false,
  "error": {
    "code": "INVALID_CREDENTIALS",
    "message": "이메일 또는 비밀번호가 일치하지 않습니다"
  }
}
```

#### Account Locked (403 Forbidden)
```json
{
  "status": 403,
  "success": false,
  "error": {
    "code": "ACCOUNT_LOCKED",
    "message": "계정이 잠겼습니다. 5회 연속 로그인 실패로 인해 15분간 로그인할 수 없습니다",
    "details": {
      "lockReason": "MULTIPLE_LOGIN_FAILURES",
      "unlockAt": "2025-11-20T10:15:00Z"
    }
  }
}
```

---

### 2.3 토큰 갱신

#### Endpoint
```http
POST /v1/auth/refresh
```

#### Request Body
```json
{
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
}
```

#### Response (200 OK)
```json
{
  "status": 200,
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
POST /v1/auth/logout
```

#### Request Headers
```http
Authorization: Bearer {access_token}
```

#### Response (204 No Content)
```
(empty response body)
```

---

### 2.5 내 정보 조회

#### Endpoint
```http
GET /v1/auth/me
```

#### Request Headers
```http
Authorization: Bearer {access_token}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동",
    "phone": "010-1234-5678",
    "role": "BUYER",
    "status": "ACTIVE",
    "language": "ko",
    "createdAt": "2025-11-20T10:00:00Z",
    "lastLoginAt": "2025-11-20T11:00:00Z"
  }
}
```

---

### 2.6 비밀번호 변경

#### Endpoint
```http
PUT /v1/auth/password
```

#### Request Headers
```http
Authorization: Bearer {access_token}
```

#### Request Body
```json
{
  "currentPassword": "OldPass123!",
  "newPassword": "NewPass456!",
  "confirmPassword": "NewPass456!"
}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "message": "비밀번호가 변경되었습니다"
}
```

#### Error Response (401 Unauthorized)
```json
{
  "status": 401,
  "success": false,
  "error": {
    "code": "INVALID_CURRENT_PASSWORD",
    "message": "현재 비밀번호가 일치하지 않습니다"
  }
}
```

---

## 3. 상품 서비스 (Product Service)

**Base Path**: `/v1/products`  
**Port**: 8082  
**책임**: 상품 CRUD, 재고 관리, 검색

### 3.1 상품 목록 조회

#### Endpoint
```http
GET /v1/products
```

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 | 기본값 |
|---------|------|------|------|--------|
| page | Integer | N | 페이지 번호 (1부터 시작) | 1 |
| size | Integer | N | 페이지 크기 (최대 100) | 20 |
| sort | String | N | 정렬 기준 (createdAt, price, rating) | createdAt |
| direction | String | N | 정렬 방향 (asc, desc) | desc |
| categoryId | Long | N | 카테고리 ID | - |
| search | String | N | 검색어 (상품명) | - |
| minPrice | BigDecimal | N | 최소 가격 | - |
| maxPrice | BigDecimal | N | 최대 가격 | - |
| status | String | N | 상태 (PUBLISHED, DRAFT) | PUBLISHED |
| sellerId | Long | N | 판매자 ID | - |

#### Request Example
```http
GET /v1/products?page=1&size=20&categoryId=5&sort=price&direction=asc
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "배추김치 1kg",
        "nameEn": "Cabbage Kimchi 1kg",
        "description": "정성껏 담근 배추김치",
        "price": 15.00,
        "currency": "USD",
        "stockQuantity": 100,
        "status": "PUBLISHED",
        "images": [
          {
            "url": "https://s3.../image1.jpg",
            "order": 1,
            "isPrimary": true
          }
        ],
        "ratingAverage": 4.5,
        "reviewCount": 10,
        "viewCount": 150,
        "seller": {
          "id": 2,
          "name": "John Kim",
          "email": "seller@example.com"
        },
        "category": {
          "id": 5,
          "name": "배추김치",
          "nameEn": "Cabbage Kimchi",
          "parentId": 1
        },
        "createdAt": "2025-11-20T10:00:00Z",
        "updatedAt": "2025-11-20T10:00:00Z"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 20,
      "sort": "price,asc"
    },
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false,
    "numberOfElements": 20
  }
}
```

#### Java Service Example
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    private final ProductQueryRepository productQueryRepository;
    
    public Page<ProductResponse> getProducts(ProductSearchRequest request, Pageable pageable) {
        Page<Product> products = productQueryRepository.search(request, pageable);
        return products.map(ProductResponse::from);
    }
}
```

#### QueryDSL Example
```java
@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    public Page<Product> search(ProductSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        
        // 카테고리 필터
        if (request.getCategoryId() != null) {
            builder.and(product.category.id.eq(request.getCategoryId()));
        }
        
        // 가격 범위 필터
        if (request.getMinPrice() != null) {
            builder.and(product.price.goe(request.getMinPrice()));
        }
        if (request.getMaxPrice() != null) {
            builder.and(product.price.loe(request.getMaxPrice()));
        }
        
        // 검색어 (상품명)
        if (StringUtils.hasText(request.getSearch())) {
            builder.and(product.name.containsIgnoreCase(request.getSearch())
                .or(product.nameEn.containsIgnoreCase(request.getSearch())));
        }
        
        // 상태 필터
        if (request.getStatus() != null) {
            builder.and(product.status.eq(request.getStatus()));
        }
        
        List<Product> content = queryFactory
            .selectFrom(product)
            .leftJoin(product.seller, user).fetchJoin()
            .leftJoin(product.category, category).fetchJoin()
            .where(builder)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(getOrderSpecifier(pageable.getSort()))
            .fetch();
        
        Long total = queryFactory
            .select(product.count())
            .from(product)
            .where(builder)
            .fetchOne();
        
        return new PageImpl<>(content, pageable, total);
    }
    
    private OrderSpecifier<?>[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();
        
        sort.forEach(order -> {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String property = order.getProperty();
            
            switch (property) {
                case "price":
                    orders.add(new OrderSpecifier<>(direction, product.price));
                    break;
                case "rating":
                    orders.add(new OrderSpecifier<>(direction, product.ratingAverage));
                    break;
                default:
                    orders.add(new OrderSpecifier<>(direction, product.createdAt));
            }
        });
        
        return orders.toArray(new OrderSpecifier[0]);
    }
}
```

---

### 3.2 상품 상세 조회

#### Endpoint
```http
GET /v1/products/{productId}
```

#### Path Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| productId | Long | 상품 ID |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg",
    "nameEn": "Cabbage Kimchi 1kg",
    "description": "정성껏 담근 배추김치입니다. 국내산 배추와 천일염을 사용하였습니다.",
    "descriptionEn": "Traditionally fermented cabbage kimchi...",
    "price": 15.00,
    "currency": "USD",
    "stockQuantity": 100,
    "status": "PUBLISHED",
    "images": [
      {
        "id": 1,
        "url": "https://s3.amazonaws.com/.../kimchi1.jpg",
        "order": 1,
        "isPrimary": true
      },
      {
        "id": 2,
        "url": "https://s3.amazonaws.com/.../kimchi2.jpg",
        "order": 2,
        "isPrimary": false
      }
    ],
    "ratingAverage": 4.5,
    "reviewCount": 10,
    "viewCount": 150,
    "seller": {
      "id": 2,
      "name": "John Kim",
      "email": "seller@example.com",
      "phone": "010-1234-5678",
      "totalProducts": 50,
      "averageRating": 4.6
    },
    "category": {
      "id": 5,
      "name": "배추김치",
      "nameEn": "Cabbage Kimchi",
      "parentId": 1,
      "parentName": "김치/반찬"
    },
    "createdAt": "2025-11-20T10:00:00Z",
    "updatedAt": "2025-11-20T10:00:00Z"
  }
}
```

#### Error Response (404 Not Found)
```json
{
  "status": 404,
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품을 찾을 수 없습니다",
    "details": {
      "productId": 999
    }
  }
}
```

#### Cache Strategy
```java
@Service
@RequiredArgsConstructor
public class ProductService {
    
    @Cacheable(value = "products", key = "#productId", unless = "#result == null")
    public ProductDetailResponse getProduct(Long productId) {
        Product product = productRepository.findByIdWithDetails(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        // 조회수 증가 (비동기)
        productEventPublisher.publishViewEvent(productId);
        
        return ProductDetailResponse.from(product);
    }
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
X-User-Role: SELLER
```

#### Request Body
```json
{
  "categoryId": 5,
  "name": "배추김치 1kg",
  "nameEn": "Cabbage Kimchi 1kg",
  "description": "정성껏 담근 배추김치",
  "descriptionEn": "Traditionally fermented",
  "price": 15.00,
  "currency": "USD",
  "stockQuantity": 100,
  "images": [
    {
      "url": "https://s3.../image1.jpg",
      "order": 1,
      "isPrimary": true
    },
    {
      "url": "https://s3.../image2.jpg",
      "order": 2,
      "isPrimary": false
    }
  ],
  "status": "PUBLISHED"
}
```

#### Request Validation

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| categoryId | Long | Y | 존재하는 카테고리 |
| name | String | Y | 2-500자 |
| nameEn | String | N | 2-500자 |
| description | String | Y | 10-5000자 |
| price | BigDecimal | Y | 0 이상 |
| stockQuantity | Integer | Y | 0 이상 |
| images | List | N | 최대 5개 |
| status | Enum | N | DRAFT, PUBLISHED |

#### Response (201 Created)
```json
{
  "status": 201,
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg",
    "status": "PUBLISHED",
    "createdAt": "2025-11-20T10:00:00Z"
  },
  "message": "상품이 등록되었습니다"
}
```

#### Java Controller Example
```java
@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductCreateResponse>> createProduct(
        @Valid @RequestBody ProductCreateRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ProductCreateResponse response = productService.createProduct(request, currentUser.getId());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, "상품이 등록되었습니다"));
    }
}
```

#### Java Service Example with Transaction
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ProductEventPublisher eventPublisher;
    
    public ProductCreateResponse createProduct(ProductCreateRequest request, Long sellerId) {
        // 카테고리 존재 확인
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
        
        // 판매자 확인
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new UserNotFoundException(sellerId));
        
        if (!seller.getRole().equals(UserRole.SELLER)) {
            throw new UnauthorizedException("판매자만 상품을 등록할 수 있습니다");
        }
        
        // 상품 생성
        Product product = Product.builder()
            .seller(seller)
            .category(category)
            .name(request.getName())
            .nameEn(request.getNameEn())
            .description(request.getDescription())
            .descriptionEn(request.getDescriptionEn())
            .price(request.getPrice())
            .currency(request.getCurrency())
            .stockQuantity(request.getStockQuantity())
            .status(request.getStatus())
            .build();
        
        // 이미지 추가
        if (request.getImages() != null) {
            request.getImages().forEach(imageDto -> 
                product.addImage(imageDto.getUrl(), imageDto.getOrder(), imageDto.getIsPrimary())
            );
        }
        
        Product savedProduct = productRepository.save(product);
        
        // 이벤트 발행 (Kafka)
        eventPublisher.publishProductCreated(savedProduct);
        
        // 검색 인덱스 업데이트 (Elasticsearch)
        elasticsearchService.indexProduct(savedProduct);
        
        return ProductCreateResponse.from(savedProduct);
    }
}
```

---

### 3.4 상품 수정 (판매자)

#### Endpoint
```http
PUT /v1/products/{productId}
```

#### Authorization
```http
Authorization: Bearer {access_token}
X-User-Role: SELLER
```

#### Request Body
```json
{
  "name": "배추김치 1kg (특가)",
  "price": 14.00,
  "stockQuantity": 150,
  "status": "PUBLISHED"
}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "id": 1,
    "name": "배추김치 1kg (특가)",
    "price": 14.00,
    "stockQuantity": 150,
    "updatedAt": "2025-11-20T11:00:00Z"
  },
  "message": "상품이 수정되었습니다"
}
```

#### Error Response (403 Forbidden)
```json
{
  "status": 403,
  "success": false,
  "error": {
    "code": "FORBIDDEN",
    "message": "본인의 상품만 수정할 수 있습니다"
  }
}
```

#### Java Service Example
```java
@Transactional
public ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request, Long currentUserId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ProductNotFoundException(productId));
    
    // 권한 확인 (본인 상품인지)
    if (!product.getSeller().getId().equals(currentUserId)) {
        throw new ForbiddenException("본인의 상품만 수정할 수 있습니다");
    }
    
    // 상품 정보 수정
    product.update(
        request.getName(),
        request.getNameEn(),
        request.getDescription(),
        request.getDescriptionEn(),
        request.getPrice(),
        request.getStockQuantity(),
        request.getStatus()
    );
    
    // 캐시 무효화
    cacheManager.evict("products", productId);
    
    // 이벤트 발행
    eventPublisher.publishProductUpdated(product);
    
    return ProductUpdateResponse.from(product);
}
```

---

### 3.5 상품 삭제 (판매자)

#### Endpoint
```http
DELETE /v1/products/{productId}
```

#### Authorization
```http
Authorization: Bearer {access_token}
X-User-Role: SELLER or ADMIN
```

#### Response (204 No Content)
```
(empty response body)
```

#### Soft Delete Strategy
```java
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ProductStatus.DISCONTINUED;
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

---

### 3.6 재고 관리 (판매자)

#### Endpoint
```http
PATCH /v1/products/{productId}/stock
```

#### Request Body
```json
{
  "quantity": 50,
  "operation": "ADD"
}
```

#### Operation Types
- `ADD`: 재고 추가
- `SUBTRACT`: 재고 차감
- `SET`: 재고 설정

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "productId": 1,
    "previousStock": 100,
    "currentStock": 150,
    "operation": "ADD",
    "updatedAt": "2025-11-20T11:00:00Z"
  }
}
```

#### Concurrent Stock Control
```java
@Service
@RequiredArgsConstructor
public class StockService {
    
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Integer> redisTemplate;
    
    @Transactional
    public StockUpdateResponse updateStock(Long productId, StockUpdateRequest request) {
        // Redis 분산 락 획득
        String lockKey = "stock:lock:" + productId;
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, 1, Duration.ofSeconds(10));
        
        if (!lockAcquired) {
            throw new ConcurrentModificationException("다른 작업이 진행 중입니다");
        }
        
        try {
            Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
            
            int previousStock = product.getStockQuantity();
            int newStock;
            
            switch (request.getOperation()) {
                case ADD:
                    newStock = previousStock + request.getQuantity();
                    break;
                case SUBTRACT:
                    newStock = previousStock - request.getQuantity();
                    if (newStock < 0) {
                        throw new InsufficientStockException(productId, previousStock);
                    }
                    break;
                case SET:
                    newStock = request.getQuantity();
                    break;
                default:
                    throw new IllegalArgumentException("Invalid operation");
            }
            
            product.setStockQuantity(newStock);
            
            // Redis 캐시 업데이트
            redisTemplate.opsForValue().set("stock:" + productId, newStock);
            
            return StockUpdateResponse.builder()
                .productId(productId)
                .previousStock(previousStock)
                .currentStock(newStock)
                .operation(request.getOperation())
                .updatedAt(LocalDateTime.now())
                .build();
        } finally {
            // 락 해제
            redisTemplate.delete(lockKey);
        }
    }
}
```

---

## 4. 주문 서비스 (Order Service)

**Base Path**: `/v1/orders`  
**Port**: 8083  
**책임**: 주문 생성, 조회, 상태 관리

### 4.1 주문 생성

#### Endpoint
```http
POST /v1/orders
```

#### Authorization
```http
Authorization: Bearer {access_token}
X-User-Role: BUYER
```

#### Request Body
```json
{
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "recipientName": "홍길동",
    "phone": "010-1234-5678",
    "addressLine1": "서울시 강남구 테헤란로 123",
    "addressLine2": "ABC빌딩 456호",
    "city": "서울",
    "state": "서울특별시",
    "zipCode": "06000",
    "country": "KR"
  },
  "buyerNote": "배송 전 연락 주세요",
  "paymentMethod": "STRIPE"
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "success": true,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251120-0001",
    "buyerId": 3,
    "sellerId": 2,
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "배추김치 1kg",
        "productNameEn": "Cabbage Kimchi 1kg",
        "quantity": 2,
        "unitPrice": 15.00,
        "subtotal": 30.00
      }
    ],
    "subtotal": 30.00,
    "shippingFee": 3.00,
    "tax": 2.31,
    "total": 35.31,
    "currency": "USD",
    "status": "PENDING",
    "shippingAddress": {
      "recipientName": "홍길동",
      "phone": "010-1234-5678",
      "addressLine1": "서울시 강남구 테헤란로 123",
      "zipCode": "06000",
      "country": "KR"
    },
    "createdAt": "2025-11-20T10:00:00Z",
    "estimatedDeliveryDate": "2025-11-25T00:00:00Z"
  },
  "message": "주문이 생성되었습니다"
}
```

#### Java Service with Saga Pattern
```java
@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final OrderEventPublisher eventPublisher;
    
    public OrderCreateResponse createOrder(OrderCreateRequest request, Long buyerId) {
        // 1. 주문 전 검증
        validateOrderItems(request.getItems());
        
        // 2. 재고 확인 및 임시 확보
        List<StockReservation> reservations = reserveStock(request.getItems());
        
        try {
            // 3. 주문 생성
            Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .buyerId(buyerId)
                .sellerId(getSellerId(request.getItems()))
                .shippingAddress(request.getShippingAddress())
                .buyerNote(request.getBuyerNote())
                .status(OrderStatus.PENDING)
                .build();
            
            // 4. 주문 항목 추가
            request.getItems().forEach(itemDto -> {
                Product product = productService.getProduct(itemDto.getProductId());
                OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .productNameEn(product.getNameEn())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(product.getPrice())
                    .subtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())))
                    .build();
                order.addItem(item);
            });
            
            // 5. 금액 계산
            order.calculateAmounts();
            
            // 6. 주문 저장
            Order savedOrder = orderRepository.save(order);
            
            // 7. 재고 확정 차감
            confirmStockReservations(reservations);
            
            // 8. 이벤트 발행 (Kafka - Python Analytics로 전달)
            eventPublisher.publishOrderCreated(savedOrder);
            
            // 9. 알림 발송 (비동기)
            notificationService.sendOrderConfirmation(savedOrder);
            
            return OrderCreateResponse.from(savedOrder);
            
        } catch (Exception e) {
            // 실패 시 재고 복구
            rollbackStockReservations(reservations);
            throw e;
        }
    }
    
    private void validateOrderItems(List<OrderItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException("주문 항목이 비어있습니다");
        }
        
        // 상품 존재 및 재고 확인
        items.forEach(item -> {
            Product product = productService.getProduct(item.getProductId());
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                    product.getId(), 
                    product.getStockQuantity(), 
                    item.getQuantity()
                );
            }
        });
    }
    
    private List<StockReservation> reserveStock(List<OrderItemRequest> items) {
        return items.stream()
            .map(item -> stockService.reserve(item.getProductId(), item.getQuantity()))
            .collect(Collectors.toList());
    }
    
    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequence = orderRepository.countByCreatedAtAfter(LocalDate.now().atStartOfDay());
        return String.format("ORD-%s-%04d", date, sequence + 1);
    }
}
```

---

### 4.2 주문 목록 조회

#### Endpoint
```http
GET /v1/orders
```

#### Query Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| page | Integer | 페이지 번호 |
| size | Integer | 페이지 크기 |
| status | String | 주문 상태 필터 |
| startDate | LocalDate | 시작일 |
| endDate | LocalDate | 종료일 |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "orderNumber": "ORD-20251120-0001",
        "buyerId": 3,
        "sellerId": 2,
        "total": 35.31,
        "currency": "USD",
        "status": "DELIVERED",
        "itemsCount": 2,
        "createdAt": "2025-11-20T10:00:00Z",
        "deliveredAt": "2025-11-25T15:00:00Z"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 20
    },
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

### 4.3 주문 상세 조회

#### Endpoint
```http
GET /v1/orders/{orderId}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "id": 1,
    "orderNumber": "ORD-20251120-0001",
    "buyer": {
      "id": 3,
      "name": "홍길동",
      "email": "buyer@example.com"
    },
    "seller": {
      "id": 2,
      "name": "John Kim",
      "email": "seller@example.com"
    },
    "items": [
      {
        "id": 1,
        "productId": 1,
        "productName": "배추김치 1kg",
        "quantity": 2,
        "unitPrice": 15.00,
        "subtotal": 30.00
      }
    ],
    "subtotal": 30.00,
    "shippingFee": 3.00,
    "tax": 2.31,
    "total": 35.31,
    "currency": "USD",
    "status": "DELIVERED",
    "shippingAddress": {
      "recipientName": "홍길동",
      "phone": "010-1234-5678",
      "addressLine1": "서울시 강남구 테헤란로 123",
      "zipCode": "06000"
    },
    "payment": {
      "id": 1,
      "amount": 35.31,
      "status": "COMPLETED",
      "paymentMethod": "STRIPE",
      "paidAt": "2025-11-20T10:05:00Z"
    },
    "statusHistory": [
      {
        "status": "PENDING",
        "changedAt": "2025-11-20T10:00:00Z"
      },
      {
        "status": "CONFIRMED",
        "changedAt": "2025-11-20T10:10:00Z"
      },
      {
        "status": "SHIPPED",
        "changedAt": "2025-11-21T09:00:00Z",
        "trackingNumber": "1234567890"
      },
      {
        "status": "DELIVERED",
        "changedAt": "2025-11-25T15:00:00Z"
      }
    ],
    "createdAt": "2025-11-20T10:00:00Z"
  }
}
```

---

### 4.4 주문 상태 변경 (판매자/관리자)

#### Endpoint
```http
PATCH /v1/orders/{orderId}/status
```

#### Authorization
```http
Authorization: Bearer {access_token}
X-User-Role: SELLER or ADMIN
```

#### Request Body
```json
{
  "status": "SHIPPED",
  "trackingNumber": "1234567890",
  "carrierName": "CJ대한통운",
  "note": "배송이 시작되었습니다"
}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "orderId": 1,
    "previousStatus": "CONFIRMED",
    "currentStatus": "SHIPPED",
    "trackingNumber": "1234567890",
    "updatedAt": "2025-11-21T09:00:00Z"
  },
  "message": "주문 상태가 변경되었습니다"
}
```

#### Status Transition Rules
```java
@Service
public class OrderStatusService {
    
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
        OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
        OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
        OrderStatus.SHIPPED, Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED, Set.of(OrderStatus.RETURN_REQUESTED),
        OrderStatus.RETURN_REQUESTED, Set.of(OrderStatus.RETURNED, OrderStatus.DELIVERED)
    );
    
    @Transactional
    public OrderStatusUpdateResponse updateStatus(
        Long orderId, 
        OrderStatusUpdateRequest request, 
        Long currentUserId
    ) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        
        // 권한 확인
        validatePermission(order, currentUserId);
        
        // 상태 전이 가능 여부 확인
        if (!canTransition(order.getStatus(), request.getStatus())) {
            throw new InvalidStatusTransitionException(
                order.getStatus(), 
                request.getStatus()
            );
        }
        
        OrderStatus previousStatus = order.getStatus();
        
        // 상태 변경
        order.updateStatus(
            request.getStatus(),
            request.getTrackingNumber(),
            request.getCarrierName(),
            request.getNote()
        );
        
        // 이벤트 발행
        eventPublisher.publishOrderStatusChanged(order, previousStatus);
        
        // 알림 발송
        notificationService.sendStatusChangeNotification(order);
        
        return OrderStatusUpdateResponse.from(order, previousStatus);
    }
    
    private boolean canTransition(OrderStatus from, OrderStatus to) {
        Set<OrderStatus> allowedStatuses = ALLOWED_TRANSITIONS.get(from);
        return allowedStatuses != null && allowedStatuses.contains(to);
    }
}
```

---

### 4.5 주문 취소

#### Endpoint
```http
POST /v1/orders/{orderId}/cancel
```

#### Request Body
```json
{
  "reason": "배송지 변경 필요",
  "cancelType": "BUYER_REQUEST"
}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "orderId": 1,
    "status": "CANCELLED",
    "refundAmount": 35.31,
    "refundStatus": "PENDING",
    "cancelledAt": "2025-11-20T11:00:00Z"
  },
  "message": "주문이 취소되었습니다"
}
```

---

## 5. 결제 서비스 (Payment Service)

**Base Path**: `/v1/payments`  
**Port**: 8084  
**책임**: 결제 처리, PG 연동, 환불

### 5.1 결제 시작

#### Endpoint
```http
POST /v1/payments
```

#### Request Body
```json
{
  "orderId": 1,
  "paymentMethod": "STRIPE",
  "amount": 35.31,
  "currency": "USD",
  "returnUrl": "https://spicyjump.com/payment/success",
  "cancelUrl": "https://spicyjump.com/payment/cancel"
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "success": true,
  "data": {
    "paymentId": 1,
    "orderId": 1,
    "amount": 35.31,
    "currency": "USD",
    "status": "PENDING",
    "paymentMethod": "STRIPE",
    "checkoutUrl": "https://checkout.stripe.com/pay/cs_test_...",
    "expiresAt": "2025-11-20T10:30:00Z",
    "createdAt": "2025-11-20T10:00:00Z"
  }
}
```

#### Java Service with Stripe Integration
```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final StripeClient stripeClient;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @Transactional
    public PaymentCreateResponse createPayment(PaymentCreateRequest request) {
        // 주문 확인
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException(request.getOrderId()));
        
        // 중복 결제 확인
        if (paymentRepository.existsByOrderIdAndStatusIn(
            order.getId(), 
            List.of(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
        )) {
            throw new DuplicatePaymentException(order.getId());
        }
        
        // Stripe Payment Intent 생성
        PaymentIntent intent = stripeClient.createPaymentIntent(
            order.getTotal(),
            order.getCurrency(),
            Map.of("orderId", order.getId().toString())
        );
        
        // 결제 정보 저장
        Payment payment = Payment.builder()
            .orderId(order.getId())
            .amount(order.getTotal())
            .currency(order.getCurrency())
            .paymentMethod(request.getPaymentMethod())
            .status(PaymentStatus.PENDING)
            .pgProvider("STRIPE")
            .pgTransactionId(intent.getId())
            .pgResponse(intent.toJson())
            .expiresAt(LocalDateTime.now().plusMinutes(30))
            .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        return PaymentCreateResponse.builder()
            .paymentId(savedPayment.getId())
            .orderId(order.getId())
            .checkoutUrl(intent.getCheckoutUrl())
            .expiresAt(savedPayment.getExpiresAt())
            .build();
    }
}
```

---

### 5.2 결제 Webhook (Stripe)

#### Endpoint
```http
POST /v1/payments/webhook/stripe
```

#### Request Headers
```http
Stripe-Signature: {signature}
```

#### Request Body (Stripe Event)
```json
{
  "id": "evt_1234567890",
  "type": "payment_intent.succeeded",
  "data": {
    "object": {
      "id": "pi_3ABC123",
      "amount": 3531,
      "currency": "usd",
      "status": "succeeded",
      "metadata": {
        "orderId": "1"
      }
    }
  }
}
```

#### Response (200 OK)
```json
{
  "received": true
}
```

#### Java Webhook Handler
```java
@RestController
@RequestMapping("/v1/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {
    
    private final PaymentWebhookService webhookService;
    
    @PostMapping("/stripe")
    public ResponseEntity<Map<String, Boolean>> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String signature
    ) {
        try {
            Event event = Webhook.constructEvent(
                payload,
                signature,
                webhookSecret
            );
            
            webhookService.processStripeEvent(event);
            
            return ResponseEntity.ok(Map.of("received", true));
            
        } catch (SignatureVerificationException e) {
            log.error("Invalid signature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentWebhookService {
    
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final PaymentEventPublisher eventPublisher;
    
    public void processStripeEvent(Event event) {
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentSuccess(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailure(event);
                break;
            case "charge.refunded":
                handleRefund(event);
                break;
            default:
                log.info("Unhandled event type: {}", event.getType());
        }
    }
    
    private void handlePaymentSuccess(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject()
            .orElseThrow();
        
        String transactionId = intent.getId();
        Long orderId = Long.parseLong(intent.getMetadata().get("orderId"));
        
        // 결제 상태 업데이트
        Payment payment = paymentRepository.findByPgTransactionId(transactionId)
            .orElseThrow(() -> new PaymentNotFoundException(transactionId));
        
        payment.updateStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        
        // 주문 상태 업데이트
        orderService.confirmOrder(orderId);
        
        // 이벤트 발행 (Kafka)
        eventPublisher.publishPaymentCompleted(payment);
        
        // 알림 발송
        notificationService.sendPaymentConfirmation(payment);
        
        log.info("Payment succeeded: paymentId={}, orderId={}", payment.getId(), orderId);
    }
    
    private void handlePaymentFailure(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject()
            .orElseThrow();
        
        Payment payment = paymentRepository.findByPgTransactionId(intent.getId())
            .orElseThrow(() -> new PaymentNotFoundException(intent.getId()));
        
        payment.updateStatus(PaymentStatus.FAILED);
        payment.setFailureReason(intent.getLastPaymentError().getMessage());
        
        // 알림 발송
        notificationService.sendPaymentFailure(payment);
        
        log.error("Payment failed: paymentId={}, reason={}", 
            payment.getId(), 
            payment.getFailureReason()
        );
    }
}
```

---

### 5.3 환불 처리

#### Endpoint
```http
POST /v1/payments/{paymentId}/refund
```

#### Authorization
```http
Authorization: Bearer {access_token}
X-User-Role: ADMIN or SELLER
```

#### Request Body
```json
{
  "amount": 35.31,
  "reason": "고객 요청",
  "refundType": "FULL"
}
```

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "refundId": 1,
    "paymentId": 1,
    "orderId": 1,
    "amount": 35.31,
    "currency": "USD",
    "status": "PROCESSING",
    "refundType": "FULL",
    "reason": "고객 요청",
    "createdAt": "2025-11-20T12:00:00Z",
    "estimatedCompletionDate": "2025-11-27T00:00:00Z"
  },
  "message": "환불 처리가 시작되었습니다"
}
```

---

## 6. 리뷰 서비스 (Review Service)

**Base Path**: `/v1/reviews`  
**Port**: 8085  
**책임**: 리뷰 CRUD, 평점 관리

### 6.1 리뷰 작성

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
  "productId": 1,
  "rating": 5,
  "title": "정말 맛있어요!",
  "content": "배송도 빠르고 맛도 정말 좋습니다. 재구매 의사 100%!",
  "images": [
    {
      "url": "https://s3.../review1.jpg",
      "order": 1
    }
  ]
}
```

#### Request Validation

| 필드 | 타입 | 필수 | 규칙 |
|------|------|------|------|
| productId | Long | Y | 존재하는 상품 |
| rating | Integer | Y | 1-5 |
| title | String | N | 최대 200자 |
| content | String | Y | 10-5000자 |
| images | List | N | 최대 3개 |

#### Response (201 Created)
```json
{
  "status": 201,
  "success": true,
  "data": {
    "id": 1,
    "productId": 1,
    "userId": 3,
    "userName": "홍길동",
    "rating": 5,
    "title": "정말 맛있어요!",
    "content": "배송도 빠르고 맛도 정말 좋습니다...",
    "images": [
      {
        "url": "https://s3.../review1.jpg",
        "order": 1
      }
    ],
    "isVerifiedPurchase": true,
    "status": "PUBLISHED",
    "createdAt": "2025-11-20T10:00:00Z"
  },
  "message": "리뷰가 등록되었습니다"
}
```

#### Java Service with Business Rules
```java
@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewEventPublisher eventPublisher;
    
    public ReviewCreateResponse createReview(ReviewCreateRequest request, Long userId) {
        // 1. 상품 존재 확인
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException(request.getProductId()));
        
        // 2. 중복 리뷰 확인
        if (reviewRepository.existsByProductIdAndUserId(request.getProductId(), userId)) {
            throw new DuplicateReviewException(request.getProductId(), userId);
        }
        
        // 3. 구매 인증 확인
        boolean isVerifiedPurchase = orderRepository.existsCompletedOrder(
            userId, 
            request.getProductId()
        );
        
        // 4. 리뷰 생성
        Review review = Review.builder()
            .productId(product.getId())
            .userId(userId)
            .rating(request.getRating())
            .title(request.getTitle())
            .content(request.getContent())
            .isVerifiedPurchase(isVerifiedPurchase)
            .status(ReviewStatus.PUBLISHED)
            .build();
        
        // 5. 이미지 추가
        if (request.getImages() != null) {
            request.getImages().forEach(imageDto ->
                review.addImage(imageDto.getUrl(), imageDto.getOrder())
            );
        }
        
        Review savedReview = reviewRepository.save(review);
        
        // 6. 상품 평점 업데이트 (비동기)
        updateProductRating(product.getId());
        
        // 7. 이벤트 발행 (Kafka - Python 감정 분석)
        eventPublisher.publishReviewCreated(savedReview);
        
        return ReviewCreateResponse.from(savedReview);
    }
    
    @Async
    public void updateProductRating(Long productId) {
        Double averageRating = reviewRepository.calculateAverageRating(productId);
        Long reviewCount = reviewRepository.countByProductIdAndStatus(
            productId, 
            ReviewStatus.PUBLISHED
        );
        
        productRepository.updateRating(productId, averageRating, reviewCount);
    }
}
```

---

### 6.2 리뷰 목록 조회

#### Endpoint
```http
GET /v1/reviews
```

#### Query Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| productId | Long | 상품 ID |
| page | Integer | 페이지 번호 |
| size | Integer | 페이지 크기 |
| rating | Integer | 평점 필터 (1-5) |
| sort | String | 정렬 (latest, rating, helpful) |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "user": {
          "id": 3,
          "name": "홍길동",
          "totalReviews": 5
        },
        "rating": 5,
        "title": "정말 맛있어요!",
        "content": "배송도 빠르고 맛도 정말 좋습니다...",
        "images": [
          {
            "url": "https://s3.../review1.jpg"
          }
        ],
        "isVerifiedPurchase": true,
        "helpfulCount": 10,
        "createdAt": "2025-11-20T10:00:00Z"
      }
    ],
    "pageable": {
      "page": 1,
      "size": 20
    },
    "totalElements": 50,
    "summary": {
      "averageRating": 4.5,
      "totalReviews": 50,
      "ratingDistribution": {
        "5": 30,
        "4": 15,
        "3": 3,
        "2": 1,
        "1": 1
      },
      "verifiedPurchaseCount": 45
    }
  }
}
```

---

## 7. 관리자 서비스 (Admin Service)

**Base Path**: `/v1/admin`  
**Port**: 8086  
**책임**: 관리자 대시보드, 회원/상품/주문 관리

### 7.1 대시보드 통계

#### Endpoint
```http
GET /v1/admin/dashboard/stats
```

#### Authorization
```http
Authorization: Bearer {access_token}
X-User-Role: ADMIN
```

#### Query Parameters

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| period | String | 기간 (today, week, month, year) |

#### Response (200 OK)
```json
{
  "status": 200,
  "success": true,
  "data": {
    "period": "month",
    "totalUsers": 10000,
    "newUsers": 500,
    "totalProducts": 5000,
    "newProducts": 50,
    "totalOrders": 1000,
    "newOrders": 100,
    "totalRevenue": 50000.00,
    "currency": "USD",
    "revenueGrowth": 15.5,
    "ordersByStatus": {
      "PENDING": 10,
      "CONFIRMED": 20,
      "SHIPPED": 30,
      "DELIVERED": 900,
      "CANCELLED": 40
    },
    "topProducts": [
      {
        "productId": 1,
        "productName": "배추김치 1kg",
        "salesCount": 150,
        "revenue": 2250.00
      }
    ],
    "topSellers": [
      {
        "sellerId": 2,
        "sellerName": "John Kim",
        "salesCount": 500,
        "revenue": 7500.00
      }
    ]
  }
}
```

#### Java Service with Python Analytics Integration
```java
@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PythonAnalyticsClient analyticsClient;
    
    public DashboardStatsResponse getDashboardStats(String period) {
        LocalDateTime startDate = getStartDate(period);
        
        // Java에서 기본 통계 조회
        DashboardStatsResponse stats = DashboardStatsResponse.builder()
            .period(period)
            .totalUsers(userRepository.count())
            .newUsers(userRepository.countByCreatedAtAfter(startDate))
            .totalProducts(productRepository.count())
            .newProducts(productRepository.countByCreatedAtAfter(startDate))
            .totalOrders(orderRepository.count())
            .newOrders(orderRepository.countByCreatedAtAfter(startDate))
            .totalRevenue(orderRepository.sumTotalByCreatedAtAfter(startDate))
            .build();
        
        // Python Analytics Service에서 고급 분석 데이터 가져오기
        AnalyticsDataResponse analyticsData = analyticsClient.getAdvancedAnalytics(period);
        
        stats.setRevenueGrowth(analyticsData.getRevenueGrowth());
        stats.setTopProducts(analyticsData.getTopProducts());
        stats.setTopSellers(analyticsData.getTopSellers());
        stats.setPredictedRevenue(analyticsData.getPredictedRevenue());
        
        return stats;
    }
}
```

---

## 8. 블록체인 서비스 (Blockchain Service)

**Base Path**: `/v1/blockchain`  
**Port**: 8087  
**책임**: 블록체인 연동, 원산지 추적, 거래 이력

### 8.1 거래 이력 기록

#### Endpoint
```http
POST /v1/blockchain/transactions
```

#### Request Body
```json
{
  "orderId": 1,
  "productId": 1,
  "buyerId": 3,
  "sellerId": 2,
  "amount": 35.31,
  "currency": "USD",
  "timestamp": "2025-11-20T10:00:00Z"
}
```

#### Response (201 Created)
```json
{
  "status": 201,
  "success": true,
  "data": {
    "transactionId": 1,
    "blockchainHash": "0x1234567890abcdef...",
    "blockNumber": 12345,
    "status": "CONFIRMED",
    "gasUsed": "21000",
    "transactionFee": "0.001",
    "createdAt": "2025-11-20T10:00:05Z"
  }
}
```

#### Java Service with Web3j
```java
@Service
@RequiredArgsConstructor
public class BlockchainService {
    
    private final Web3j web3j;
    private final Credentials credentials;
    private final TransactionRepository transactionRepository;
    
    @Value("${blockchain.contract.address}")
    private String contractAddress;
    
    @Async
    public CompletableFuture<BlockchainTransactionResponse> recordTransaction(
        BlockchainTransactionRequest request
    ) {
        try {
            // 스마트 컨트랙트 로드
            TransactionContract contract = TransactionContract.load(
                contractAddress,
                web3j,
                credentials,
                new DefaultGasProvider()
            );
            
            // 트랜잭션 전송
            TransactionReceipt receipt = contract.recordTransaction(
                BigInteger.valueOf(request.getOrderId()),
                BigInteger.valueOf(request.getProductId()),
                request.getBuyerId().toString(),
                request.getSellerId().toString(),
                toWei(request.getAmount()),
                request.getTimestamp().toEpochSecond(ZoneOffset.UTC)
            ).send();
            
            // DB에 저장
            BlockchainTransaction transaction = BlockchainTransaction.builder()
                .orderId(request.getOrderId())
                .blockchainHash(receipt.getTransactionHash())
                .blockNumber(receipt.getBlockNumber().longValue())
                .status(BlockchainStatus.CONFIRMED)
                .gasUsed(receipt.getGasUsed().toString())
                .build();
            
            transactionRepository.save(transaction);
            
            return CompletableFuture.completedFuture(
                BlockchainTransactionResponse.from(transaction)
            );
            
        } catch (Exception e) {
            log.error("Blockchain transaction failed: {}", e.getMessage());
            throw new BlockchainException("블록체인 거래 기록 실패", e);
        }
    }
    
    private BigInteger toWei(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(1e18)).toBigInteger();
    }
}
```

---

## 9. 공통 응답 구조

### 9.1 ApiResponse Wrapper

```java
@Getter
@Builder
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private Integer status;
    private Boolean success;
    private T data;
    private String message;
    private String path;
    private String requestId;
    
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.OK.value())
            .success(true)
            .data(data)
            .message(message)
            .build();
    }
    
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .success(false)
            .message(message)
            .build();
    }
}
```

---

## 10. 예외 처리

### 10.1 Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
        BusinessException e,
        HttpServletRequest request
    ) {
        log.error("Business exception: {}", e.getMessage());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(e.getStatus().value())
            .success(false)
            .error(ErrorDetail.builder()
                .code(e.getErrorCode())
                .message(e.getMessage())
                .build())
            .path(request.getRequestURI())
            .requestId(request.getHeader("X-Request-ID"))
            .build();
        
        return ResponseEntity.status(e.getStatus()).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
        MethodArgumentNotValidException e,
        HttpServletRequest request
    ) {
        List<FieldErrorDetail> details = e.getBindingResult().getFieldErrors().stream()
            .map(error -> FieldErrorDetail.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build())
            .collect(Collectors.toList());
        
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
            .success(false)
            .error(ErrorDetail.builder()
                .code("VALIDATION_ERROR")
                .message("입력 값이 유효하지 않습니다")
                .details(details)
                .build())
            .path(request.getRequestURI())
            .build();
        
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }
}
```

---

## 11. Rate Limiting

### 11.1 Bucket4j + Redis

```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public ProxyManager<String> proxyManager(RedisTemplate<String, byte[]> redisTemplate) {
        return new ProxyManager<>(new RedissonBasedProxyManager<>(
            redisTemplate.getConnectionFactory()
        ));
    }
}

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final ProxyManager<String> proxyManager;
    
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        
        String userKey = getUserKey(request);
        Bucket bucket = getBucket(userKey);
        
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        
        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining", 
                String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
        }
    }
    
    private Bucket getBucket(String key) {
        return proxyManager.getProxy(key, () -> 
            Bucket4j.builder()
                .addLimit(Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(15))))
                .build()
        );
    }
}
```

---

## 부록

### A. OpenAPI (Swagger) 설정

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SpicyJump API")
                .version("1.0")
                .description("K-Food 거래 플랫폼 API 문서")
                .contact(new Contact()
                    .name("SpicyJump Team")
                    .email("support@spicyjump.com"))
            )
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                )
            );
    }
}
```

---

**문서 작성**: AI Assistant  
**최종 업데이트**: 2025-11-20  
**다음 단계**: Python API 상세 명세서 작성

