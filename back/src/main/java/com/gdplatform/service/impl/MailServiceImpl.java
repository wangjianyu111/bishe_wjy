package com.gdplatform.service.impl;

import com.gdplatform.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendVerificationCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【大学生毕设审批一体化平台】登录验证码");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee;\">" +
                    "<h2 style=\"color: #6366f1; text-align: center;\">大学生毕设审批一体化平台</h2>" +
                    "<div style=\"background: #f9fafb; padding: 20px; border-radius: 8px; text-align: center;\">" +
                    "<h3 style=\"margin: 0 0 15px; color: #1f2937;\">登录验证码</h3>" +
                    "<p style=\"font-size: 16px; margin: 0 0 10px; color: #4b5563;\">您的验证码为：</p>" +
                    "<div style=\"font-size: 32px; font-weight: bold; color: #6366f1; letter-spacing: 8px; margin: 15px 0;\">" + code + "</div>" +
                    "<p style=\"margin: 0; color: #6b7280;\">该验证码将在5分钟内失效，请及时使用。</p>" +
                    "</div>" +
                    "<p style=\"text-align: center; margin-top: 20px; color: #9ca3af; font-size: 12px;\">如果您未申请此验证码，请忽略此邮件。</p>" +
                    "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("验证码邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("发送验证码邮件失败", e);
            throw new RuntimeException("发送验证码邮件失败", e);
        }
    }
}