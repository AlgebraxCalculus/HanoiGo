package com.example.hanoiGo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hanoiGo.dto.request.ForgotPasswordRequest;
import com.example.hanoiGo.dto.request.ResetPasswordRequest;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.service.PasswordResetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.sendOtp(request);
        return ApiResponse.success(null, "OTP sent successfully to " + request.getEmail());
    }

    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        return ApiResponse.success(null, "Password reset successfully");
    }
}