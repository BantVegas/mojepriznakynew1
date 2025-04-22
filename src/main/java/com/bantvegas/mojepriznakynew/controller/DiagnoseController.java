package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.service.GptVisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiagnoseController {

    private final GptVisionService gptVisionService;

    @PostMapping("/diagnose")
    public ResponseEntity<?> analyze(
            @RequestParam("prompt") String prompt,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        try {
            String result = gptVisionService.analyze(prompt, file);
            return ResponseEntity.ok(Map.of("result", result));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("result", "❌ Chyba pri AI analýze: " + e.getMessage()));
        }
    }
}
