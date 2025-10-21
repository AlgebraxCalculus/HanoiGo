package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.LoginRequest;
import com.example.hanoiGo.dto.request.RegisterRequest;
import com.example.hanoiGo.dto.request.UpdateFcmTokenRequest;
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

import java.util.Comparator;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

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
                user.setLastLogin(LocalDateTime.now());
                user = userRepository.save(user);
                
             } else {
                 // User mới, tạo mới
                 user = new User();
                 user.setFirebaseUid(firebaseUser.getUid());
                 user.setEmail(firebaseUser.getEmail());
                 user.setUsername(firebaseUser.getName());
                 user.setProfilePicture(firebaseUser.getPicture());
                 user.setPassword("");
                 user.setPoints(0);
                 user.setLastLogin(LocalDateTime.now());
                 user = userRepository.save(user);
             }
            
             // Tạo JWT token cho backend
             String token = jwtUtil.generateToken(user.getUsername(), user.getId());
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

     // Lấy tất cả user có thể xếp theo điểm
     public List<UserResponse> getAllUsers(boolean orderByPoints) {
        List<User> users = userRepository.findAll();
        if (orderByPoints) {
            users.sort(
            Comparator.comparingInt(User::getPoints).reversed() // Ưu tiên điểm giảm dần
                      .thenComparing(User::getUsername, String.CASE_INSENSITIVE_ORDER) // Nếu điểm bằng, sort theo tên tăng dần
            );
        }
        return userMapper.toUserResponseList(users);
     }

     // Lấy rank của user theo username
     public int getMyRank(String username) {
        List<UserResponse> users = getAllUsers(true);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(username)) {
                return i + 1; // Rank bắt đầu từ 1
            }
        }
        return -1;
     }
    
     // Lấy user theo Firebase UID
     public UserResponse getUserByFirebaseUid(String firebaseUid) {
         Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
         if (userOpt.isEmpty()) {
             throw new AppException(ErrorCode.USER_NOT_EXISTED);
         }
         return userMapper.toUserResponse(userOpt.get());
     }

        // Lấy user theo ID
        public UserResponse getUserById(UUID id) {
            Optional<User> userOpt = userRepository.findUserById(id);
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
        user.setProfilePicture("https://res.cloudinary.com/dsm1uhecl/image/upload/v1758534990/%C6%A0_K%C3%8CA_VI%E1%BB%86T_NAM___AROUND_VIETNAM_ILLUSTRATION_-_Sunmire_Vu_sjajtt.jpg");

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
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());
            return new LoginResponse(token, userMapper.toUserResponse(user));
     }

     public void updateFcmToken(UpdateFcmTokenRequest request) {
        User user = null;
        // Ưu tiên xác định theo Firebase UID
        if (request.getFirebaseUid() != null && !request.getFirebaseUid().isEmpty()) {
            user = userRepository.findByFirebaseUid(request.getFirebaseUid())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        } 
        // Nếu không có Firebase UID thì tìm theo userId
        else if (request.getUserId() != null && !request.getUserId().isEmpty()) {
            try {
                UUID uuid = UUID.fromString(request.getUserId());
                user = userRepository.findUserById(uuid)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.USER_NOT_EXISTED);
            }
        } 
        else {
            throw new AppException(ErrorCode.INVALID_KEY);
        }
        // Cập nhật FCM token
        user.setFcmToken(request.getFcmToken());
        userRepository.save(user);
    }
}
