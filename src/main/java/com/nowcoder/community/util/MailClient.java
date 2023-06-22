package com.nowcoder.community.util;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class MailClient {
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from; // 发送者

    public void sendMail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage(); // 创建邮件
            MimeMessageHelper helper = new MimeMessageHelper(message); // 创建邮件助手
            helper.setFrom(from); // 设置发送者
            helper.setTo(to); // 设置接收者
            helper.setSubject(subject); // 设置主题
            helper.setText(content, true); // 设置内容，true表示支持html
            mailSender.send(helper.getMimeMessage()); // 发送邮件
        } catch (Exception e) {
            logger.error("发送邮件失败：" + e.getMessage());
        }
    }
}
