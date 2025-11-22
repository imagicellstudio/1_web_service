package com.xlcfi.payment.dto.tosspayments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 토스페이먼츠 Webhook 요청
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossWebhookRequest {
    
    /**
     * 이벤트 타입
     * - PAYMENT_CONFIRMED: 결제 승인 완료
     * - PAYMENT_CANCELED: 결제 취소
     * - PAYMENT_FAILED: 결제 실패
     */
    private String eventType;
    
    /**
     * 이벤트 발생 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 결제 데이터
     */
    private WebhookData data;
    
    /**
     * Webhook 데이터
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookData {
        private String paymentKey;
        private String orderId;
        private String status;
    }
}







