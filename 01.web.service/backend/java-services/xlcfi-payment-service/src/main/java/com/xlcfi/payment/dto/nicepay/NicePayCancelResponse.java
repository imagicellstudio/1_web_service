package com.xlcfi.payment.dto.nicepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 나이스페이 결제 취소 응답
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NicePayCancelResponse {
    
    private String resultCode;
    private String resultMsg;
    private String tid;
    private String orderId;
    private BigDecimal cancelAmt;
    private BigDecimal balanceAmt;      // 취소 후 잔액
    private LocalDateTime cancelledAt;
}

