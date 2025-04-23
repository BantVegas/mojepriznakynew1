package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import com.bantvegas.mojepriznakynew.repository.DiagnosisRecordRepository;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/diagnose")
@RequiredArgsConstructor
public class DoctorSendController {

    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @PostMapping("/send")
    public ResponseEntity<?> sendToDoctor(@RequestBody Map<String, Object> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Neautorizovaný prístup"));
        }

        Long diagnosisId = Long.valueOf(payload.get("diagnosisId").toString());
        String doctorEmail = payload.get("doctorEmail").toString();

        DiagnosisRecord record = diagnosisRecordRepository.findById(diagnosisId).orElse(null);
        if (record == null) {
            return ResponseEntity.status(404).body(Map.of("error", "❌ Diagnóza neexistuje"));
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(doctorEmail);
            helper.setSubject("🧠 AI Diagnóza pacienta");
            helper.setFrom(new InternetAddress(from, "MojePriznaky.sk"));
            helper.setText("Analýza pacienta:\n\n" + record.getResult(), false);

            mailSender.send(message);

            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "❌ Chyba pri odosielaní e-mailu"));
        }
    }
}
