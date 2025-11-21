package com.xlcfi.payment.dto.stripe;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Stripe Webhook 요청
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StripeWebhookRequest {
    
    /**
     * 이벤트 ID
     */
    private String id;
    
    /**
     * 이벤트 타입
     * - payment_intent.succeeded: 결제 성공
     * - payment_intent.payment_failed: 결제 실패
     * - charge.refunded: 환불 완료
     */
    private String type;
    
    /**
     * 이벤트 데이터
     */
    private WebhookData data;
    
    /**
     * 생성 일시 (Unix timestamp)
     */
    private Long created;
    
    /**
     * Webhook 데이터
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookData {
        private Map<String, Object> object;
    }
}


