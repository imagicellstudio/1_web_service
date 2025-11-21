package com.xlcfi.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlcfi.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 인증 실패 시 처리하는 Entry Point
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.error("인증 실패: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("code", "UNAUTHORIZED");
        errorDetails.put("message", "인증이 필요합니다");
        errorDetails.put("path", request.getRequestURI());

        ApiResponse<Object> apiResponse = ApiResponse.error(
                "인증이 필요합니다", 
                errorDetails
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

