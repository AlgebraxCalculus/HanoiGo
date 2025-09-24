package com.example.hanoiGo.service;

import com.example.hanoiGo.model.User;
import com.example.hanoiGo.model.OtpToken;
import com.example.hanoiGo.repository.UserRepository;
import com.example.hanoiGo.repository.OtpTokenRepository;
import com.example.hanoiGo.util.PasswordValidator;
import com.example.hanoiGo.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Transactional
    public User registerUser(User user) {
        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username đã được sử dụng!");
        }

        // Validate password
        PasswordValidator.validatePassword(user.getPassword());

        // Mã hóa password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        // Gửi welcome email
        try {
            emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getUsername());
        } catch (Exception e) {
            System.err.println("Gửi welcome email thất bại: " + e.getMessage());
        }

        return savedUser;
    }

    public String loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email không tồn tại!");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mật khẩu không đúng!");
        }

        if (!user.getEnabled()) {
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa!");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    @Transactional
    public void forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Email không tồn tại trong hệ thống!");
        }

        // Xóa các OTP cũ của email này
        otpTokenRepository.deleteByEmail(email);
        otpTokenRepository.flush(); // Force delete commit

        // Tạo OTP mới
        String otpCode = generateOTP();
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtpCode(otpCode);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5)); // OTP hết hạn sau 5 phút

        otpTokenRepository.save(otpToken);
        otpTokenRepository.flush(); // Force save commit

        // Gửi email
        emailService.sendOtpEmail(email, otpCode);
    }

    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        // Validate password mới trước
        PasswordValidator.validatePassword(newPassword);

        // Kiểm tra OTP
        Optional<OtpToken> otpTokenOpt = otpTokenRepository
            .findByEmailAndOtpCodeAndUsedFalse(email, otpCode);

        if (otpTokenOpt.isEmpty()) {
            throw new RuntimeException("OTP không hợp lệ!");
        }

        OtpToken otpToken = otpTokenOpt.get();

        if (otpToken.isExpired()) {
            throw new RuntimeException("OTP đã hết hạn!");
        }

        // Cập nhật password
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User không tồn tại!");
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        userRepository.flush(); // Force immediate commit

        // Đánh dấu OTP đã sử dụng
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        otpTokenRepository.flush(); // Force immediate commit
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6 digit OTP
        return String.valueOf(otp);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
