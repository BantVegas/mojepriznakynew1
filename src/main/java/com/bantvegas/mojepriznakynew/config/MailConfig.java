package com.bantvegas.mojepriznakynew.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender mailSender(
            @Value("${SENDGRID_SMTP_USERNAME}") String username,
            @Value("${SENDGRID_SMTP_PASSWORD}") String password
    ) {
        System.out.println("✅ Konfigurujem JavaMailSender pre SendGrid používateľa: " + username);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("smtp.sendgrid.net");
        sender.setPort(587); // STARTTLS port
        sender.setUsername(username); // musí byť "apikey"
        sender.setPassword(password); // je to tvoj API Key

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "true");

        return sender;
    }
}
