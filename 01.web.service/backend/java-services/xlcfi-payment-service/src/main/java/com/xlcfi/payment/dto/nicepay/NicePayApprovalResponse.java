package com.xlcfi.payment.dto.nicepay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 나이스페이 결제 승인 응답
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NicePayApprovalResponse {
    
    /**
     * 결과 코드 (0000: 성공)
     */
    private String resultCode;
    
    /**
     * 결과 메시지
     */
    private String resultMsg;
    
    /**
     * 거래 ID
     */
    private String tid;
    
    /**
     * 주문번호
     */
    private String orderId;
    
    /**
     * 결제 금액
     */
    private BigDecimal amt;
    
    /**
     * 결제 수단 (CARD, BANK, VBANK 등)
     */
    private String payMethod;
    
    /**
     * 카드 정보
     */
    private NicePayCardInfo cardInfo;
    
    /**
     * 가상계좌 정보
     */
    private NicePayVirtualAccountInfo vbankInfo;
    
    /**
     * 승인 일시
     */
    private LocalDateTime authDate;
    
    /**
     * 카드 정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NicePayCardInfo {
        private String cardCode;        // 카드사 코드
        private String cardName;        // 카드사명
        private String cardNum;         // 카드번호 (마스킹)
        private Integer cardQuota;      // 할부 개월 수
        private String authCode;        // 승인번호
        private String cardType;        // 카드 타입 (신용/체크)
    }
    
    /**
     * 가상계좌 정보
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NicePayVirtualAccountInfo {
        private String vbankCode;       // 은행 코드
        private String vbankName;       // 은행명
        private String vbankNum;        // 계좌번호
        private String vbankHolder;     // 예금주
        private LocalDateTime vbankExpDate; // 입금 기한
    }
}



