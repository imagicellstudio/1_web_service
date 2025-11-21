package com.xlcfi.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private final LocalDateTime timestamp;
    private final Integer status;
    private final Boolean success;
    private final T data;
    private final String message;
    private final String path;
    private final String requestId;
    
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.OK.value())
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CREATED.value())
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .success(false)
                .message(message)
                .build();
    }
}

