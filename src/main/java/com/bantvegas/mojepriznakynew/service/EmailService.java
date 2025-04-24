package com.bantvegas.mojepriznakynew.service;

import com.bantvegas.mojepriznakynew.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendDiagnosisAsPdf(String toEmail, User user, String diagnosisText, String timestampFormatted) {
        try {
            System.out.println("📤 Pokus o odoslanie e-mailu...");
            System.out.println("➡️ Komu: " + toEmail);
            System.out.println("🧠 Výsledok AI:\n" + diagnosisText);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("🧠 Diagnóza pacienta z MojePriznaky.sk");

            String body = String.format("""
                Dobrý deň,

                Posielame AI analýzu pacienta:

                Meno: %s %s
                E-mail: %s
                Dátum: %s

                Výsledok AI:
                -----------------------
                %s

                S pozdravom,
                Tím MojePriznaky.sk
                """, user.getFirstName(), user.getLastName(), user.getEmail(), timestampFormatted, diagnosisText);

            helper.setText(body, false);
            mailSender.send(message);

            System.out.println("✅ E-mail bol úspešne odoslaný.");
        } catch (MessagingException e) {
            System.out.println("❌ Chyba pri odosielaní e-mailu:");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("❌ Iná chyba pri odosielaní:");
            e.printStackTrace();
        }
    }
}
