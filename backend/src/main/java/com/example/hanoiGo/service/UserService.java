package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.LoginRequest;
import com.example.hanoiGo.dto.request.RegisterRequest;
import com.example.hanoiGo.dto.response.LoginResponse;
import com.example.hanoiGo.dto.response.UserResponse;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.mapper.UserMapper;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.UserRepository;
import com.example.hanoiGo.service.FirebaseService.FirebaseUserInfo;
import com.example.hanoiGo.util.JwtUtil;
import lombok.*;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

 @Service
 @RequiredArgsConstructor
 public class UserService {
    
     private final UserRepository userRepository;
     private final FirebaseService firebaseService;
     private final JwtUtil jwtUtil;
     private final UserMapper userMapper;
     private final PasswordEncoder passwordEncoder;
    
     // Đăng nhập bằng Firebase
     public LoginResponse loginWithFirebase(String firebaseToken) {
         try {
             // Xác thực Firebase token
             FirebaseUserInfo firebaseUser = firebaseService.getUserInfo(firebaseToken);
            
             // Tìm user trong database theo Firebase UID
             Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUser.getUid());
            
             User user;
             if (userOpt.isPresent()) {
                 // User đã tồn tại
                 user = userOpt.get();
                System.out.println("Thông tin user: " + user);
                
             } else {
                 // User mới, tạo mới
                 user = new User();
                 user.setFirebaseUid(firebaseUser.getUid());
                 user.setEmail(firebaseUser.getEmail());
                 user.setUsername(firebaseUser.getName()); // Dùng email làm username
                //  user.setFullName(firebaseUser.getName());
                 user.setProfilePicture(firebaseUser.getPicture());
                //  user.setSignInProvider(firebaseUser.getSignInProvider());
                 user.setPassword(""); // Không cần password cho Firebase user
                 user.setPoints(0);
                 user.setLastLogin(LocalDateTime.now());
                 user = userRepository.save(user);
             }
            
             // Tạo JWT token cho backend
             String token = jwtUtil.generateToken(user.getUsername());
             return new LoginResponse(token, userMapper.toUserResponse(user));

         } catch (Exception e) {
             throw new RuntimeException("Lỗi xác thực Firebase: " + e.getMessage());
         }
     }
    
     // Lấy thông tin user hiện tại
     public UserResponse getCurrentUser(String username) {
         Optional<User> userOpt = userRepository.findByUsername(username);
         if (userOpt.isEmpty()) {
             throw new AppException(ErrorCode.USER_NOT_EXISTED);
         }
         return userMapper.toUserResponse(userOpt.get());
     }
    
     // Lấy user theo Firebase UID
     public UserResponse getUserByFirebaseUid(String firebaseUid) {
         Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
         if (userOpt.isEmpty()) {
             throw new AppException(ErrorCode.USER_NOT_EXISTED);
         }
         return userMapper.toUserResponse(userOpt.get());
     }

     public UserResponse register(RegisterRequest request) {
        //  Kiểm tra username, email đã tồn tại chưa
         if (userRepository.existsByUsername(request.getUsername())) {
             throw new AppException(ErrorCode.USERNAME_EXISTED);
         }
         if (userRepository.existsByEmail(request.getEmail())) {
             throw new AppException(ErrorCode.EMAIL_EXISTED);
         }

         // Tạo user mới
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProfilePicture("https://res.cloudinary.com/dsm1uhecl/image/upload/v1758534990/%C6%A0_K%C3%8CA_VI%E1%BB%86T_NAM___AROUND_VIETNAM_ILLUSTRATION_-_Sunmire_Vu_sjajtt.jpg?fbclid=IwY2xjawNHBytleHRuA2FlbQIxMABicmlkETE3NVRVZmZuZFdPcENBNGlMAR48_f--ldiLXXT8eL7O_UisEgFrJN9mGT3GGCFfxZDH8_sxmZrnoPOJcc2f5Q_aem_3vjyyKyXnp39d9OtThXiIg");
        
        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        return userMapper.toUserResponse(user);
     }

     public LoginResponse login (LoginRequest request) {
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
            if (userOpt.isEmpty()) {
                throw new AppException(ErrorCode.LOGIN_FAIL);
            }
            User user = userOpt.get();
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                throw new AppException(ErrorCode.LOGIN_FAIL);
            }
            
            // Cập nhật last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            
            // Tạo JWT token
            String token = jwtUtil.generateToken(user.getUsername());
            return new LoginResponse(token, userMapper.toUserResponse(user));
     }
}
