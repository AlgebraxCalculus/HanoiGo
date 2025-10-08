package com.example.hanoiGo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AsyncEmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendMail(SimpleMailMessage message) {
        mailSender.send(message);
    }
}
