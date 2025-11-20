package com.xlcfi.common.annotation;

import java.lang.annotation.*;

/**
 * Role 기반 접근 제어 어노테이션
 * 
 * 사용 예:
 * @RequireRole("ADMIN")
 * public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
 *     // ADMIN 권한이 있는 사용자만 접근 가능
 * }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireRole {
    String[] value();
}

