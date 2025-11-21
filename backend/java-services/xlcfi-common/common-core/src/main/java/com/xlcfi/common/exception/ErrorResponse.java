package com.xlcfi.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    private final LocalDateTime timestamp;
    private final Integer status;
    private final Boolean success;
    private final ErrorDetail error;
    private final String path;
    private final String requestId;
    
    @Getter
    @Builder
    public static class ErrorDetail {
        private final String code;
        private final String message;
        private final List<FieldError> details;
    }
    
    @Getter
    @Builder
    public static class FieldError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }
}

