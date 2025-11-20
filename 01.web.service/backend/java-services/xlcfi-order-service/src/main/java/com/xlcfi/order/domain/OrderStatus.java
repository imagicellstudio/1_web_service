package com.xlcfi.order.domain;

public enum OrderStatus {
    PENDING,      // 대기
    PAID,         // 결제완료
    CONFIRMED,    // 판매자확인
    SHIPPING,     // 배송중
    DELIVERED,    // 배송완료
    CANCELLED     // 취소
}

