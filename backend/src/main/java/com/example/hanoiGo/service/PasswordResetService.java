package com.example.hanoiGo.service;

import com.example.hanoiGo.dto.request.ForgotPasswordRequest;
import com.example.hanoiGo.dto.request.ResetPasswordRequest;
import com.example.hanoiGo.dto.request.VerifyOtpRequest;
import com.example.hanoiGo.exception.AppException;
import com.example.hanoiGo.exception.ErrorCode;
import com.example.hanoiGo.model.PasswordResetToken;
import com.example.hanoiGo.model.User;
import com.example.hanoiGo.repository.PasswordResetTokenRepository;
import com.example.hanoiGo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // send OTP
    public void sendOtp(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String otp = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken token = tokenRepository.findByUser(user);
        if(token == null) {
            token = new PasswordResetToken();
            token.setUser(user);
        }
        token.setToken(otp);
        token.setExpiryDate(Date.from(Instant.now().plusSeconds(5 * 60)));
        tokenRepository.save(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("hanoigoptit@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("HanoiGo - Password Reset OTP");
        message.setText("Xin chào " + user.getUsername() +
                ",\n\nMã OTP đặt lại mật khẩu của bạn là: " + otp +
                "\nOTP này có hiệu lực trong 5 phút.\n\nHanoiGo Team");
        mailSender.send(message);
    }

    // verify OTP
    public void verifyOtp(VerifyOtpRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.getOtp());
        if(token == null || !token.getToken().equals(request.getOtp())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        if(token.getExpiryDate().before(new Date())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        if (token.getExpiryDate().before(new Date())) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        token.setVerified(true);
        tokenRepository.save(token);
    }

    // reset password
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = tokenRepository.findFirstByVerifiedTrueOrderByExpiryDateDesc();
        if(token == null) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        tokenRepository.delete(token);
    }
}