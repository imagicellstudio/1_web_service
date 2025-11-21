# 통합 결제 시스템 구현 완료

## 📋 개요

**구현 일자:** 2025-11-20  
**구현 범위:** 토스페이먼츠, 나이스페이, Stripe 통합  
**구현 상태:** ✅ 완료

---

## 1. 지원 PG사

### 1.1 국내 PG

| PG사 | 용도 | 지원 결제 수단 | 상태 |
|------|------|----------------|------|
| **토스페이먼츠** | 주요 국내 결제 | 카드, 가상계좌, 간편결제 | ✅ 완료 |
| **나이스페이** | 보조 국내 결제 | 카드, 가상계좌, 계좌이체 | ✅ 완료 |

### 1.2 해외 PG

| PG사 | 용도 | 지원 통화 | 상태 |
|------|------|-----------|------|
| **Stripe** | 글로벌 결제 | USD, EUR, KRW 등 135+ | ✅ 완료 |

---

## 2. 구현 완료 항목

### 2.1 Client 구현

| Client | 기능 | 파일 |
|--------|------|------|
| TossPaymentsClient | 결제 승인, 조회, 취소 | `client/TossPaymentsClient.java` |
| NicePayClient | 결제 승인, 조회, 취소 | `client/NicePayClient.java` |
| StripeClient | Payment Intent, 환불 | `client/StripeClient.java` |

### 2.2 DTO 클래스

**토스페이먼츠 (6개)**
- TossPaymentConfirmRequest/Response
- TossPaymentResponse
- TossPaymentCancelRequest/Response
- TossWebhookRequest

**나이스페이 (5개)**
- NicePayApprovalRequest/Response
- NicePayResponse
- NicePayCancelRequest/Response

**Stripe (5개)**
- StripePaymentIntentRequest/Response
- StripeRefundRequest/Response
- StripeWebhookRequest

### 2.3 PaymentService 통합

```java
// PG사별 결제 승인 메서드
- confirmTossPayment()       // 토스페이먼츠
- confirmNicePayment()        // 나이스페이
- createStripePaymentIntent() // Stripe (Intent 생성)
- confirmStripePayment()      // Stripe (Webhook 확인)

// 통합 환불 메서드
- refundPayment()             // PG사 자동 분기
  ├─ refundTossPayment()
  ├─ refundNicePayment()
  └─ refundStripePayment()
```

### 2.4 Webhook 통합

**Endpoint:**
- `POST /api/payments/webhook/toss` - 토스페이먼츠
- `POST /api/payments/webhook/nicepay` - 나이스페이
- `POST /api/payments/webhook/stripe` - Stripe

---

## 3. 결제 흐름 비교

### 3.1 토스페이먼츠 (국내 주력)

```
1. 주문 생성 → 결제 생성 (PENDING)
2. 프론트엔드: 토스 결제 위젯 호출
3. 사용자 카드 입력 → 토스 승인
4. 백엔드: confirmTossPayment() 호출
5. 결제 상태: COMPLETED
6. Webhook: 비동기 상태 동기화
```

**특징:**
- ✅ 간편결제 지원 (토스페이, 카카오페이 등)
- ✅ 국내 주요 카드사 모두 지원
- ✅ 가상계좌 지원
- ✅ 빠른 정산 (D+1)

### 3.2 나이스페이 (국내 보조)

```
1. 주문 생성 → 결제 생성 (PENDING)
2. 프론트엔드: 나이스페이 결제창 호출
3. 사용자 카드 입력 → 나이스페이 승인 (TID 발급)
4. 백엔드: confirmNicePayment() 호출
5. 결제 상태: COMPLETED
6. Webhook: 비동기 상태 동기화
```

**특징:**
- ✅ 계좌이체 지원
- ✅ 가상계좌 지원
- ✅ SHA-256 서명 검증
- ✅ 안정적인 국내 PG

### 3.3 Stripe (해외 주력)

```
1. 주문 생성 → 결제 생성 (PENDING)
2. 백엔드: createStripePaymentIntent() 호출
   └─ clientSecret 반환
3. 프론트엔드: Stripe Elements로 카드 입력
4. Stripe: 자동 결제 처리
5. Webhook: payment_intent.succeeded 이벤트
6. 백엔드: confirmStripePayment() 호출
7. 결제 상태: COMPLETED
```

**특징:**
- ✅ 135+ 통화 지원
- ✅ 글로벌 카드 모두 지원
- ✅ 3D Secure 자동 처리
- ✅ 강력한 사기 방지 (Radar)
- ✅ 구독 결제 지원

---

## 4. API 엔드포인트

### 4.1 공통 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/payments` | 결제 생성 (PG 무관) |
| GET | `/api/payments/{id}` | 결제 조회 |
| GET | `/api/payments/user` | 내 결제 목록 |
| POST | `/api/payments/{id}/refund` | 환불 (PG 자동 분기) |

### 4.2 PG별 승인 API

| Method | Endpoint | PG | 설명 |
|--------|----------|-----|------|
| POST | `/api/payments/toss/confirm` | 토스 | 결제 승인 |
| POST | `/api/payments/nicepay/confirm` | 나이스 | 결제 승인 |
| POST | `/api/payments/stripe/intent` | Stripe | Intent 생성 |

### 4.3 Webhook API

| Method | Endpoint | PG | 설명 |
|--------|----------|-----|------|
| POST | `/api/payments/webhook/toss` | 토스 | 상태 동기화 |
| POST | `/api/payments/webhook/nicepay` | 나이스 | 상태 동기화 |
| POST | `/api/payments/webhook/stripe` | Stripe | 상태 동기화 |

---

## 5. 환경 설정

### 5.1 application.yml

```yaml
payment:
  toss:
    secret-key: ${TOSS_SECRET_KEY:test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R}
    api-url: https://api.tosspayments.com
    client-key: ${TOSS_CLIENT_KEY:test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq}
  
  nicepay:
    merchant-key: ${NICEPAY_MERCHANT_KEY:test_merchant_key}
    merchant-id: ${NICEPAY_MERCHANT_ID:test_merchant_id}
    api-url: https://api.nicepay.co.kr
  
  stripe:
    secret-key: ${STRIPE_SECRET_KEY:sk_test_your_stripe_secret_key}
    webhook-secret: ${STRIPE_WEBHOOK_SECRET:whsec_your_webhook_secret}
```

### 5.2 환경 변수 (프로덕션)

```bash
# 토스페이먼츠
export TOSS_SECRET_KEY="live_sk_..."
export TOSS_CLIENT_KEY="live_ck_..."

# 나이스페이
export NICEPAY_MERCHANT_KEY="live_merchant_key"
export NICEPAY_MERCHANT_ID="live_merchant_id"

# Stripe
export STRIPE_SECRET_KEY="sk_live_..."
export STRIPE_WEBHOOK_SECRET="whsec_..."
```

---

## 6. 결제 수단별 지원 현황

### 6.1 카드 결제

| PG | 국내 카드 | 해외 카드 | 할부 | 무이자 |
|----|----------|----------|------|--------|
| 토스 | ✅ | ✅ | ✅ | ✅ |
| 나이스 | ✅ | ✅ | ✅ | ✅ |
| Stripe | ⚠️ | ✅ | ❌ | ❌ |

### 6.2 간편결제

| PG | 토스페이 | 카카오페이 | 네이버페이 | Apple Pay | Google Pay |
|----|---------|-----------|-----------|-----------|------------|
| 토스 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 나이스 | ❌ | ✅ | ✅ | ❌ | ❌ |
| Stripe | ❌ | ❌ | ❌ | ✅ | ✅ |

### 6.3 기타 결제 수단

| PG | 가상계좌 | 계좌이체 | 휴대폰 | 상품권 |
|----|---------|---------|--------|--------|
| 토스 | ✅ | ✅ | ✅ | ✅ |
| 나이스 | ✅ | ✅ | ✅ | ❌ |
| Stripe | ❌ | ✅ (SEPA) | ❌ | ❌ |

---

## 7. 수수료 비교

### 7.1 국내 PG

| PG | 카드 | 가상계좌 | 간편결제 | 정산 주기 |
|----|------|---------|---------|-----------|
| 토스페이먼츠 | 2.5~3.5% | 500원/건 | 2.9% | D+1 |
| 나이스페이 | 2.5~3.5% | 500원/건 | 3.0% | D+2 |

### 7.2 해외 PG

| PG | 국내 카드 | 해외 카드 | 통화 변환 | 정산 주기 |
|----|----------|----------|----------|-----------|
| Stripe | 3.6% + $0.30 | 3.9% + $0.30 | 1% | 7일 (Rolling) |

---

## 8. 에러 처리

### 8.1 공통 에러 코드

| 코드 | 메시지 | HTTP |
|------|--------|------|
| PAYMENT001 | 주문을 찾을 수 없습니다 | 404 |
| PAYMENT002 | 결제 금액 불일치 | 400 |
| PAYMENT003 | 중복 결제 | 400 |
| PAYMENT004 | 결제를 찾을 수 없습니다 | 404 |
| PAYMENT005 | 이미 처리된 결제 | 400 |
| PAYMENT006 | 결제 승인 실패 | 500 |
| PAYMENT007 | 환불 불가 상태 | 400 |
| PAYMENT008 | 지원하지 않는 PG사 | 400 |
| PAYMENT009 | 환불 처리 오류 | 500 |

### 8.2 PG별 에러 처리

**토스페이먼츠:**
```json
{
  "code": "INVALID_CARD_NUMBER",
  "message": "유효하지 않은 카드 번호입니다"
}
```

**나이스페이:**
```json
{
  "resultCode": "3001",
  "resultMsg": "카드 승인 거절"
}
```

**Stripe:**
```json
{
  "type": "card_error",
  "code": "card_declined",
  "message": "Your card was declined"
}
```

---

## 9. 테스트

### 9.1 테스트 카드

**토스페이먼츠:**
- 카드번호: `5429-7900-0000-0000`
- 유효기간: `12/25`, CVC: `123`, 비밀번호: `1234`

**나이스페이:**
- 카드번호: `5432-1234-5678-9012`
- 유효기간: `12/25`, CVC: `123`

**Stripe:**
- 카드번호: `4242-4242-4242-4242`
- 유효기간: `12/34`, CVC: `123`
- ZIP: `12345`

### 9.2 로컬 Webhook 테스트

```bash
# ngrok으로 로컬 서버 노출
ngrok http 8084

# Webhook URL 등록
# 토스: https://abc123.ngrok.io/api/payments/webhook/toss
# 나이스: https://abc123.ngrok.io/api/payments/webhook/nicepay
# Stripe: https://abc123.ngrok.io/api/payments/webhook/stripe
```

---

## 10. 프론트엔드 연동

### 10.1 토스페이먼츠

```javascript
// Payment Widget SDK 사용
const paymentWidget = await loadPaymentWidget(clientKey, customerKey);
await paymentWidget.requestPayment({
  orderId: orderId,
  orderName: '유기농 배추 외 2건',
  successUrl: `${window.location.origin}/payment/success`,
  failUrl: `${window.location.origin}/payment/fail`
});
```

### 10.2 나이스페이

```javascript
// 나이스페이 결제창 호출
AUTHNICE.requestPay({
  clientId: 'nicepay_client_id',
  method: 'card',
  orderId: orderId,
  amount: amount,
  goodsName: '유기농 배추',
  returnUrl: '/payment/nicepay/callback'
});
```

### 10.3 Stripe

```javascript
// Stripe Elements 사용
const stripe = Stripe('pk_test_...');
const { clientSecret } = await fetch('/api/payments/stripe/intent', {
  method: 'POST',
  body: JSON.stringify({ orderId })
}).then(r => r.json());

const { error } = await stripe.confirmCardPayment(clientSecret, {
  payment_method: {
    card: cardElement,
    billing_details: { name: 'Customer Name' }
  }
});
```

---

## 11. 구현 통계

### 11.1 파일 통계

| 항목 | 개수 |
|------|------|
| Client 클래스 | 3개 |
| DTO 클래스 | 16개 |
| Service 메서드 | 15개 |
| Controller 엔드포인트 | 8개 |
| 총 코드 라인 | 약 2,500줄 |

### 11.2 Git Commits

| Commit | 설명 | 파일 수 |
|--------|------|---------|
| 1 | 토스페이먼츠 연동 | 14개 |
| 2 | 나이스페이 & Stripe 연동 | 16개 |
| **총계** | **2개 커밋** | **30개 파일** |

---

## 12. 다음 단계

### ✅ 완료된 작업
1. 토스페이먼츠 연동
2. 나이스페이 연동
3. Stripe 연동
4. 통합 결제 서비스
5. Webhook 처리
6. 환불 로직

### ⏭️ 향후 작업
1. **프론트엔드 연동** (1주)
   - 결제 위젯 구현
   - 결제 성공/실패 페이지
   
2. **고급 기능** (2주)
   - 정기결제 (구독)
   - 에스크로
   - 부분 환불
   - 결제 통계 대시보드

3. **블록체인 통합** (5주)
   - 사용자 간 거래 토큰 시스템
   - 스마트 컨트랙트
   - 토큰 이코노미

---

## 13. 주요 파일 목록

```
xlcfi-payment-service/
├── client/
│   ├── TossPaymentsClient.java      ✅ 토스 API 클라이언트
│   ├── NicePayClient.java           ✅ 나이스 API 클라이언트
│   └── StripeClient.java            ✅ Stripe API 클라이언트
├── dto/
│   ├── tosspayments/                ✅ 토스 DTO (6개)
│   ├── nicepay/                     ✅ 나이스 DTO (5개)
│   └── stripe/                      ✅ Stripe DTO (5개)
├── service/
│   └── PaymentService.java          ✅ 통합 결제 서비스
├── controller/
│   ├── PaymentController.java       ✅ 결제 API
│   └── PaymentWebhookController.java ✅ Webhook API
└── config/
    └── WebClientConfig.java         ✅ WebClient 설정
```

---

## 14. 결론

### ✅ 구현 완료

**3개 PG사 통합 결제 시스템**이 **100% 완료**되었습니다.

**주요 성과:**
- ✅ 국내 2개 PG (토스, 나이스)
- ✅ 해외 1개 PG (Stripe)
- ✅ 통합 결제 인터페이스
- ✅ PG별 자동 라우팅
- ✅ 통합 환불 로직
- ✅ Webhook 비동기 처리
- ✅ 에러 처리 및 로깅

**다음 단계:**
- 블록체인 토큰 시스템 설계 (사용자 간 거래)

---

**작성일:** 2025-11-20  
**작성자:** 장재훈  **구현 상태:** ✅ 완료 (Phase 1)




