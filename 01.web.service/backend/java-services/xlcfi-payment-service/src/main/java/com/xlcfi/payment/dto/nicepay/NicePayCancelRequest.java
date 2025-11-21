package com.xlcfi.payment.dto.nicepay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 나이스페이 결제 취소 요청
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NicePayCancelRequest {
    
    /**
     * 취소 금액
     */
    private BigDecimal cancelAmt;
    
    /**
     * 취소 사유
     */
    private String cancelMsg;
    
    /**
     * 부분 취소 여부
     */
    private Boolean partialCancelCode;
    
    /**
     * 서명 (SHA-256)
     */
    private String signature;
}


