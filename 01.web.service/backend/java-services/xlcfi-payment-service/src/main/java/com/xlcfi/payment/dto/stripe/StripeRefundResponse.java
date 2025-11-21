package com.xlcfi.payment.dto.stripe;

import com.stripe.model.Refund;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Stripe 환불 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeRefundResponse {
    
    /**
     * Refund ID
     */
    private String id;
    
    /**
     * Payment Intent ID
     */
    private String paymentIntentId;
    
    /**
     * 환불 금액 (달러 단위)
     */
    private BigDecimal amount;
    
    /**
     * 통화
     */
    private String currency;
    
    /**
     * 상태 (pending, succeeded, failed, canceled)
     */
    private String status;
    
    /**
     * 환불 사유
     */
    private String reason;
    
    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;
    
    /**
     * Stripe Refund 객체로부터 변환
     */
    public static StripeRefundResponse from(Refund refund) {
        return StripeRefundResponse.builder()
                .id(refund.getId())
                .paymentIntentId(refund.getPaymentIntent())
                .amount(BigDecimal.valueOf(refund.getAmount())
                        .divide(BigDecimal.valueOf(100))) // 센트 → 달러
                .currency(refund.getCurrency())
                .status(refund.getStatus())
                .reason(refund.getReason())
                .createdAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(refund.getCreated()),
                        ZoneId.systemDefault()))
                .build();
    }
}




