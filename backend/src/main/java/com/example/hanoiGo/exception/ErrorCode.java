package com.example.hanoiGo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    LOGIN_FAIL(1006, "Login fail", HttpStatus.BAD_REQUEST),
    USERNAME_EXISTED(1007, "Username existed", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1008, "Email existed", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1009, "Invalid token", HttpStatus.BAD_REQUEST),
    LOCATION_NOT_EXISTED(2001, "Location not existed", HttpStatus.NOT_FOUND),
    API_FAIL_RESPONSE(2002, "Fail to get response from external API", HttpStatus.SERVICE_UNAVAILABLE),
    CHECKPOINT_EXISTED(2003, "User has already checked in at this location", HttpStatus.BAD_REQUEST),
    BOOKMARK_ALREADY_EXISTS(2004, "Bookmark already exists", HttpStatus.BAD_REQUEST),
    BOOKMARK_NOT_FOUND(2005, "Bookmark not found", HttpStatus.NOT_FOUND),
    BOOKMARK_LIST_NOT_FOUND(2006, "Bookmark list not found", HttpStatus.NOT_FOUND),
    BOOKMARK_LIST_ALREADY_EXISTS(2007, "Bookmark list already exists", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(9998, "Unauthorized", HttpStatus.FORBIDDEN)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
