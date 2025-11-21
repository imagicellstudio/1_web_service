package com.xlcfi.payment.dto.tosspayments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 토스페이먼츠 결제 승인 응답
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentConfirmResponse {
    
    /**
     * 결제 키
     */
    private String paymentKey;
    
    /**
     * 주문 ID
     */
    private String orderId;
    
    /**
     * 주문명
     */
    private String orderName;
    
    /**
     * 결제 상태 (READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)
     */
    private String status;
    
    /**
     * 결제 수단 (카드, 가상계좌, 간편결제, 휴대폰, 계좌이체, 문화상품권, 도서문화상품권, 게임문화상품권)
     */
    private String method;
    
    /**
     * 총 결제 금액
     */
    private BigDecimal totalAmount;
    
    /**
     * 실제 결제 금액
     */
    private BigDecimal balanceAmount;
    
    /**
     * 공급가액
     */
    private BigDecimal suppliedAmount;
    
    /**
     * 부가세
     */
    private BigDecimal vat;
    
    /**
     * 통화 (KRW)
     */
    private String currency;
    
    /**
     * 결제 승인 날짜
     */
    private LocalDateTime approvedAt;
    
    /**
     * 카드 정보
     */
    private TossCardInfo card;
    
    /**
     * 가상계좌 정보
     */
    private TossVirtualAccountInfo virtualAccount;
    
    /**
     * 간편결제 정보
     */
    private TossEasyPayInfo easyPay;
    
    /**
     * 카드 정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TossCardInfo {
        private String company;           // 카드사
        private String number;            // 카드번호 (마스킹)
        private Integer installmentPlanMonths;  // 할부 개월 수
        private String approveNo;         // 승인 번호
        private String cardType;          // 카드 타입 (신용, 체크, 기프트)
        private String ownerType;         // 소유자 타입 (개인, 법인)
        private String acquireStatus;     // 매입 상태
        private Boolean isInterestFree;   // 무이자 할부 여부
    }
    
    /**
     * 가상계좌 정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TossVirtualAccountInfo {
        private String accountType;       // 계좌 타입 (일반, 고정)
        private String accountNumber;     // 계좌 번호
        private String bankCode;          // 은행 코드
        private String customerName;      // 입금자명
        private LocalDateTime dueDate;    // 입금 기한
        private String refundStatus;      // 환불 상태
        private Boolean expired;          // 만료 여부
        private String settlementStatus;  // 정산 상태
    }
    
    /**
     * 간편결제 정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TossEasyPayInfo {
        private String provider;          // 간편결제사 (토스페이, 네이버페이, 카카오페이 등)
        private BigDecimal amount;        // 간편결제 금액
        private BigDecimal discountAmount; // 간편결제 할인 금액
    }
}

