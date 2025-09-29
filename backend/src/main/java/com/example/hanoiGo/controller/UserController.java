package com.example.hanoiGo.controller;

import com.example.hanoiGo.dto.UserDTO;
 import com.example.hanoiGo.service.UserService;
 import com.example.hanoiGo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Cho phép CORS
public class UserController {

     private final UserService userService;
     private final JwtUtil jwtUtil;

//     API đăng nhập bằng Firebase
     @PostMapping("/firebase-login")
     public ResponseEntity<?> loginWithFirebase(@RequestBody UserDTO.FirebaseLoginRequest request) {
         try {
            //  System.out.println("Loginnnnn")
             UserDTO.LoginResponse response = userService.loginWithFirebase(request.getFirebaseToken());
             return ResponseEntity.ok(response);
         } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
         }
     }

     // API lấy thông tin user hiện tại
     @GetMapping("/me")
     public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
         try {
             System.out.println("Received Authorization header: " + authHeader);

             // Lấy token từ header "Authorization: Bearer <token>"
             String token = authHeader.substring(7); // Bỏ "Bearer "
             System.out.println("Extracted token: " + token);

             // Lấy username từ token
             String username = jwtUtil.getUsernameFromToken(token);
             System.out.println("Username from token: " + username);

             // Lấy thông tin user
             UserDTO user = userService.getCurrentUser(username);
             System.out.println("User info: " + user);

             return ResponseEntity.ok(user);
         } catch (Exception e) {
             e.printStackTrace(); // In stacktrace ra console
             System.err.println("Error in /me: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token không hợp lệ: " + e.getMessage());
         }
     }

    // API test
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API hoạt động bình thường!");
    }
}
