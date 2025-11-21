package com.xlcfi.payment.dto.tosspayments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 토스페이먼츠 결제 조회 응답
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {
    
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String method;
    private BigDecimal totalAmount;
    private BigDecimal balanceAmount;
    private String currency;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private String receiptUrl;
    private String checkoutUrl;
    
    /**
     * 취소 이력
     */
    private List<TossCancelInfo> cancels;
    
    /**
     * 취소 정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TossCancelInfo {
        private BigDecimal cancelAmount;
        private String cancelReason;
        private BigDecimal taxFreeAmount;
        private BigDecimal taxAmount;
        private BigDecimal refundableAmount;
        private LocalDateTime canceledAt;
        private String transactionKey;
    }
}

