package com.bantvegas.mojepriznakynew.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptVisionService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyze(String prompt, MultipartFile file) throws Exception {
        System.out.println("🧠 Spúšťam GPT-4o analýzu");
        System.out.println("📨 Prompt: " + prompt);
        System.out.println("📎 Súbor: " + (file != null ? file.getOriginalFilename() : "null"));
        System.out.println("🔑 API Key: " + (apiKey != null ? "OK" : "NULL ❌"));

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("❌ OPENAI_API_KEY nie je nastavený.");
        }

        String base64Image = null;
        if (file != null && !file.isEmpty()) {
            byte[] bytes = file.getBytes();
            base64Image = Base64.getEncoder().encodeToString(bytes);
        }

        // 🧠 Systémová správa
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "Si zdravotný asistent. Odpovedaj výhradne po slovensky."
        );

        // 👤 Užívateľská správa
        List<Map<String, Object>> userContent = new ArrayList<>();
        if (prompt != null && !prompt.isBlank()) {
            userContent.add(Map.of("type", "text", "text", prompt));
        }
        if (base64Image != null) {
            userContent.add(Map.of(
                    "type", "image_url",
                    "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image)
            ));
        }

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", userContent
        );

        // 🔁 Celý request
        Map<String, Object> request = Map.of(
                "model", "gpt-4o",
                "messages", List.of(systemMessage, userMessage),
                "max_tokens", 1000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions", entity, Map.class
            );

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                return msg.get("content").toString();
            }

            return "❌ AI neodpovedala.";
        } catch (Exception e) {
            System.out.println("❌ Výnimka pri volaní OpenAI API:");
            e.printStackTrace();
            throw e;
        }
    }
}

