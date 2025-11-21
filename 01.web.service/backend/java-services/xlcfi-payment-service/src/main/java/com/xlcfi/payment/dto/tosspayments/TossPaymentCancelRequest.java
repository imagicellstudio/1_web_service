package com.xlcfi.payment.dto.tosspayments;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 토스페이먼츠 결제 취소 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentCancelRequest {
    
    /**
     * 취소 사유
     */
    private String cancelReason;
    
    /**
     * 취소 금액 (부분 취소 시 필수, 전체 취소 시 생략 가능)
     */
    private BigDecimal cancelAmount;
    
    /**
     * 환불 계좌 (가상계좌 결제 취소 시)
     */
    private RefundReceiveAccount refundReceiveAccount;
    
    /**
     * 환불 계좌 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefundReceiveAccount {
        private String bank;              // 은행 코드
        private String accountNumber;     // 계좌 번호
        private String holderName;        // 예금주명
    }
}



