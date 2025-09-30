package com.example.hanoiGo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {
    @NotBlank(message = "OTP không được để trống")
    private String otp;
}
