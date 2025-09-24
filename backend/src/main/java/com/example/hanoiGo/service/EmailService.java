package com.example.hanoiGo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("thenamdivine@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Mã OTP Reset Mật Khẩu - HanoiGo");
            message.setText(buildOtpEmailContent(otpCode));

            emailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Gửi email thất bại: " + e.getMessage());
        }
    }

    private String buildOtpEmailContent(String otpCode) {
        return "Xin chào,\n\n" +
               "Bạn đã yêu cầu reset mật khẩu cho tài khoản HanoiGo.\n\n" +
               "Mã OTP của bạn là: " + otpCode + "\n\n" +
               "Mã này sẽ hết hạn sau 5 phút.\n\n" +
               "Nếu bạn không yêu cầu reset mật khẩu, vui lòng bỏ qua email này.\n\n" +
               "Trân trọng,\n" +
               "Đội ngũ HanoiGo";
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("thenamdivine@gmail.com");
            message.setTo(toEmail);
            message.setSubject("Chào mừng bạn đến với HanoiGo!");
            message.setText(buildWelcomeEmailContent(username));

            emailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi welcome email thất bại: " + e.getMessage());
        }
    }

    private String buildWelcomeEmailContent(String username) {
        return "Xin chào " + username + ",\n\n" +
               "Chào mừng bạn đến với HanoiGo!\n\n" +
               "Tài khoản của bạn đã được tạo thành công.\n\n" +
               "Hãy khám phá các tính năng tuyệt vời mà chúng tôi mang lại!\n\n" +
               "Trân trọng,\n" +
               "Đội ngũ HanoiGo";
    }
}