package com.xlcfi.common.ratelimit;

import java.lang.annotation.*;

/**
 * Rate Limit 어노테이션
 * API 호출 빈도를 제한합니다.
 * 
 * 사용 예:
 * @RateLimit(limit = 10, timeWindow = 60, type = RateLimitType.IP)
 * public ResponseEntity<?> api() {
 *     // IP당 60초에 10번까지 호출 가능
 * }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    
    /**
     * 시간 창 내 최대 호출 횟수
     */
    int limit() default 100;
    
    /**
     * 시간 창 (초)
     */
    int timeWindow() default 60;
    
    /**
     * Rate Limit 타입
     */
    RateLimitType type() default RateLimitType.IP;
}

