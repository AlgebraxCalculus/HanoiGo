package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.*;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());

            User savedUser = userService.registerUser(user);

            UserDTO userDTO = new UserDTO();
            userDTO.setId(savedUser.getId());
            userDTO.setUsername(savedUser.getUsername());
            userDTO.setEmail(savedUser.getEmail());
            userDTO.setFullName(savedUser.getFullName());
            userDTO.setPhone(savedUser.getPhone());
            userDTO.setEnabled(savedUser.getEnabled());
            userDTO.setCreatedAt(savedUser.getCreatedAt());

            return ResponseEntity.ok(ApiResponse.success("Đăng ký thành công!", userDTO));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.loginUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công!", token));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            userService.forgotPassword(request.getEmail());
            return ResponseEntity.ok(ApiResponse.success("Mã OTP đã được gửi đến email của bạn!"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getEmail(), request.getOtpCode(), request.getNewPassword());
            return ResponseEntity.ok(ApiResponse.success("Đặt lại mật khẩu thành công!"));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }
}