package com.xlcfi.payment.controller;

import com.xlcfi.payment.dto.stripe.StripeWebhookRequest;
import com.xlcfi.payment.dto.tosspayments.TossWebhookRequest;
import com.xlcfi.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 Webhook Controller
 * PG사로부터 결제 상태 변경 알림을 받습니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments/webhook")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    /**
     * 토스페이먼츠 Webhook
     * 
     * @param request Webhook 요청
     * @return 200 OK
     */
    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(@RequestBody TossWebhookRequest request) {
        log.info("토스페이먼츠 Webhook 수신: eventType={}, paymentKey={}", 
                request.getEventType(), request.getData().getPaymentKey());

        try {
            switch (request.getEventType()) {
                case "PAYMENT_CONFIRMED":
                    paymentService.updatePaymentStatus(
                            request.getData().getPaymentKey(), 
                            "DONE"
                    );
                    log.info("결제 승인 완료 처리: paymentKey={}", 
                            request.getData().getPaymentKey());
                    break;

                case "PAYMENT_CANCELED":
                    paymentService.updatePaymentStatus(
                            request.getData().getPaymentKey(), 
                            "CANCELED"
                    );
                    log.info("결제 취소 처리: paymentKey={}", 
                            request.getData().getPaymentKey());
                    break;

                case "PAYMENT_FAILED":
                    paymentService.updatePaymentStatus(
                            request.getData().getPaymentKey(), 
                            "ABORTED"
                    );
                    log.info("결제 실패 처리: paymentKey={}", 
                            request.getData().getPaymentKey());
                    break;

                default:
                    log.warn("알 수 없는 이벤트 타입: {}", request.getEventType());
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }

    /**
     * 나이스페이 Webhook
     * 
     * @param tid 거래 ID
     * @param resultCode 결과 코드
     * @return 200 OK
     */
    @PostMapping("/nicepay")
    public ResponseEntity<Void> handleNicePayWebhook(
            @RequestParam String tid,
            @RequestParam String resultCode,
            @RequestParam(required = false) String status) {
        
        log.info("나이스페이 Webhook 수신: tid={}, resultCode={}, status={}", 
                tid, resultCode, status);

        try {
            if ("0000".equals(resultCode)) {
                if ("paid".equals(status)) {
                    paymentService.updatePaymentStatus(tid, "DONE");
                    log.info("결제 승인 완료 처리: tid={}", tid);
                } else if ("cancelled".equals(status)) {
                    paymentService.updatePaymentStatus(tid, "CANCELED");
                    log.info("결제 취소 처리: tid={}", tid);
                }
            } else {
                paymentService.updatePaymentStatus(tid, "ABORTED");
                log.info("결제 실패 처리: tid={}", tid);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Stripe Webhook
     * 
     * @param payload Webhook 페이로드 (JSON)
     * @param signature Stripe 서명 헤더
     * @return 200 OK
     */
    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        
        log.info("Stripe Webhook 수신");

        try {
            // TODO: Stripe 서명 검증 (보안 강화)
            // Event event = Webhook.constructEvent(payload, signature, webhookSecret);
            
            // 간단한 파싱 (실제로는 Stripe SDK 사용)
            if (payload.contains("payment_intent.succeeded")) {
                // Payment Intent ID 추출 (실제로는 JSON 파싱)
                String paymentIntentId = extractPaymentIntentId(payload);
                paymentService.confirmStripePayment(paymentIntentId);
                log.info("Stripe 결제 성공 처리: paymentIntentId={}", paymentIntentId);
            } else if (payload.contains("charge.refunded")) {
                String paymentIntentId = extractPaymentIntentId(payload);
                paymentService.updatePaymentStatus(paymentIntentId, "refunded");
                log.info("Stripe 환불 처리: paymentIntentId={}", paymentIntentId);
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Webhook 처리 중 오류 발생: {}", e.getMessage(), e);
            return ResponseEntity.ok().build();
        }
    }

    /**
     * Webhook 테스트 엔드포인트
     */
    @GetMapping("/test")
    public ResponseEntity<String> testWebhook() {
        return ResponseEntity.ok("Webhook endpoint is working");
    }

    /**
     * Payment Intent ID 추출 (간단한 구현)
     * 실제로는 JSON 파싱 라이브러리 사용
     */
    private String extractPaymentIntentId(String payload) {
        // 간단한 정규식 또는 JSON 파싱
        // 실제 구현에서는 Jackson ObjectMapper 사용
        return "pi_test_123"; // 임시
    }
}
