package com.xlcfi.payment.dto.tosspayments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 승인 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentConfirmRequest {
    
    /**
     * 결제 키 (클라이언트에서 전달받음)
     */
    private String paymentKey;
    
    /**
     * 주문 ID
     */
    private String orderId;
    
    /**
     * 결제 금액
     */
    private BigDecimal amount;
}

