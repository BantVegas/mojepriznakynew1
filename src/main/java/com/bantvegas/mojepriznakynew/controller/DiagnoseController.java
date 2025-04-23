package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import com.bantvegas.mojepriznakynew.model.User;
import com.bantvegas.mojepriznakynew.repository.DiagnosisRecordRepository;
import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.service.EmailService;
import com.bantvegas.mojepriznakynew.service.GptVisionService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiagnoseController {

    private final GptVisionService gptVisionService;
    private final UserRepository userRepository;
    private final DiagnosisRecordRepository diagnosisRecordRepository;
    private final EmailService emailService;

    @PostMapping("/diagnose")
    public ResponseEntity<?> diagnose(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("result", "❌ Neautorizovaný prístup"));
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("result", "❌ Používateľ neexistuje"));
        }

        try {
            String result = gptVisionService.analyze(prompt, file);

            DiagnosisRecord record = DiagnosisRecord.builder()
                    .prompt(prompt)
                    .result(result)
                    .timestamp(LocalDateTime.now())
                    .user(user)
                    .build();

            diagnosisRecordRepository.save(record);

            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("result", "❌ Chyba pri AI analýze"));
        }
    }

    @GetMapping("/diagnose/history")
    public ResponseEntity<?> getHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "❌ Neautorizovaný prístup"));
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "❌ Používateľ neexistuje"));
        }

        List<DiagnosisRecord> history = diagnosisRecordRepository.findByUserOrderByTimestampDesc(user);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/diagnose/send")
    public ResponseEntity<?> sendDiagnosisToDoctor(
            @RequestParam("id") Long diagnosisId,
            @RequestParam("email") String doctorEmail
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("❌ Neautorizovaný prístup.");
        }

        String userEmail = auth.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body("❌ Používateľ neexistuje.");
        }

        DiagnosisRecord record = diagnosisRecordRepository.findById(diagnosisId).orElse(null);
        if (record == null || !record.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("❌ Prístup odmietnutý.");
        }

        try {
            String formattedDate = record.getTimestamp().format(DateTimeFormatter.ofPattern("d.M.yyyy HH:mm"));

            emailService.sendDiagnosisAsPdf(
                    doctorEmail,
                    user,
                    record.getResult(),
                    formattedDate
            );

            return ResponseEntity.ok("✅ E-mail bol úspešne odoslaný doktorovi.");
        } catch (MessagingException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("❌ Nepodarilo sa odoslať e-mail.");
        }
    }
}
