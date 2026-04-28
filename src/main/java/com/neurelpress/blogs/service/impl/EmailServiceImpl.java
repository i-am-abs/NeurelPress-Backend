package com.neurelpress.blogs.service.impl;

import com.neurelpress.blogs.dto.properties.NeuralPressCorsProperties;
import com.neurelpress.blogs.dto.properties.NeuralPressMailProperties;
import com.neurelpress.blogs.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final NeuralPressMailProperties mailProperties;
    private final NeuralPressCorsProperties corsProperties;

    @Override
    @Async
    public void sendVerificationEmail(String toEmail, String username, String token) {
        String verifyUrl = corsProperties.app().primaryFrontendUrl() + "/auth/verify-email?token=" + token;
        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: system-ui, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px;">
                    <h2 style="color: #111827;">Verify your NeuralPress account</h2>
                    <p>Hi %s,</p>
                    <p>Thanks for signing up. Click the button below to verify your email address.</p>
                    <p><a href="%s" style="display: inline-block; padding: 12px 24px; background: #3B82F6; color: white; text-decoration: none; border-radius: 8px;">Verify Email</a></p>
                    <p>Or copy this link: <a href="%s">%s</a></p>
                    <p style="color: #6b7280; font-size: 14px;">This link expires in 24 hours.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">&copy; NeuralPress</p>
                </body>
                </html>
                """.formatted(username, verifyUrl, verifyUrl, verifyUrl);
        sendHtml(toEmail, "Verify your NeuralPress email", html);
        log.info("Verification email sent to {} for user {}", toEmail, username);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String username, String token) {
        String resetUrl = corsProperties.app().primaryFrontendUrl() + "/auth/reset-password?token=" + token;
        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: system-ui, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px;">
                    <h2 style="color: #111827;">Reset your password</h2>
                    <p>Hi %s,</p>
                    <p>Click the button below to set a new password.</p>
                    <p><a href="%s" style="display: inline-block; padding: 12px 24px; background: #3B82F6; color: white; text-decoration: none; border-radius: 8px;">Reset Password</a></p>
                    <p style="color: #6b7280; font-size: 14px;">This link expires in 1 hour.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">&copy; NeuralPress</p>
                </body>
                </html>
                """.formatted(username, resetUrl);
        sendHtml(toEmail, "Reset your NeuralPress password", html);
        log.info("Password reset email sent to {} for user {}", toEmail, username);
    }

    @Override
    @Async
    public void sendLoginOtpEmail(String toEmail, String username, String otpCode) {
        String html = """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body style="font-family: system-ui, sans-serif; max-width: 560px; margin: 0 auto; padding: 24px;">
                    <h2 style="color: #111827;">Your NeuralPress login code</h2>
                    <p>Hi %s,</p>
                    <p>Use the 6-digit code below to verify your email and sign in. This code expires in 10 minutes.</p>
                    <p style="font-size: 32px; font-weight: bold; letter-spacing: 0.3em;">%s</p>
                    <p style="color: #6b7280; font-size: 14px;">If you didn't request this, you can safely ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;">
                    <p style="color: #9ca3af; font-size: 12px;">&copy; NeuralPress</p>
                </body>
                </html>
                """.formatted(username, otpCode);
        sendHtml(toEmail, "Your NeuralPress login code", html);
        log.info("Login OTP email sent to {} for user {}", toEmail, username);
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailProperties.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.debug("Email sent to {}", to);
        } catch (MessagingException | MailException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
