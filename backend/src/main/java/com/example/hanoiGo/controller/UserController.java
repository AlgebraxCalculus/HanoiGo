package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.request.FirebaseLoginRequest;
import com.example.hanoiGo.dto.request.LoginRequest;
import com.example.hanoiGo.dto.request.RegisterRequest;
import com.example.hanoiGo.dto.response.ApiResponse;
import com.example.hanoiGo.dto.response.LoginResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.service.UserService;
import com.example.hanoiGo.util.JwtUtil;
import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

     // Đăng ký user
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Đăng ký thành công")
                .result(response)
                .build();
    }

    // Đăng nhập (username/password)
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Đăng nhập thành công")
                .result(response)
                .build();
    }

    // Đăng nhập Firebase
    @PostMapping("/firebase-login")
    public ApiResponse<LoginResponse> loginWithFirebase(@RequestBody FirebaseLoginRequest request) {
        LoginResponse response = userService.loginWithFirebase(request.getFirebaseToken());
        return ApiResponse.<LoginResponse>builder()
                .code(1000)
                .message("Đăng nhập thành công")
                .result(response)
                .build();
    }

    // Lấy thông tin user hiện tại
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        UserResponse user = userService.getCurrentUser(username);
        return ApiResponse.<UserResponse>builder()
                .code(1000)
                .message("Lấy thông tin user thành công")
                .result(user)
                .build();
    }

    // Lấy thông tin tất cả user có thứ tự xếp hạng
    @GetMapping("/get")
    public ApiResponse<List<UserResponse>> getAllUsers(
        @RequestParam (value = "orderByPoints", required = false) boolean orderByPoints) {
        List<UserResponse> users = userService.getAllUsers(orderByPoints);
        String message = orderByPoints ? "Lấy thông tin tất cả user theo thứ tự điểm số thành công" : "Lấy thông tin tất cả user thành công";
        return ApiResponse.<List<UserResponse>>builder()
                .code(1000)
                .message(message)
                .result(users)
                .build();
    }

    // Lấy rank của user hiện tại
    @GetMapping("/my-rank")
    public ApiResponse<Integer> getMyRank(@RequestHeader("Authorization") String authHeader) {
        String token = jwtUtil.extractToken(authHeader); // helper method: cắt "Bearer "
        String username = jwtUtil.getUsernameFromToken(token);
        Integer rank = userService.getMyRank(username);
        return ApiResponse.<Integer>builder()
                .code(1000)
                .message("Lấy rank của user thành công")
                .result(rank)
                .build();
    }

    // Test API
    @GetMapping("/test")
    public ApiResponse<String> test() {
        return ApiResponse.<String>builder()
                .code(1000)
                .message("API hoạt động bình thường!")
                .result("ok")
                .build();
    }
}
