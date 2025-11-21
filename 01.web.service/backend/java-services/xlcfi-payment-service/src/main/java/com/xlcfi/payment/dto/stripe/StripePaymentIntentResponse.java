package com.xlcfi.payment.dto.stripe;

import com.stripe.model.PaymentIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Stripe Payment Intent 응답
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentIntentResponse {
    
    /**
     * Payment Intent ID
     */
    private String id;
    
    /**
     * Client Secret (프론트엔드에서 사용)
     */
    private String clientSecret;
    
    /**
     * 결제 금액 (달러 단위)
     */
    private BigDecimal amount;
    
    /**
     * 통화
     */
    private String currency;
    
    /**
     * 상태 (requires_payment_method, requires_confirmation, succeeded, canceled 등)
     */
    private String status;
    
    /**
     * 주문 ID (metadata)
     */
    private String orderId;
    
    /**
     * 생성 일시
     */
    private LocalDateTime createdAt;
    
    /**
     * Stripe PaymentIntent 객체로부터 변환
     */
    public static StripePaymentIntentResponse from(PaymentIntent paymentIntent) {
        return StripePaymentIntentResponse.builder()
                .id(paymentIntent.getId())
                .clientSecret(paymentIntent.getClientSecret())
                .amount(BigDecimal.valueOf(paymentIntent.getAmount())
                        .divide(BigDecimal.valueOf(100))) // 센트 → 달러
                .currency(paymentIntent.getCurrency())
                .status(paymentIntent.getStatus())
                .orderId(paymentIntent.getMetadata().get("orderId"))
                .createdAt(LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(paymentIntent.getCreated()),
                        ZoneId.systemDefault()))
                .build();
    }
}


