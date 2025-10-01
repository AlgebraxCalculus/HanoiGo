package com.example.hanoiGo.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // truyền message cho RuntimeException
        this.errorCode = errorCode;
    }
}
