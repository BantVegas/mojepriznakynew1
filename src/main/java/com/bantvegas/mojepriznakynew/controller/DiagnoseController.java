package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import com.bantvegas.mojepriznakynew.model.User;
import com.bantvegas.mojepriznakynew.repository.DiagnosisRecordRepository;
import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.service.EmailService;
import com.bantvegas.mojepriznakynew.service.GptVisionService;
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

        System.out.println("🧠 Prístup na /diagnose");
        System.out.println("Používateľ: " + auth.getName());
        System.out.println("Authority: " + auth.getAuthorities());
        System.out.println("📨 Prompt: " + prompt);
        System.out.println("📎 Súbor: " + (file != null ? file.getOriginalFilename() : "null"));

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
            e.printStackTrace(); // výpis na server
            return ResponseEntity.status(500).body(
                    Map.of("result", "❌ Chyba pri AI analýze: " +
                            e.getClass().getSimpleName() + " – " + e.getMessage())
            );
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
            System.out.println("⛔ Neautentifikovaný prístup");
            return ResponseEntity.status(401).body("❌ Neautorizovaný prístup.");
        }

        System.out.println("✅ Diagnóza odosielaná doktorovi");
        System.out.println("➡️ Diagnóza ID z requestu: " + diagnosisId);
        System.out.println("👤 Prihlásený používateľ (auth.getName): " + auth.getName());

        String userEmail = auth.getName();
        User user = userRepository.findByEmail(userEmail).orElse(null);
        if (user == null) {
            System.out.println("❌ Používateľ nenájdený v DB");
            return ResponseEntity.status(404).body("❌ Používateľ neexistuje.");
        }

        System.out.println("✅ Používateľ z DB: " + user.getEmail() + " (ID: " + user.getId() + ")");

        DiagnosisRecord record = diagnosisRecordRepository.findById(diagnosisId).orElse(null);
        if (record == null) {
            System.out.println("❌ Diagnóza s ID " + diagnosisId + " neexistuje v databáze");
            return ResponseEntity.status(404).body("❌ Diagnóza neexistuje.");
        }

        System.out.println("📋 Diagnóza nájdená: ID " + record.getId());
        System.out.println("📧 Diagnóza patrí používateľovi: " + record.getUser().getEmail() + " (ID: " + record.getUser().getId() + ")");
        System.out.println("✔️ Zhoduje sa s aktuálnym používateľom? " + record.getUser().getId().equals(user.getId()));

        if (!record.getUser().getId().equals(user.getId())) {
            System.out.println("❌ Diagnóza nepatrí aktuálnemu používateľovi");
            return ResponseEntity.status(403).body("❌ Prístup odmietnutý.");
        }

        String formattedDate = record.getTimestamp().format(DateTimeFormatter.ofPattern("d.M.yyyy HH:mm"));

        emailService.sendDiagnosisAsPdf(
                doctorEmail,
                user,
                record.getResult(),
                formattedDate
        );

        return ResponseEntity.ok("✅ E-mail bol úspešne odoslaný doktorovi.");
    }
}
