package com.xlcfi.common.ratelimit;

/**
 * Rate Limit 타입
 */
public enum RateLimitType {
    /**
     * IP 주소 기반
     */
    IP,
    
    /**
     * 사용자 ID 기반
     */
    USER,
    
    /**
     * API 엔드포인트 기반
     */
    API,
    
    /**
     * 전역 (모든 요청)
     */
    GLOBAL
}

