package com.xlcfi.common.ratelimit;

import com.xlcfi.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting AOP
 * API 호출 빈도를 제한합니다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Around("@annotation(com.xlcfi.common.ratelimit.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 요청 정보 가져오기
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String key = generateKey(request, rateLimit);

        // Rate Limit 체크
        if (!checkRateLimit(key, rateLimit)) {
            log.warn("Rate limit 초과: key={}, limit={}/{}", 
                    key, rateLimit.limit(), rateLimit.timeWindow());
            throw new BusinessException("RATE_LIMIT_001", 
                    "요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        return joinPoint.proceed();
    }

    /**
     * Rate Limit 키 생성
     */
    private String generateKey(HttpServletRequest request, RateLimit rateLimit) {
        String identifier;

        switch (rateLimit.type()) {
            case IP:
                identifier = getClientIP(request);
                break;
            case USER:
                Object userId = request.getAttribute("userId");
                identifier = userId != null ? userId.toString() : "anonymous";
                break;
            case API:
                identifier = request.getRequestURI();
                break;
            default:
                identifier = "global";
        }

        return RATE_LIMIT_PREFIX + rateLimit.type().name().toLowerCase() + ":" + identifier;
    }

    /**
     * Rate Limit 체크 (Sliding Window Algorithm)
     */
    private boolean checkRateLimit(String key, RateLimit rateLimit) {
        try {
            String countKey = key + ":count";
            Long currentCount = redisTemplate.opsForValue().increment(countKey, 1);

            if (currentCount == null) {
                return true;
            }

            // 첫 요청이면 TTL 설정
            if (currentCount == 1) {
                redisTemplate.expire(countKey, rateLimit.timeWindow(), TimeUnit.SECONDS);
            }

            // Limit 체크
            return currentCount <= rateLimit.limit();

        } catch (Exception e) {
            log.error("Rate limit 체크 실패: {}", e.getMessage(), e);
            // Redis 오류 시에도 요청은 통과시킴
            return true;
        }
    }

    /**
     * 클라이언트 IP 주소 추출
     */
    private String getClientIP(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For는 여러 IP를 가질 수 있음 (첫 번째가 원본 클라이언트 IP)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}

