package com.xlcfi.payment.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stripe Payment Intent 생성 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentIntentRequest {
    
    /**
     * 결제 금액 (달러 단위)
     */
    private BigDecimal amount;
    
    /**
     * 통화 (usd, krw, eur 등)
     */
    private String currency;
    
    /**
     * 주문 ID
     */
    private String orderId;
    
    /**
     * 상품 설명
     */
    private String description;
    
    /**
     * 고객 이메일
     */
    private String customerEmail;
}




