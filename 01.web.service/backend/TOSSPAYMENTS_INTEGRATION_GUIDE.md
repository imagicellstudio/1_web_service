# 토스페이먼츠 연동 가이드

## 📋 개요

토스페이먼츠 결제 시스템 연동이 완료되었습니다.

**작성일:** 2025-11-20  
**구현 상태:** ✅ 완료

---

## 1. 구현 완료 항목

### ✅ 1.1 TossPaymentsClient
- 결제 승인 API
- 결제 조회 API
- 결제 취소 (환불) API
- Basic Auth 인증
- WebClient 기반 HTTP 통신

### ✅ 1.2 DTO 클래스
- TossPaymentConfirmRequest
- TossPaymentConfirmResponse
- TossPaymentResponse
- TossPaymentCancelRequest
- TossPaymentCancelResponse
- TossWebhookRequest

### ✅ 1.3 PaymentService 업데이트
- 토스페이먼츠 결제 승인 로직
- 중복 결제 방지
- 결제 상태 관리
- 환불 처리
- Webhook 상태 업데이트

### ✅ 1.4 Webhook Controller
- 토스페이먼츠 Webhook 엔드포인트
- 결제 상태 변경 알림 처리
- 이벤트 타입별 분기 처리

### ✅ 1.5 의존성 및 설정
- spring-boot-starter-webflux
- WebClient 설정
- application.yml 설정

---

## 2. 결제 흐름

### 2.1 전체 프로세스

```
[프론트엔드]                    [백엔드]                    [토스페이먼츠]

1. 주문 생성
   └─────────────────────────> POST /api/orders
                                └─> 주문 저장
                                └─> orderId 반환
   <──────────────────────────

2. 결제 생성
   └─────────────────────────> POST /api/payments
                                └─> 결제 정보 저장 (PENDING)
                                └─> paymentId 반환
   <──────────────────────────

3. 토스 결제 위젯 호출
   (클라이언트 SDK)
   └─────────────────────────────────────────────> 결제 처리
                                                    └─> 카드 승인
                                                    └─> paymentKey 생성
   <─────────────────────────────────────────────

4. 결제 승인 요청
   (paymentKey, orderId, amount)
   └─────────────────────────> POST /api/payments/toss/confirm
                                └─> 토스 API 호출
                                    (결제 승인)
                                    └────────────────────────> POST /v1/payments/confirm
                                                                └─> 결제 승인 처리
                                    <────────────────────────
                                └─> 결제 상태 업데이트 (COMPLETED)
                                └─> 주문 상태 업데이트 (CONFIRMED)
   <──────────────────────────

5. Webhook (비동기)
                                <────────────────────────── POST /api/payments/webhook/toss
                                └─> 결제 상태 동기화
```

---

## 3. API 명세

### 3.1 결제 생성

**Endpoint:** `POST /api/payments`

**Request:**
```json
{
  "orderId": 1,
  "amount": 50000,
  "currency": "KRW",
  "paymentMethod": "CREDIT_CARD",
  "pgProvider": "TOSS"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderId": 1,
    "amount": 50000,
    "currency": "KRW",
    "status": "PENDING",
    "pgProvider": "TOSS",
    "createdAt": "2025-11-20T10:00:00"
  },
  "message": "결제가 생성되었습니다"
}
```

### 3.2 토스페이먼츠 결제 승인

**Endpoint:** `POST /api/payments/toss/confirm`

**Query Parameters:**
- `paymentKey`: 토스페이먼츠 결제 키 (클라이언트에서 전달)
- `orderId`: 주문 ID
- `amount`: 결제 금액

**Request:**
```
POST /api/payments/toss/confirm?paymentKey=tviva20240101000000000001&orderId=1&amount=50000
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "orderId": 1,
    "amount": 50000,
    "status": "COMPLETED",
    "pgProvider": "TOSS",
    "pgTransactionId": "tviva20240101000000000001",
    "paidAt": "2025-11-20T10:05:00"
  },
  "message": "결제가 승인되었습니다"
}
```

### 3.3 결제 환불

**Endpoint:** `POST /api/payments/{id}/refund`

**Request:**
```json
{
  "reason": "고객 요청"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "status": "REFUNDED",
    "refundedAt": "2025-11-20T11:00:00"
  },
  "message": "환불이 완료되었습니다"
}
```

### 3.4 Webhook

**Endpoint:** `POST /api/payments/webhook/toss`

**Request (토스페이먼츠에서 전송):**
```json
{
  "eventType": "PAYMENT_CONFIRMED",
  "createdAt": "2025-11-20T10:05:00",
  "data": {
    "paymentKey": "tviva20240101000000000001",
    "orderId": "1",
    "status": "DONE"
  }
}
```

**Response:**
```
200 OK
```

---

## 4. 프론트엔드 연동

### 4.1 토스페이먼츠 SDK 설치

```html
<!-- index.html -->
<script src="https://js.tosspayments.com/v1/payment-widget"></script>
```

또는

```bash
npm install @tosspayments/payment-widget-sdk
```

### 4.2 결제 위젯 초기화

```javascript
// React 예시
import { useEffect, useRef } from 'react';

function CheckoutPage() {
  const paymentWidgetRef = useRef(null);
  const paymentMethodsWidgetRef = useRef(null);
  
  const clientKey = 'test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq';
  const customerKey = 'user_' + userId; // 고객 고유 ID
  
  useEffect(() => {
    (async () => {
      // 결제 위젯 초기화
      const paymentWidget = await loadPaymentWidget(clientKey, customerKey);
      
      // 결제 수단 위젯 렌더링
      const paymentMethodsWidget = paymentWidget.renderPaymentMethods(
        '#payment-widget',
        { value: 50000 },
        { variantKey: 'DEFAULT' }
      );
      
      paymentWidgetRef.current = paymentWidget;
      paymentMethodsWidgetRef.current = paymentMethodsWidget;
    })();
  }, []);
  
  const handlePayment = async () => {
    const paymentWidget = paymentWidgetRef.current;
    
    try {
      // 결제 요청
      await paymentWidget.requestPayment({
        orderId: orderId,
        orderName: '유기농 배추 외 2건',
        successUrl: `${window.location.origin}/payment/success`,
        failUrl: `${window.location.origin}/payment/fail`,
        customerEmail: 'customer@example.com',
        customerName: '홍길동',
      });
    } catch (error) {
      console.error('결제 실패:', error);
    }
  };
  
  return (
    <div>
      <div id="payment-widget"></div>
      <button onClick={handlePayment}>결제하기</button>
    </div>
  );
}
```

### 4.3 결제 성공 페이지

```javascript
// PaymentSuccessPage.jsx
import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';

function PaymentSuccessPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  useEffect(() => {
    const confirmPayment = async () => {
      const paymentKey = searchParams.get('paymentKey');
      const orderId = searchParams.get('orderId');
      const amount = searchParams.get('amount');
      
      try {
        // 백엔드에 결제 승인 요청
        const response = await axios.post(
          '/api/payments/toss/confirm',
          null,
          {
            params: { paymentKey, orderId, amount }
          }
        );
        
        if (response.data.success) {
          alert('결제가 완료되었습니다!');
          navigate('/orders');
        }
      } catch (error) {
        console.error('결제 승인 실패:', error);
        alert('결제 승인에 실패했습니다.');
        navigate('/payment/fail');
      }
    };
    
    confirmPayment();
  }, []);
  
  return <div>결제 처리 중...</div>;
}
```

### 4.4 결제 실패 페이지

```javascript
// PaymentFailPage.jsx
import { useSearchParams } from 'react-router-dom';

function PaymentFailPage() {
  const [searchParams] = useSearchParams();
  
  const errorCode = searchParams.get('code');
  const errorMessage = searchParams.get('message');
  
  return (
    <div>
      <h1>결제 실패</h1>
      <p>에러 코드: {errorCode}</p>
      <p>에러 메시지: {errorMessage}</p>
      <button onClick={() => window.location.href = '/'}>
        홈으로 돌아가기
      </button>
    </div>
  );
}
```

---

## 5. 환경 설정

### 5.1 토스페이먼츠 계정 설정

1. **토스페이먼츠 가입**
   - https://www.tosspayments.com/

2. **개발자 센터 접속**
   - https://developers.tosspayments.com/

3. **API 키 발급**
   - 개발자 센터 → 내 개발 정보
   - **클라이언트 키** (프론트엔드용)
   - **시크릿 키** (백엔드용)

### 5.2 테스트 키 (개발 환경)

```yaml
# application-dev.yml
payment:
  toss:
    secret-key: test_sk_zXLkKEypNArWmo50nX3lmeaxYG5R
    api-url: https://api.tosspayments.com
    client-key: test_ck_D5GePWvyJnrK0W0k6q8gLzN97Eoq
```

### 5.3 프로덕션 키

```bash
# 환경 변수로 설정
export TOSS_SECRET_KEY="live_sk_your_secret_key"
export TOSS_CLIENT_KEY="live_ck_your_client_key"
```

### 5.4 Webhook URL 등록

1. 토스페이먼츠 개발자 센터
2. 내 개발 정보 → Webhook 설정
3. URL 입력: `https://yourdomain.com/api/payments/webhook/toss`

---

## 6. 테스트

### 6.1 테스트 카드 번호

토스페이먼츠 테스트 환경에서 사용 가능한 카드:

| 카드사 | 카드번호 | 유효기간 | CVC | 비밀번호 |
|--------|----------|----------|-----|----------|
| 신한카드 | 5429-7900-0000-0000 | 12/25 | 123 | 1234 |
| 국민카드 | 9430-0400-0000-0000 | 12/25 | 123 | 1234 |
| 현대카드 | 5487-0100-0000-0000 | 12/25 | 123 | 1234 |

### 6.2 로컬 Webhook 테스트

**ngrok 사용:**

```bash
# ngrok 설치
npm install -g ngrok

# 터널 생성
ngrok http 8084

# 출력된 URL을 토스페이먼츠 Webhook URL에 등록
# 예: https://abc123.ngrok.io/api/payments/webhook/toss
```

### 6.3 Postman 테스트

**결제 승인 테스트:**

```
POST http://localhost:8084/api/payments/toss/confirm
  ?paymentKey=test_payment_key_123
  &orderId=1
  &amount=50000
```

---

## 7. 에러 처리

### 7.1 주요 에러 코드

| 에러 코드 | 설명 | 처리 방법 |
|-----------|------|-----------|
| PAYMENT001 | 주문을 찾을 수 없습니다 | 주문 ID 확인 |
| PAYMENT002 | 결제 금액 불일치 | 금액 재확인 |
| PAYMENT003 | 중복 결제 | 기존 결제 확인 |
| PAYMENT004 | 결제를 찾을 수 없습니다 | 결제 ID 확인 |
| PAYMENT005 | 이미 처리된 결제 | 결제 상태 확인 |
| PAYMENT006 | 결제 승인 실패 | 토스 API 응답 확인 |
| PAYMENT007 | 환불 불가 상태 | 결제 상태 확인 |

### 7.2 토스페이먼츠 에러

토스페이먼츠 API 에러는 `pgResponse`에 저장됩니다:

```json
{
  "error": "INVALID_CARD_NUMBER",
  "message": "유효하지 않은 카드 번호입니다",
  "failedAt": "2025-11-20T10:05:00"
}
```

---

## 8. 보안 고려사항

### 8.1 시크릿 키 보안

- ✅ 환경 변수로 관리
- ✅ Git에 커밋하지 않음
- ✅ 프론트엔드에 노출 금지

### 8.2 결제 금액 검증

```java
// 서버에서 반드시 금액 검증
if (request.getAmount().compareTo(order.getTotal()) != 0) {
    throw new BusinessException("PAYMENT002", "결제 금액이 주문 금액과 일치하지 않습니다");
}
```

### 8.3 중복 결제 방지

```java
// 중복 결제 확인
boolean existsPendingPayment = paymentRepository.existsByOrderIdAndStatusIn(
        order.getId(), 
        List.of(PaymentStatus.PENDING, PaymentStatus.COMPLETED)
);
```

### 8.4 Webhook 검증

토스페이먼츠는 Webhook 서명 검증을 제공하지 않으므로, IP 화이트리스트 설정 권장.

---

## 9. 다음 단계

### ✅ 완료
1. 토스페이먼츠 연동
2. 결제 승인/취소
3. Webhook 처리

### ⏭️ 향후 구현
1. 나이스페이 연동
2. Stripe 연동 (해외 결제)
3. 정기결제
4. 에스크로
5. 부분 환불

---

## 10. 참고 자료

- [토스페이먼츠 공식 문서](https://docs.tosspayments.com/)
- [결제 위젯 가이드](https://docs.tosspayments.com/guides/payment-widget/integration)
- [API 레퍼런스](https://docs.tosspayments.com/reference)
- [Webhook 가이드](https://docs.tosspayments.com/guides/webhook)

---

**작성일:** 2025-11-20  
**작성자:** AI Assistant  
**구현 상태:** ✅ 완료


