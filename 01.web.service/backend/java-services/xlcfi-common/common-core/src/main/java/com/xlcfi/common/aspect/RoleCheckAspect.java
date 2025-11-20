package com.xlcfi.common.aspect;

import com.xlcfi.common.annotation.RequireRole;
import com.xlcfi.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * Role 기반 접근 제어 AOP
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {

    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new BusinessException("SECURITY001", "요청 컨텍스트를 찾을 수 없습니다");
        }

        HttpServletRequest request = attributes.getRequest();
        String userRole = (String) request.getAttribute("role");

        if (userRole == null) {
            throw new BusinessException("SECURITY002", "인증 정보가 없습니다");
        }

        String[] allowedRoles = requireRole.value();
        boolean hasRole = Arrays.asList(allowedRoles).contains(userRole);

        if (!hasRole) {
            log.warn("권한 없음: userRole={}, requiredRoles={}", userRole, Arrays.toString(allowedRoles));
            throw new BusinessException("SECURITY003", "접근 권한이 없습니다");
        }

        log.debug("권한 확인 성공: userRole={}", userRole);
    }
}

