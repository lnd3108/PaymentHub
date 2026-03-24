package com.example.demo.common.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {
    private boolean success;
    private String message;
    private Map<String, String> errors;
    private LocalDateTime timestamp;

    public static ErrorResponse of(String message) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(String message, Map<String, String> errors) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
