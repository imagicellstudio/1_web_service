package com.xlcfi.payment.controller;

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
     * 토스페이먼츠는 결제 상태가 변경될 때마다 Webhook을 호출합니다.
     * - PAYMENT_CONFIRMED: 결제 승인 완료
     * - PAYMENT_CANCELED: 결제 취소
     * - PAYMENT_FAILED: 결제 실패
     * 
     * @param request Webhook 요청
     * @return 200 OK
     */
    @PostMapping("/toss")
    public ResponseEntity<Void> handleTossWebhook(@RequestBody TossWebhookRequest request) {
        log.info("토스페이먼츠 Webhook 수신: eventType={}, paymentKey={}", 
                request.getEventType(), request.getData().getPaymentKey());

        try {
            // 이벤트 타입별 처리
            switch (request.getEventType()) {
                case "PAYMENT_CONFIRMED":
                    // 결제 승인 완료
                    paymentService.updatePaymentStatus(
                            request.getData().getPaymentKey(), 
                            "DONE"
                    );
                    log.info("결제 승인 완료 처리: paymentKey={}", 
                            request.getData().getPaymentKey());
                    break;

                case "PAYMENT_CANCELED":
                    // 결제 취소
                    paymentService.updatePaymentStatus(
                            request.getData().getPaymentKey(), 
                            "CANCELED"
                    );
                    log.info("결제 취소 처리: paymentKey={}", 
                            request.getData().getPaymentKey());
                    break;

                case "PAYMENT_FAILED":
                    // 결제 실패
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
            // Webhook은 실패해도 200을 반환해야 재시도를 방지할 수 있음
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
}

