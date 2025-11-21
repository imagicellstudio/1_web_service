package com.xlcfi.payment.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Stripe 환불 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripeRefundRequest {
    
    /**
     * Payment Intent ID
     */
    private String paymentIntentId;
    
    /**
     * 환불 금액 (null이면 전액 환불)
     */
    private BigDecimal amount;
    
    /**
     * 환불 사유 (duplicate, fraudulent, requested_by_customer)
     */
    private String reason;
}


