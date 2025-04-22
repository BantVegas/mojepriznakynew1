package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("text", "❌ Súbor nebol priložený."));
        }

        String text = ocrService.extractText(file);
        return ResponseEntity.ok(Map.of("text", text));
    }
}
