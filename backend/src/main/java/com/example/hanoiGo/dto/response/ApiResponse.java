package com.example.hanoiGo.dto.response;

import com.example.hanoiGo.exception.ErrorCode;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String message;
    private T result;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // success response
    public static <T> ApiResponse<T> success(T result, String message) {
        return ApiResponse.<T>builder()
                .code(1000) // code mặc định cho success
                .message(message)
                .result(result)
                .build();
    }

    // error response từ ErrorCode
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // error response custom message
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(customMessage)
                .build();
    }
    
    // error response custom data
    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
    return ApiResponse.<T>builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .result(data)
            .build();
}
}
