package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.dto.FreeformRequest;
import com.bantvegas.mojepriznakynew.enums.SubscriptionTier;
import com.bantvegas.mojepriznakynew.model.DiagnosisRecord;
import com.bantvegas.mojepriznakynew.model.User;
import com.bantvegas.mojepriznakynew.repository.DiagnosisRecordRepository;
import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FreeformController {

    private final UserRepository userRepository;
    private final OpenAiService openAiService;
    private final DiagnosisRecordRepository diagnosisRecordRepository;

    @PostMapping("/freeform")
    public ResponseEntity<?> handleFreeform(@RequestBody FreeformRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("result", "❌ Neautorizovaný prístup"));
        }

        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("result", "❌ Používateľ neexistuje"));
        }

        if (user.getSubscriptionTier() == SubscriptionTier.PACIENT && user.getAiUsageCount() >= 2) {
            return ResponseEntity.status(403).body(Map.of("result", "❌ Limit 2 analýz bol vyčerpaný"));
        }

        try {
            String aiAnswer = openAiService.askChatGPT(request.getPrompt());

            user.setAiUsageCount(user.getAiUsageCount() + 1);
            userRepository.save(user);

            DiagnosisRecord record = DiagnosisRecord.builder()
                    .prompt(request.getPrompt())
                    .result(aiAnswer)
                    .timestamp(LocalDateTime.now())
                    .user(user)
                    .build();

            diagnosisRecordRepository.save(record);

            return ResponseEntity.ok(Map.of("result", aiAnswer));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("result", "❌ Chyba pri AI spracovaní"));
        }
    }
}