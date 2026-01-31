package com.aos.fitness_app.auth.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.name}")
    private String appName;


    //Todo: Adjust email source and design
    public void sendOtpEmail(String email, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Password Reset OTP - " + appName);
            helper.setText(otp, true);
            helper.setFrom("noreply@fitnessapp.com");

            mailSender.send(message);
            log.info("OTP email sent successfully to: " + email);

        } catch (jakarta.mail.MessagingException e) {
            log.error("Failed to send OTP email to: " + email);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

}
