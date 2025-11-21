# 블록체인 & 결제 기능 현황 정리

## 📋 문서 목적

개발규정에 명시된 **블록체인 기술**과 **결제 기능**의 현재 설계/구현 상태를 정리하고, 추가 구현이 필요한 사항을 명확히 합니다.

**작성일:** 2025-11-20  
**검토 시점:** 백엔드 구현 완료 후

---

## 1. 개발규정 요구사항 (재확인)

### 1.1 블록체인 기술

**목적:**
- 사용자 간 거래 투명성 확보
- 원산지 추적 시스템
- 결제 이력 관리 (불변성)

**요구사항:**
```
✓ 거래 원장 (Transaction Ledger)
✓ 스마트 컨트랙트 (Smart Contract)
✓ 원산지 추적 (Origin Tracking)
✓ 거래 투명성 (Transparency)
```

### 1.2 결제 기능

**국내 PG:**
- 토스페이먼츠 (Toss Payments)
- 나이스페이 (NICE Payments)

**해외 PG:**
- Stripe

**요구사항:**
```
✓ 다양한 결제 수단 지원
✓ 안전한 결제 처리
✓ 결제 이력 관리
✓ 환불 처리
```

---

## 2. 현재 구현 상태

### 2.1 블록체인 - 설계만 완료 ❌ (구현 안 됨)

#### ✅ 설계 문서에 포함된 내용

**1. 기술 스택 정의 (`09_java_spring_boot_techstack_defin.md`)**

```yaml
Blockchain Library:
  - Web3j: 4.10.x
  - 이유: Java 생태계 표준, Ethereum 호환

Smart Contract:
  - Solidity: 0.8.x
  - 용도: 거래 이력, 원산지 추적, 리워드 토큰

Network:
  - Phase 1: Ethereum Testnet (Sepolia)
  - Phase 2: Polygon (낮은 가스비)
  - Phase 3: Private Blockchain (Hyperledger Besu)
```

**2. 아키텍처 설계 (`10_hybrid_architecture_design.md`)**

```
Java Spring Boot (메인 엔진)
  - 블록체인 연동
  
Data Layer
  - Blockchain Ledger (거래 원장)
```

**3. 로드맵 (`02_based_java_pased_dev_roadmap.md`)**

```
Phase 3 (고도화):
  추가: Blockchain (Web3j + Ethereum)
```

#### ❌ 구현되지 않은 것

1. **xlcfi-blockchain-service** 모듈 - 없음
2. **BlockchainService** - 없음
3. **Smart Contract** - 없음
4. **Web3j 의존성** - 없음
5. **블록체인 Entity/Repository** - 없음
6. **블록체인 API 엔드포인트** - 없음

#### 📊 구현 상태: 0% (설계만 존재)

---

### 2.2 결제 기능 - 부분 구현 ⚠️ (기본 구조만)

#### ✅ 구현된 내용

**1. Payment Service 모듈**
```
xlcfi-payment-service/
├── domain/
│   ├── Payment.java ✅
│   ├── PaymentMethod.java ✅
│   └── PaymentStatus.java ✅
├── dto/
│   ├── CreatePaymentRequest.java ✅
│   └── PaymentResponse.java ✅
├── repository/
│   └── PaymentRepository.java ✅
├── service/
│   └── PaymentService.java ✅ (기본 CRUD만)
└── controller/
    └── PaymentController.java ✅ (기본 엔드포인트만)
```

**2. 데이터베이스 스키마**
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    pg_provider VARCHAR(50),           -- ✅ 있음
    pg_transaction_id VARCHAR(255),    -- ✅ 있음
    pg_response JSONB,                 -- ✅ 있음
    created_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP
);
```

**3. PaymentMethod Enum**
```java
public enum PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER,
    PAYPAL,
    KAKAO_PAY
}
```

**4. 기본 API 엔드포인트**
```
POST   /api/payments              - 결제 생성 ✅
POST   /api/payments/{id}/process - 결제 처리 ✅ (로직 없음)
GET    /api/payments/{id}         - 결제 조회 ✅
GET    /api/payments/user         - 내 결제 목록 ✅
POST   /api/payments/{id}/refund  - 환불 처리 ✅ (로직 없음)
```

#### ❌ 구현되지 않은 것 (중요!)

**1. PG사 연동 로직**
```java
// 현재 PaymentService.processPayment()
@Transactional
public PaymentResponse processPayment(Long paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException("PAYMENT003", "결제를 찾을 수 없습니다"));

    // TODO: 실제 PG사 API 호출 로직 구현 필요 ❌
    payment.setStatus(PaymentStatus.COMPLETED);
    payment.setPaidAt(LocalDateTime.now());

    Payment updatedPayment = paymentRepository.save(payment);
    return PaymentResponse.from(updatedPayment);
}
```

**2. PG사별 클라이언트**
- ❌ TossPaymentsClient - 없음
- ❌ NicePayClient - 없음
- ❌ StripeClient - 없음

**3. PG사 SDK 의존성**
```gradle
// build.gradle.kts에 없음
dependencies {
    // ❌ 토스페이먼츠 SDK
    // ❌ 나이스페이 SDK
    // ❌ Stripe Java SDK
}
```

**4. Webhook 처리**
- ❌ 토스페이먼츠 Webhook 엔드포인트
- ❌ 나이스페이 Webhook 엔드포인트
- ❌ Stripe Webhook 엔드포인트

**5. 결제 검증 로직**
- ❌ 결제 금액 검증
- ❌ 중복 결제 방지
- ❌ 결제 타임아웃 처리

**6. 환불 로직**
```java
// 현재 refundPayment()
@Transactional
public PaymentResponse refundPayment(Long paymentId, String reason) {
    Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new BusinessException("PAYMENT003", "결제를 찾을 수 없습니다"));

    // TODO: 실제 PG사 환불 API 호출 필요 ❌
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setRefundedAt(LocalDateTime.now());

    Payment refundedPayment = paymentRepository.save(payment);
    return PaymentResponse.from(refundedPayment);
}
```

**7. 결제 상태 관리**
- ❌ 결제 만료 처리 (타임아웃)
- ❌ 결제 실패 재시도
- ❌ 부분 환불 처리

#### 📊 구현 상태: 30% (기본 구조만, 핵심 로직 없음)

---

## 3. 상세 비교표

### 3.1 블록체인 기능

| 항목 | 설계 | 구현 | 상태 |
|------|------|------|------|
| 모듈 구조 | ✅ 언급됨 | ❌ 없음 | 미구현 |
| Web3j 의존성 | ✅ 정의됨 | ❌ 없음 | 미구현 |
| Smart Contract | ✅ 계획됨 | ❌ 없음 | 미구현 |
| 거래 원장 | ✅ 설계됨 | ❌ 없음 | 미구현 |
| 원산지 추적 | ✅ 요구됨 | ❌ 없음 | 미구현 |
| API 엔드포인트 | ❌ 없음 | ❌ 없음 | 미구현 |
| 테스트 | ❌ 없음 | ❌ 없음 | 미구현 |

**결론:** 블록체인은 **Phase 3 (고도화)** 단계로 계획되어 있으며, 현재는 구현되지 않음.

### 3.2 결제 기능

| 항목 | 설계 | 구현 | 상태 |
|------|------|------|------|
| Payment Entity | ✅ | ✅ | 완료 |
| Payment Repository | ✅ | ✅ | 완료 |
| Payment Service | ✅ | ⚠️ | 부분 (기본 CRUD만) |
| Payment Controller | ✅ | ⚠️ | 부분 (엔드포인트만) |
| 토스페이먼츠 연동 | ✅ 계획됨 | ❌ | 미구현 |
| 나이스페이 연동 | ✅ 계획됨 | ❌ | 미구현 |
| Stripe 연동 | ✅ 계획됨 | ❌ | 미구현 |
| PG사 SDK | ✅ 정의됨 | ❌ | 미구현 |
| Webhook 처리 | ✅ 설계됨 | ❌ | 미구현 |
| 결제 검증 | ✅ 필요 | ❌ | 미구현 |
| 환불 로직 | ✅ 계획됨 | ❌ | 미구현 |
| 결제 상태 관리 | ✅ 필요 | ⚠️ | 부분 (Enum만) |

**결론:** 결제는 **기본 구조만 구현**되었으며, 실제 PG사 연동 로직은 없음.

---

## 4. 추가 구현 필요 사항

### 4.1 블록체인 (Phase 3 - 향후)

#### 우선순위: 낮음 (Phase 3)

**1. 모듈 생성**
```
backend/java-services/xlcfi-blockchain-service/
├── domain/
│   ├── BlockchainTransaction.java
│   ├── OriginTracking.java
│   └── SmartContractEvent.java
├── service/
│   ├── BlockchainService.java
│   ├── Web3Service.java
│   └── SmartContractService.java
├── controller/
│   └── BlockchainController.java
└── config/
    └── Web3Config.java
```

**2. 의존성 추가**
```gradle
dependencies {
    // Web3j
    implementation("org.web3j:core:4.10.3")
    implementation("org.web3j:contracts:4.10.3")
    
    // Ethereum
    implementation("org.ethereum:ethereumj-core:1.15.0")
}
```

**3. Smart Contract 개발**
```solidity
// contracts/OriginTracking.sol
pragma solidity ^0.8.0;

contract OriginTracking {
    struct Product {
        uint256 productId;
        string originCountry;
        string foodCode;
        bool haccpCertified;
        uint256 timestamp;
    }
    
    mapping(uint256 => Product) public products;
    
    function registerProduct(
        uint256 _productId,
        string memory _originCountry,
        string memory _foodCode,
        bool _haccpCertified
    ) public {
        products[_productId] = Product({
            productId: _productId,
            originCountry: _originCountry,
            foodCode: _foodCode,
            haccpCertified: _haccpCertified,
            timestamp: block.timestamp
        });
    }
    
    function getProduct(uint256 _productId) 
        public view returns (Product memory) {
        return products[_productId];
    }
}
```

**4. API 엔드포인트**
```
POST   /api/blockchain/products/{id}/register  - 상품 블록체인 등록
GET    /api/blockchain/products/{id}/trace     - 원산지 추적
POST   /api/blockchain/transactions            - 거래 기록
GET    /api/blockchain/transactions/{id}       - 거래 조회
```

**5. 구현 예상 시간**
- Smart Contract 개발: 2주
- Backend 연동: 2주
- 테스트 및 배포: 1주
- **총 5주 (1.25개월)**

---

### 4.2 결제 기능 (Phase 1 - 긴급)

#### 우선순위: 높음 (MVP 필수)

**1. 토스페이먼츠 연동**

**의존성 추가:**
```gradle
// xlcfi-payment-service/build.gradle.kts
dependencies {
    // 토스페이먼츠 SDK
    implementation("com.tosspayments:payment-sdk-server:1.0.0")
    // 또는 RestTemplate/WebClient 사용
}
```

**TossPaymentsClient 구현:**
```java
@Service
@RequiredArgsConstructor
public class TossPaymentsClient {
    
    @Value("${tosspayments.secret-key}")
    private String secretKey;
    
    @Value("${tosspayments.api-url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    
    /**
     * 결제 승인
     */
    public TossPaymentResponse confirmPayment(
            String paymentKey, 
            String orderId, 
            BigDecimal amount) {
        
        String url = apiUrl + "/v1/payments/confirm";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, "");
        
        Map<String, Object> body = Map.of(
            "paymentKey", paymentKey,
            "orderId", orderId,
            "amount", amount
        );
        
        HttpEntity<Map<String, Object>> request = 
            new HttpEntity<>(body, headers);
        
        ResponseEntity<TossPaymentResponse> response = 
            restTemplate.postForEntity(url, request, TossPaymentResponse.class);
        
        return response.getBody();
    }
    
    /**
     * 결제 취소 (환불)
     */
    public TossRefundResponse cancelPayment(
            String paymentKey, 
            String cancelReason) {
        
        String url = apiUrl + "/v1/payments/" + paymentKey + "/cancel";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, "");
        
        Map<String, Object> body = Map.of(
            "cancelReason", cancelReason
        );
        
        HttpEntity<Map<String, Object>> request = 
            new HttpEntity<>(body, headers);
        
        ResponseEntity<TossRefundResponse> response = 
            restTemplate.postForEntity(url, request, TossRefundResponse.class);
        
        return response.getBody();
    }
}
```

**Webhook 처리:**
```java
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(
            @RequestBody TossWebhookRequest request) {
        
        // 서명 검증
        if (!verifySignature(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        // 결제 상태 업데이트
        switch (request.getEventType()) {
            case "PAYMENT_CONFIRMED":
                paymentService.confirmPayment(request.getPaymentKey());
                break;
            case "PAYMENT_CANCELLED":
                paymentService.cancelPayment(request.getPaymentKey());
                break;
        }
        
        return ResponseEntity.ok().build();
    }
    
    private boolean verifySignature(TossWebhookRequest request) {
        // 토스페이먼츠 서명 검증 로직
        return true;
    }
}
```

**2. 나이스페이 연동**

```java
@Service
@RequiredArgsConstructor
public class NicePayClient {
    
    @Value("${nicepay.merchant-key}")
    private String merchantKey;
    
    @Value("${nicepay.api-url}")
    private String apiUrl;
    
    private final RestTemplate restTemplate;
    
    public NicePayResponse processPayment(NicePayRequest request) {
        // 나이스페이 API 호출 로직
        // ...
    }
    
    public NicePayRefundResponse refundPayment(String tid, BigDecimal amount) {
        // 나이스페이 환불 로직
        // ...
    }
}
```

**3. Stripe 연동**

**의존성 추가:**
```gradle
dependencies {
    // Stripe Java SDK
    implementation("com.stripe:stripe-java:24.0.0")
}
```

**StripeClient 구현:**
```java
@Service
@RequiredArgsConstructor
public class StripeClient {
    
    @Value("${stripe.secret-key}")
    private String secretKey;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }
    
    /**
     * Payment Intent 생성
     */
    public PaymentIntent createPaymentIntent(
            BigDecimal amount, 
            String currency) throws StripeException {
        
        PaymentIntentCreateParams params = 
            PaymentIntentCreateParams.builder()
                .setAmount(amount.multiply(BigDecimal.valueOf(100)).longValue())
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                        .setEnabled(true)
                        .build()
                )
                .build();
        
        return PaymentIntent.create(params);
    }
    
    /**
     * 결제 확인
     */
    public PaymentIntent retrievePaymentIntent(String paymentIntentId) 
            throws StripeException {
        return PaymentIntent.retrieve(paymentIntentId);
    }
    
    /**
     * 환불 처리
     */
    public Refund createRefund(String paymentIntentId) 
            throws StripeException {
        
        RefundCreateParams params = 
            RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId)
                .build();
        
        return Refund.create(params);
    }
}
```

**Stripe Webhook:**
```java
@PostMapping("/webhook/stripe")
public ResponseEntity<Void> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader) {
    
    Event event;
    
    try {
        event = Webhook.constructEvent(
            payload, sigHeader, webhookSecret
        );
    } catch (SignatureVerificationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    // 이벤트 타입별 처리
    switch (event.getType()) {
        case "payment_intent.succeeded":
            PaymentIntent paymentIntent = 
                (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElse(null);
            paymentService.handlePaymentSuccess(paymentIntent);
            break;
        case "payment_intent.payment_failed":
            // 결제 실패 처리
            break;
    }
    
    return ResponseEntity.ok().build();
}
```

**4. PaymentService 업데이트**

```java
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentsClient tossPaymentsClient;
    private final NicePayClient nicePayClient;
    private final StripeClient stripeClient;
    
    /**
     * 결제 생성 (PG사별 분기)
     */
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        // 주문 조회
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new BusinessException("PAYMENT001", "주문을 찾을 수 없습니다"));
        
        // 결제 금액 검증
        if (request.getAmount().compareTo(order.getTotal()) != 0) {
            throw new BusinessException("PAYMENT002", "결제 금액이 주문 금액과 일치하지 않습니다");
        }
        
        // 중복 결제 확인
        if (paymentRepository.existsByOrderIdAndStatusIn(
                order.getId(), 
                List.of(PaymentStatus.PENDING, PaymentStatus.COMPLETED))) {
            throw new BusinessException("PAYMENT003", "이미 결제가 진행 중이거나 완료되었습니다");
        }
        
        // PG사별 결제 생성
        String pgTransactionId;
        Map<String, Object> pgResponse;
        
        switch (request.getPgProvider()) {
            case "TOSS":
                // 토스페이먼츠 결제 생성
                TossPaymentResponse tossResponse = 
                    tossPaymentsClient.createPayment(request);
                pgTransactionId = tossResponse.getPaymentKey();
                pgResponse = tossResponse.toMap();
                break;
                
            case "NICEPAY":
                // 나이스페이 결제 생성
                NicePayResponse niceResponse = 
                    nicePayClient.createPayment(request);
                pgTransactionId = niceResponse.getTid();
                pgResponse = niceResponse.toMap();
                break;
                
            case "STRIPE":
                // Stripe Payment Intent 생성
                PaymentIntent intent = 
                    stripeClient.createPaymentIntent(
                        request.getAmount(), 
                        request.getCurrency()
                    );
                pgTransactionId = intent.getId();
                pgResponse = Map.of(
                    "clientSecret", intent.getClientSecret(),
                    "status", intent.getStatus()
                );
                break;
                
            default:
                throw new BusinessException("PAYMENT004", "지원하지 않는 PG사입니다");
        }
        
        // 결제 정보 저장
        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .pgProvider(request.getPgProvider())
                .pgTransactionId(pgTransactionId)
                .pgResponse(pgResponse)
                .build();
        
        Payment savedPayment = paymentRepository.save(payment);
        
        return PaymentResponse.from(savedPayment);
    }
    
    /**
     * 결제 승인 (Webhook에서 호출)
     */
    @Transactional
    public void confirmPayment(String pgTransactionId) {
        Payment payment = paymentRepository.findByPgTransactionId(pgTransactionId)
                .orElseThrow(() -> new BusinessException("PAYMENT005", "결제를 찾을 수 없습니다"));
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        
        paymentRepository.save(payment);
        
        // 주문 상태 업데이트
        Order order = payment.getOrder();
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }
    
    /**
     * 환불 처리
     */
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT006", "결제를 찾을 수 없습니다"));
        
        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException("PAYMENT007", "완료된 결제만 환불할 수 있습니다");
        }
        
        // PG사별 환불 처리
        switch (payment.getPgProvider()) {
            case "TOSS":
                tossPaymentsClient.cancelPayment(
                    payment.getPgTransactionId(), 
                    reason
                );
                break;
            case "NICEPAY":
                nicePayClient.refundPayment(
                    payment.getPgTransactionId(), 
                    payment.getAmount()
                );
                break;
            case "STRIPE":
                stripeClient.createRefund(payment.getPgTransactionId());
                break;
        }
        
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        
        Payment refundedPayment = paymentRepository.save(payment);
        
        return PaymentResponse.from(refundedPayment);
    }
}
```

**5. 환경 변수 설정**

```yaml
# application.yml
payment:
  toss:
    secret-key: ${TOSS_SECRET_KEY}
    api-url: https://api.tosspayments.com
    
  nicepay:
    merchant-key: ${NICEPAY_MERCHANT_KEY}
    api-url: https://api.nicepay.co.kr
    
  stripe:
    secret-key: ${STRIPE_SECRET_KEY}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET}
```

**6. 구현 예상 시간**
- 토스페이먼츠 연동: 1주
- 나이스페이 연동: 1주
- Stripe 연동: 1주
- Webhook 처리: 3일
- 테스트 및 검증: 1주
- **총 4.5주 (약 1개월)**

---

## 5. 우선순위 및 로드맵

### Phase 1 (MVP) - 현재 단계
```
✅ 기본 백엔드 구조
✅ 인증/권한
✅ 상품/주문 관리
⚠️ 결제 기능 (기본 구조만)
❌ 실제 PG 연동 (긴급 필요)
```

### Phase 2 (확장)
```
- Python 마이크로서비스 (분석/추천)
- Elasticsearch 검색
- Kafka 이벤트 스트리밍
- 고급 결제 기능 (정기결제, 에스크로)
```

### Phase 3 (고도화)
```
- 블록체인 통합
- 스마트 컨트랙트
- 원산지 추적
- 글로벌 확장
```

---

## 6. 권장 구현 순서

### 즉시 구현 필요 (Phase 1 완성)

1. **토스페이먼츠 연동** (1주)
   - 국내 주요 PG
   - 간편결제 지원
   - 우선순위: 최고

2. **Stripe 연동** (1주)
   - 해외 결제
   - 글로벌 확장 대비
   - 우선순위: 높음

3. **나이스페이 연동** (1주)
   - 국내 보조 PG
   - 우선순위: 중간

4. **Webhook 처리** (3일)
   - 결제 상태 동기화
   - 우선순위: 높음

5. **결제 검증 로직** (3일)
   - 보안 강화
   - 우선순위: 높음

### 향후 구현 (Phase 3)

6. **블록체인 모듈** (5주)
   - Smart Contract 개발
   - Web3j 연동
   - 우선순위: 낮음 (Phase 3)

---

## 7. 예상 비용

### 개발 비용
- 결제 연동: 4.5주 × 개발자 1명
- 블록체인: 5주 × 개발자 1명

### 운영 비용
- 토스페이먼츠: 수수료 2.5~3.5%
- 나이스페이: 수수료 2.5~3.5%
- Stripe: 수수료 2.9% + $0.30
- Ethereum Gas Fee: 거래당 $1~10 (네트워크 상황에 따라)
- Polygon Gas Fee: 거래당 $0.01~0.1 (저렴)

---

## 8. 결론 및 제안

### 현재 상태 요약

| 기능 | 설계 | 구현 | 우선순위 | 예상 시간 |
|------|------|------|----------|-----------|
| 블록체인 | ✅ | ❌ | 낮음 (Phase 3) | 5주 |
| 결제 (기본) | ✅ | ✅ | - | 완료 |
| 토스페이먼츠 | ✅ | ❌ | 최고 | 1주 |
| 나이스페이 | ✅ | ❌ | 중간 | 1주 |
| Stripe | ✅ | ❌ | 높음 | 1주 |
| Webhook | ✅ | ❌ | 높음 | 3일 |

### 제안사항

**1. 즉시 구현 (Phase 1 완성)**
```
✓ 토스페이먼츠 연동 (1주)
✓ Stripe 연동 (1주)
✓ Webhook 처리 (3일)
✓ 결제 검증 로직 (3일)

총 3주 소요
```

**2. 선택적 구현 (Phase 1 보완)**
```
✓ 나이스페이 연동 (1주)
✓ 정기결제 (1주)
✓ 에스크로 (1주)

총 3주 소요
```

**3. 향후 구현 (Phase 3)**
```
✓ 블록체인 모듈 (5주)
✓ Smart Contract (포함)
✓ 원산지 추적 (포함)
```

### 최종 권장사항

**지금 당장:**
1. 토스페이먼츠 연동 구현 (MVP 필수)
2. Stripe 연동 구현 (글로벌 대비)
3. Webhook 처리 구현 (안정성)

**나중에:**
4. 블록체인은 Phase 3에서 구현 (서비스 안정화 후)

---

## 9. 다음 단계

### 결제 기능 구현 시작

1. **Issue 생성**
   - [ ] 토스페이먼츠 연동
   - [ ] Stripe 연동
   - [ ] Webhook 처리
   - [ ] 결제 검증 로직

2. **문서 작성**
   - [ ] 결제 연동 가이드
   - [ ] PG사별 설정 방법
   - [ ] Webhook 테스트 방법

3. **테스트 환경 구축**
   - [ ] 토스페이먼츠 테스트 계정
   - [ ] Stripe 테스트 계정
   - [ ] Webhook 로컬 테스트 (ngrok)

---

**작성일:** 2025-11-20  
**작성자:** AI Assistant  
**다음 검토:** Phase 1 완료 후

