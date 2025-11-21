# 하이브리드 MSA 아키텍처 설계서

**문서 버전**: v1.0  
**작성일**: 2025-11-20  
**프로젝트**: 식품 거래 플랫폼 (XLCfi)

---

## 1. 개요

Java Spring Boot(핵심 거래 플랫폼) + Python(AI/데이터 분석)의 하이브리드 마이크로서비스 아키텍처를 정의합니다.

### 1.1 아키텍처 원칙

- **단일 책임**: 각 서비스는 하나의 비즈니스 도메인 담당
- **느슨한 결합**: 서비스 간 독립적 배포 및 확장
- **API 우선**: RESTful API 기반 통신
- **데이터 주권**: 각 서비스는 자체 데이터베이스 소유
- **장애 격리**: 한 서비스 장애가 전체 시스템에 영향 최소화

---

## 2. 전체 시스템 아키텍처

### 2.1 High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Web App    │  │  Mobile App  │  │  Admin Web   │          │
│  │  (React.js)  │  │ (React Native)│ │  (React.js)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                         CDN Layer                                │
│                    (AWS CloudFront)                              │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway Layer                           │
│              (Spring Cloud Gateway - Port 8080)                  │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │  • 라우팅 및 로드밸런싱                                  │   │
│  │  • 인증/인가 (JWT 검증)                                  │   │
│  │  │  • Rate Limiting                                      │   │
│  │  • CORS 처리                                             │   │
│  │  • 요청/응답 로깅                                        │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    Core Services (Java Spring Boot)              │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Auth Service │  │Member Service│  │Product Service│          │
│  │   :8081      │  │    :8082     │  │    :8083     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Order Service│  │Payment Service│ │Blockchain Svc│          │
│  │   :8084      │  │    :8085     │  │    :8086     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │Review Service│  │ Admin Service│                             │
│  │   :8087      │  │    :8088     │                             │
│  └──────────────┘  └──────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                AI/Analytics Services (Python)                    │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ AI Analysis  │  │Market Insight│  │Image Process │          │
│  │   :9001      │  │    :9002     │  │    :9003     │          │
│  │  (FastAPI)   │  │  (FastAPI)   │  │  (FastAPI)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐                             │
│  │Recommendation│  │ Data ETL     │                             │
│  │   :9004      │  │    :9005     │                             │
│  │  (FastAPI)   │  │  (FastAPI)   │                             │
│  └──────────────┘  └──────────────┘                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       Data Layer                                 │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  PostgreSQL  │  │    Redis     │  │Elasticsearch │          │
│  │  (Primary)   │  │   (Cache)    │  │   (Search)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   MongoDB    │  │    Kafka     │  │   AWS S3     │          │
│  │  (Document)  │  │  (Message)   │  │   (Object)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    External Services                             │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ Payment PG   │  │  Blockchain  │  │  Public API  │          │
│  │(Stripe/Toss) │  │  (Ethereum)  │  │(MFDS/HACCP)  │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. API Gateway 상세 설계

### 3.1 Spring Cloud Gateway 구성

```yaml
# API Gateway application.yml
spring:
  cloud:
    gateway:
      routes:
        # 인증 서비스
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/v1/auth/**
          filters:
            - RewritePath=/api/v1/auth/(?<segment>.*), /${segment}
        
        # 회원 서비스
        - id: member-service
          uri: lb://MEMBER-SERVICE
          predicates:
            - Path=/api/v1/members/**
          filters:
            - AuthenticationFilter
            - RewritePath=/api/v1/members/(?<segment>.*), /${segment}
        
        # 상품 서비스
        - id: product-service
          uri: lb://PRODUCT-SERVICE
          predicates:
            - Path=/api/v1/products/**
          filters:
            - AuthenticationFilter
            - RateLimitFilter
        
        # 주문 서비스
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/v1/orders/**
          filters:
            - AuthenticationFilter
        
        # 결제 서비스
        - id: payment-service
          uri: lb://PAYMENT-SERVICE
          predicates:
            - Path=/api/v1/payments/**
          filters:
            - AuthenticationFilter
            - CircuitBreakerFilter
        
        # 블록체인 서비스
        - id: blockchain-service
          uri: lb://BLOCKCHAIN-SERVICE
          predicates:
            - Path=/api/v1/blockchain/**
          filters:
            - AuthenticationFilter
        
        # 평가 서비스
        - id: review-service
          uri: lb://REVIEW-SERVICE
          predicates:
            - Path=/api/v1/reviews/**
          filters:
            - AuthenticationFilter
        
        # 관리자 서비스
        - id: admin-service
          uri: lb://ADMIN-SERVICE
          predicates:
            - Path=/api/v1/admin/**
          filters:
            - AuthenticationFilter
            - AdminAuthorizationFilter
        
        # AI 분석 서비스 (Python)
        - id: ai-service
          uri: http://ai-analysis-service:9001
          predicates:
            - Path=/api/v1/ai/**
          filters:
            - AuthenticationFilter
        
        # 시장 인사이트 서비스 (Python)
        - id: market-service
          uri: http://market-insight-service:9002
          predicates:
            - Path=/api/v1/market/**
          filters:
            - AuthenticationFilter
      
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
        - AddResponseHeader=X-Response-Time, ${responseTime}
      
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "https://xlcfi.com"
              - "https://admin.xlcfi.com"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600
```

### 3.2 커스텀 필터 구현

```java
// AuthenticationFilter.java
@Component
public class AuthenticationFilter implements GatewayFilter {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // JWT 토큰 추출
        String token = extractToken(request);
        
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        
        // 사용자 정보를 헤더에 추가
        String userId = jwtTokenProvider.getUserId(token);
        String roles = jwtTokenProvider.getRoles(token);
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", userId)
            .header("X-User-Roles", roles)
            .build();
        
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

```java
// RateLimitFilter.java
@Component
public class RateLimitFilter implements GatewayFilter {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final int MAX_REQUESTS = 100; // 분당 최대 요청 수
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String key = "rate_limit:" + userId;
        
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
        }
        
        if (count > MAX_REQUESTS) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }
        
        return chain.filter(exchange);
    }
}
```

---

## 4. 서비스 간 통신

### 4.1 동기 통신 (REST API)

```java
// Spring Cloud OpenFeign 사용
@FeignClient(name = "member-service", fallback = MemberServiceFallback.class)
public interface MemberServiceClient {
    
    @GetMapping("/members/{memberId}")
    MemberResponse getMember(@PathVariable("memberId") Long memberId);
    
    @GetMapping("/members/{memberId}/points")
    PointResponse getPoints(@PathVariable("memberId") Long memberId);
}

// Fallback 구현 (Circuit Breaker)
@Component
public class MemberServiceFallback implements MemberServiceClient {
    
    @Override
    public MemberResponse getMember(Long memberId) {
        return MemberResponse.builder()
            .id(memberId)
            .name("Unknown")
            .build();
    }
    
    @Override
    public PointResponse getPoints(Long memberId) {
        return PointResponse.builder()
            .memberId(memberId)
            .points(0)
            .build();
    }
}
```

### 4.2 비동기 통신 (Kafka)

```java
// 이벤트 발행 (Order Service)
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public void createOrder(OrderCreateRequest request) {
        // 주문 생성 로직
        Order order = orderRepository.save(newOrder);
        
        // 주문 생성 이벤트 발행
        OrderEvent event = OrderEvent.builder()
            .orderId(order.getId())
            .memberId(order.getMemberId())
            .totalAmount(order.getTotalAmount())
            .eventType(EventType.ORDER_CREATED)
            .timestamp(LocalDateTime.now())
            .build();
        
        kafkaTemplate.send("order-events", event);
    }
}

// 이벤트 구독 (Payment Service)
@Service
@Slf4j
public class PaymentEventListener {
    
    @KafkaListener(topics = "order-events", groupId = "payment-service")
    public void handleOrderEvent(OrderEvent event) {
        if (event.getEventType() == EventType.ORDER_CREATED) {
            log.info("주문 생성 이벤트 수신: {}", event.getOrderId());
            // 결제 준비 로직
            paymentService.preparePayment(event);
        }
    }
}
```

### 4.3 Java ↔ Python 통신

```java
// Java에서 Python AI 서비스 호출
@Service
@RequiredArgsConstructor
public class ProductRecommendationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${ai.service.url}")
    private String aiServiceUrl;
    
    public List<ProductResponse> getRecommendations(Long memberId) {
        String url = aiServiceUrl + "/recommendations/" + memberId;
        
        try {
            ResponseEntity<RecommendationResponse> response = 
                restTemplate.getForEntity(url, RecommendationResponse.class);
            
            return response.getBody().getProducts();
        } catch (Exception e) {
            log.error("AI 추천 서비스 호출 실패", e);
            return getDefaultRecommendations();
        }
    }
}
```

```python
# Python AI 서비스 (FastAPI)
from fastapi import FastAPI, HTTPException
from typing import List
import httpx

app = FastAPI()

# Java Product Service 호출
async def get_product_details(product_ids: List[int]):
    async with httpx.AsyncClient() as client:
        response = await client.post(
            "http://product-service:8083/internal/products/batch",
            json={"productIds": product_ids},
            headers={"X-Internal-Request": "true"}
        )
        return response.json()

@app.get("/recommendations/{member_id}")
async def get_recommendations(member_id: int):
    # AI 추천 로직
    recommended_ids = ml_model.predict(member_id)
    
    # Java 서비스에서 상품 상세 정보 조회
    products = await get_product_details(recommended_ids)
    
    return {
        "memberId": member_id,
        "products": products,
        "algorithm": "collaborative_filtering"
    }
```

---

## 5. 데이터베이스 전략

### 5.1 Database per Service 패턴

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Auth Service   │     │ Member Service  │     │ Product Service │
│                 │     │                 │     │                 │
│  ┌───────────┐  │     │  ┌───────────┐  │     │  ┌───────────┐  │
│  │  auth_db  │  │     │  │ member_db │  │     │  │product_db │  │
│  └───────────┘  │     │  └───────────┘  │     │  └───────────┘  │
└─────────────────┘     └─────────────────┘     └─────────────────┘

┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Order Service  │     │ Payment Service │     │ Review Service  │
│                 │     │                 │     │                 │
│  ┌───────────┐  │     │  ┌───────────┐  │     │  ┌───────────┐  │
│  │ order_db  │  │     │  │payment_db │  │     │  │ review_db │  │
│  └───────────┘  │     │  └───────────┘  │     │  └───────────┘  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
```

### 5.2 데이터 일관성 전략

#### Saga 패턴 (분산 트랜잭션)

```java
// 주문 생성 Saga Orchestrator
@Service
@RequiredArgsConstructor
public class OrderSagaOrchestrator {
    
    private final OrderService orderService;
    private final PaymentServiceClient paymentClient;
    private final ProductServiceClient productClient;
    private final KafkaTemplate<String, SagaEvent> kafkaTemplate;
    
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // Step 1: 주문 생성 (Pending 상태)
        Order order = orderService.createPendingOrder(request);
        
        try {
            // Step 2: 재고 확인 및 차감
            productClient.reserveStock(request.getProductId(), request.getQuantity());
            
            // Step 3: 결제 처리
            PaymentResponse payment = paymentClient.processPayment(
                order.getId(), 
                order.getTotalAmount()
            );
            
            // Step 4: 주문 확정
            order.confirm();
            orderRepository.save(order);
            
            // 성공 이벤트 발행
            publishSuccessEvent(order);
            
            return OrderResponse.from(order);
            
        } catch (Exception e) {
            // 보상 트랜잭션 (Rollback)
            compensate(order, request);
            throw new OrderCreationException("주문 생성 실패", e);
        }
    }
    
    private void compensate(Order order, OrderCreateRequest request) {
        // 재고 복구
        productClient.releaseStock(request.getProductId(), request.getQuantity());
        
        // 결제 취소
        paymentClient.cancelPayment(order.getId());
        
        // 주문 취소
        order.cancel();
        orderRepository.save(order);
        
        // 실패 이벤트 발행
        publishFailureEvent(order);
    }
}
```

### 5.3 CQRS 패턴 (읽기/쓰기 분리)

```java
// Command Side (쓰기)
@Service
public class ProductCommandService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;
    
    @Transactional
    public void createProduct(ProductCreateCommand command) {
        Product product = Product.create(command);
        productRepository.save(product);
        
        // 이벤트 발행 (읽기 모델 업데이트용)
        ProductEvent event = ProductEvent.created(product);
        kafkaTemplate.send("product-events", event);
    }
}

// Query Side (읽기)
@Service
public class ProductQueryService {
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    public List<ProductDocument> searchProducts(String keyword) {
        // Elasticsearch에서 빠른 검색
        return elasticsearchOperations.search(
            Query.findByKeyword(keyword),
            ProductDocument.class
        );
    }
}

// 이벤트 리스너 (읽기 모델 동기화)
@Service
public class ProductEventListener {
    
    @Autowired
    private ElasticsearchOperations elasticsearchOperations;
    
    @KafkaListener(topics = "product-events")
    public void handleProductEvent(ProductEvent event) {
        if (event.getType() == EventType.PRODUCT_CREATED) {
            ProductDocument doc = ProductDocument.from(event);
            elasticsearchOperations.save(doc);
        }
    }
}
```

---

## 6. 보안 아키텍처

### 6.1 인증/인가 흐름

```
1. 사용자 로그인
   Client → API Gateway → Auth Service
   
2. JWT 토큰 발급
   Auth Service → Client (Access Token + Refresh Token)
   
3. API 요청
   Client → API Gateway (with Access Token)
   
4. 토큰 검증
   API Gateway → JWT 검증 → 서비스 라우팅
   
5. 서비스 호출
   API Gateway → Target Service (with X-User-Id header)
```

### 6.2 서비스 간 인증

```java
// Internal API 보호
@Configuration
public class InternalApiSecurityConfig {
    
    @Bean
    public SecurityFilterChain internalApiFilterChain(HttpSecurity http) {
        http
            .securityMatcher("/internal/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/internal/**")
                .hasIpAddress("10.0.0.0/8") // 내부 IP만 허용
            )
            .addFilterBefore(
                new InternalServiceAuthFilter(), 
                UsernamePasswordAuthenticationFilter.class
            );
        
        return http.build();
    }
}

// 서비스 간 통신 시 인증 헤더 추가
@Component
public class FeignClientInterceptor implements RequestInterceptor {
    
    @Value("${internal.service.secret}")
    private String internalSecret;
    
    @Override
    public void apply(RequestTemplate template) {
        template.header("X-Internal-Secret", internalSecret);
        template.header("X-Service-Name", "order-service");
    }
}
```

---

## 7. 장애 처리 및 복원력

### 7.1 Circuit Breaker (Resilience4j)

```java
@Service
public class PaymentService {
    
    @CircuitBreaker(
        name = "payment-pg",
        fallbackMethod = "paymentFallback"
    )
    @Retry(name = "payment-pg", maxAttempts = 3)
    @TimeLimiter(name = "payment-pg")
    public PaymentResponse processPayment(PaymentRequest request) {
        // PG사 API 호출
        return pgClient.charge(request);
    }
    
    // Fallback 메서드
    public PaymentResponse paymentFallback(
        PaymentRequest request, 
        Exception e
    ) {
        log.error("결제 처리 실패, 대기 큐에 추가", e);
        
        // 결제 대기 큐에 추가
        paymentQueueService.enqueue(request);
        
        return PaymentResponse.builder()
            .status(PaymentStatus.PENDING)
            .message("결제 처리 중입니다. 잠시 후 다시 확인해주세요.")
            .build();
    }
}
```

### 7.2 Bulkhead (격리)

```yaml
# application.yml
resilience4j:
  bulkhead:
    instances:
      payment-service:
        max-concurrent-calls: 10
        max-wait-duration: 1s
      
  circuitbreaker:
    instances:
      payment-pg:
        sliding-window-size: 100
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 10
      
  retry:
    instances:
      payment-pg:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2
```

---

## 8. 모니터링 및 관찰성

### 8.1 분산 추적 (Distributed Tracing)

```yaml
# Spring Cloud Sleuth + Zipkin
spring:
  sleuth:
    sampler:
      probability: 1.0 # 100% 샘플링 (개발), 운영은 10%
  zipkin:
    base-url: http://zipkin-server:9411
    sender:
      type: web
```

```java
// 커스텀 Span 추가
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final Tracer tracer;
    
    public Order createOrder(OrderCreateRequest request) {
        Span span = tracer.nextSpan().name("create-order").start();
        
        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            span.tag("order.memberId", request.getMemberId().toString());
            span.tag("order.amount", request.getTotalAmount().toString());
            
            // 주문 생성 로직
            Order order = processOrder(request);
            
            span.event("order-created");
            return order;
            
        } finally {
            span.end();
        }
    }
}
```

### 8.2 메트릭 수집 (Prometheus + Grafana)

```java
// 커스텀 메트릭
@Component
public class OrderMetrics {
    
    private final Counter orderCounter;
    private final Timer orderProcessingTimer;
    
    public OrderMetrics(MeterRegistry registry) {
        this.orderCounter = Counter.builder("orders.created")
            .description("총 주문 생성 수")
            .tag("service", "order-service")
            .register(registry);
        
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("주문 처리 시간")
            .register(registry);
    }
    
    public void recordOrderCreated() {
        orderCounter.increment();
    }
    
    public void recordProcessingTime(long milliseconds) {
        orderProcessingTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }
}
```

---

## 9. Python 서비스 상세 설계

### 9.1 AI 분석 서비스 (FastAPI)

```python
# main.py
from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from prometheus_fastapi_instrumentator import Instrumentator
import httpx
from typing import List

app = FastAPI(title="AI Analysis Service", version="1.0.0")

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Prometheus 메트릭
Instrumentator().instrument(app).expose(app)

# Java 서비스 클라이언트
class JavaServiceClient:
    def __init__(self):
        self.base_url = "http://api-gateway:8080"
        self.internal_secret = os.getenv("INTERNAL_SERVICE_SECRET")
    
    async def get_member_history(self, member_id: int):
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{self.base_url}/api/v1/members/{member_id}/history",
                headers={"X-Internal-Secret": self.internal_secret}
            )
            return response.json()

# AI 추천 엔드포인트
@app.get("/recommendations/{member_id}")
async def get_recommendations(
    member_id: int,
    client: JavaServiceClient = Depends()
):
    # 회원 구매 이력 조회
    history = await client.get_member_history(member_id)
    
    # ML 모델로 추천
    recommendations = ml_model.predict(history)
    
    return {
        "memberId": member_id,
        "recommendations": recommendations,
        "algorithm": "collaborative_filtering",
        "confidence": 0.85
    }

# 시장 가격 분석
@app.post("/market/price-analysis")
async def analyze_price(request: PriceAnalysisRequest):
    # 시계열 분석
    forecast = time_series_model.forecast(
        product_id=request.product_id,
        days=request.forecast_days
    )
    
    return {
        "productId": request.product_id,
        "currentPrice": request.current_price,
        "forecast": forecast,
        "recommendation": "적정 가격" if forecast["optimal"] else "가격 조정 필요"
    }
```

### 9.2 Python 서비스 배포 (Dockerfile)

```dockerfile
FROM python:3.11-slim

WORKDIR /app

# 의존성 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 복사
COPY . .

# 비root 사용자
RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

# 포트 노출
EXPOSE 9001

# Uvicorn 실행
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "9001", "--workers", "4"]
```

---

## 10. 배포 전략

### 10.1 Blue-Green 배포

```yaml
# Kubernetes Service (항상 안정 버전 가리킴)
apiVersion: v1
kind: Service
metadata:
  name: product-service
spec:
  selector:
    app: product-service
    version: blue  # 또는 green
  ports:
  - port: 8083
    targetPort: 8080

---
# Blue Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service-blue
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
      version: blue
  template:
    metadata:
      labels:
        app: product-service
        version: blue
    spec:
      containers:
      - name: product-service
        image: xlcfi/product-service:1.0.0
        ports:
        - containerPort: 8080

---
# Green Deployment (새 버전)
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service-green
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
      version: green
  template:
    metadata:
      labels:
        app: product-service
        version: green
    spec:
      containers:
      - name: product-service
        image: xlcfi/product-service:1.1.0
        ports:
        - containerPort: 8080
```

### 10.2 Canary 배포

```yaml
# Istio VirtualService (트래픽 분할)
apiVersion: networking.istio.io/v1beta1
kind: VirtualService
metadata:
  name: product-service
spec:
  hosts:
  - product-service
  http:
  - match:
    - headers:
        x-canary:
          exact: "true"
    route:
    - destination:
        host: product-service
        subset: v2
  - route:
    - destination:
        host: product-service
        subset: v1
      weight: 90
    - destination:
        host: product-service
        subset: v2
      weight: 10  # 10% 트래픽만 신규 버전으로
```

---

## 11. 비용 최적화

### 11.1 리소스 할당 전략

```yaml
# Phase 1 (MVP) - 최소 리소스
Core Services (Java):
  - Auth: 1 pod (0.5 CPU, 512MB)
  - Member: 2 pods (0.5 CPU, 512MB)
  - Product: 2 pods (1 CPU, 1GB)
  - Order: 2 pods (1 CPU, 1GB)
  - Payment: 2 pods (1 CPU, 1GB)

Python Services:
  - 아직 미배포 (Phase 2부터)

Database:
  - PostgreSQL: db.t3.medium (2 vCPU, 4GB)
  - Redis: cache.t3.micro (2 vCPU, 0.5GB)

월 예상 비용: $300-400

# Phase 2 (확장) - 중간 리소스
Core Services: 각 3-4 pods로 확장
Python Services: AI/Market 서비스 추가
Database: db.t3.large로 업그레이드

월 예상 비용: $800-1000

# Phase 3 (글로벌) - 대규모 리소스
Auto Scaling 적용
Multi-Region 배포
CDN 확장

월 예상 비용: $2000-3000
```

---

## 12. 다음 단계

1. **각 서비스별 상세 API 명세서** 작성
2. **데이터베이스 스키마** Java Entity로 변환
3. **단계별 구현 로드맵** 수정
4. **CI/CD 파이프라인** 구축

---

**문서 작성**: AI Assistant  
**검토 필요**: 시스템 아키텍트, DevOps 엔지니어

