package com.example.hanoiGo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    private String email;
    private String otp;
    @NotBlank(message = "Password mới không được để trống")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&:/])[A-Za-z\\d@$!%*?&:/]{8,}$",
        message = "Password phải có ít nhất 8 ký tự, bao gồm chữ cái, số và ký tự đặc biệt"
    )
    private String newPassword;
}