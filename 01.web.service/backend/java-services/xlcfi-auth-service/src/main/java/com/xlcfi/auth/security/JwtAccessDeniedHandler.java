package com.xlcfi.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlcfi.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 인가 실패 시 처리하는 Handler
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        log.error("접근 거부: {}", accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", "FORBIDDEN");
        errorDetails.put("message", "접근 권한이 없습니다");
        errorDetails.put("path", request.getRequestURI());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                "접근 권한이 없습니다", 
                errorDetails
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

