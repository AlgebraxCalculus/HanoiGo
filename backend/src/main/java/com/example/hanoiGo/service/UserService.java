 package com.example.hanoiGo.service;

 import com.example.hanoiGo.dto.UserDTO;
 import com.example.hanoiGo.model.User;
 import com.example.hanoiGo.repository.UserRepository;
 import com.example.hanoiGo.service.FirebaseService.FirebaseUserInfo;
 import com.example.hanoiGo.util.JwtUtil;
 import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

 import java.time.LocalDateTime;
 import java.util.Optional;
 import java.util.UUID;

 @Service
 @RequiredArgsConstructor
 public class UserService {
    
     private final UserRepository userRepository;
     private final FirebaseService firebaseService;
     private final JwtUtil jwtUtil;
    
     // Đăng nhập bằng Firebase
     public UserDTO.LoginResponse loginWithFirebase(String firebaseToken) {
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
             UserDTO userDTO = convertToDTO(user);
            
             return new UserDTO.LoginResponse(token, userDTO);
            
         } catch (Exception e) {
             throw new RuntimeException("Lỗi xác thực Firebase: " + e.getMessage());
         }
     }
    
     // Lấy thông tin user hiện tại
     public UserDTO getCurrentUser(String username) {
         Optional<User> userOpt = userRepository.findByUsername(username);
         if (userOpt.isEmpty()) {
             throw new RuntimeException("Không tìm thấy user!");
         }
         return convertToDTO(userOpt.get());
     }
    
     // Lấy user theo Firebase UID
     public UserDTO getUserByFirebaseUid(String firebaseUid) {
         Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
         if (userOpt.isEmpty()) {
             throw new RuntimeException("Không tìm thấy user!");
         }
         return convertToDTO(userOpt.get());
     }
    
     // Chuyển đổi User entity sang UserDTO
     private UserDTO convertToDTO(User user) {
         UserDTO dto = new UserDTO();
         dto.setId(user.getId());
         dto.setUsername(user.getUsername());
         dto.setEmail(user.getEmail());
        //  dto.setFullName(user.getFullName());
         dto.setProfilePicture(user.getProfilePicture());
        //  dto.setSignInProvider(user.getSignInProvider());
         dto.setCreatedAt(user.getCreatedAt());
         dto.setLastLogin(user.getLastLogin());
         dto.setPoints(user.getPoints());
         dto.setRank(user.getRank());
         return dto;
     }
 }
