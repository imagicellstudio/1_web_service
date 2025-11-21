package com.xlcfi.payment.dto.nicepay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 나이스페이 결제 승인 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicePayApprovalRequest {
    
    /**
     * 거래 ID (나이스페이에서 발급)
     */
    private String tid;
    
    /**
     * 결제 금액
     */
    private BigDecimal amt;
    
    /**
     * 상품명
     */
    private String goodsName;
    
    /**
     * 주문번호
     */
    private String orderId;
    
    /**
     * 구매자명
     */
    private String buyerName;
    
    /**
     * 구매자 이메일
     */
    private String buyerEmail;
    
    /**
     * 구매자 전화번호
     */
    private String buyerTel;
    
    /**
     * 서명 (SHA-256)
     */
    private String signature;
}


