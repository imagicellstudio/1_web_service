package com.xlcfi.common.annotation;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * 현재 로그인한 사용자의 ID를 가져오는 어노테이션
 * 
 * 사용 예:
 * public ResponseEntity<?> getProfile(@CurrentUser Long userId) {
 *     // userId는 JWT에서 추출한 사용자 ID
 * }
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}

