package com.xlcfi.payment.dto.nicepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 나이스페이 결제 조회 응답
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NicePayResponse {
    
    private String resultCode;
    private String resultMsg;
    private String tid;
    private String orderId;
    private BigDecimal amt;
    private String payMethod;
    private String status;          // paid, ready, failed, cancelled
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
}







