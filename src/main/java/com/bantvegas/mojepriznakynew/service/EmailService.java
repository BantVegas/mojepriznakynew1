package com.bantvegas.mojepriznakynew.service;

import com.bantvegas.mojepriznakynew.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
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
            System.out.println("📧 Príprava e-mailu...");
            System.out.println("➡️ Komu: " + toEmail);
            System.out.println("➡️ Od: " + user.getEmail());
            System.out.println("➡️ Meno: " + user.getFirstName() + " " + user.getLastName());
            System.out.println("➡️ Čas: " + timestampFormatted);
            System.out.println("📝 Text AI výstupu:\n" + diagnosisText);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setTo(toEmail);
            helper.setSubject("🧠 Diagnóza pacienta z MojePriznaky.sk");

            String body = String.format("""
                Dobrý deň,

                zasielame AI analýzu pacienta:
                Meno: %s %s
                E-mail: %s
                Dátum: %s

                Výsledok je priložený ako PDF.

                S pozdravom,
                Tím MojePriznaky.sk
                """, user.getFirstName(), user.getLastName(), user.getEmail(), timestampFormatted);

            helper.setText(body, false);

            byte[] pdfContent = generateSimplePdf(diagnosisText);
            helper.addAttachment("vysledok.pdf", new ByteArrayResource(pdfContent));

            mailSender.send(message);
            System.out.println("✅ E-mail bol úspešne odoslaný.");
        } catch (MessagingException e) {
            System.out.println("❌ Chyba pri odosielaní e-mailu:");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("❌ Všeobecná chyba:");
            e.printStackTrace();
        }
    }

    private byte[] generateSimplePdf(String content) {
        String fakePdf = """
                %PDF-1.4
                1 0 obj
                << /Type /Catalog /Pages 2 0 R >>
                endobj
                2 0 obj
                << /Type /Pages /Kids [3 0 R] /Count 1 >>
                endobj
                3 0 obj
                << /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792]
                   /Contents 4 0 R /Resources << >> >>
                endobj
                4 0 obj
                << /Length 55 >>
                stream
                BT
                /F1 24 Tf
                100 700 Td
                (%s) Tj
                ET
                endstream
                endobj
                xref
                0 5
                0000000000 65535 f 
                0000000010 00000 n 
                0000000079 00000 n 
                0000000178 00000 n 
                0000000375 00000 n 
                trailer
                << /Root 1 0 R /Size 5 >>
                startxref
                520
                %%EOF
                """.formatted(content.replaceAll("[\\r\\n]+", " "));

        return fakePdf.getBytes(StandardCharsets.US_ASCII);
    }
}
