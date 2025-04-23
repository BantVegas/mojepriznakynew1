package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import com.bantvegas.mojepriznakynew.model.User;
import com.bantvegas.mojepriznakynew.repository.DiagnosisRecordRepository;
import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.service.GptVisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiagnoseController {

    private final GptVisionService gptVisionService;
    private final UserRepository userRepository;
    private final DiagnosisRecordRepository diagnosisRecordRepository;

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

}