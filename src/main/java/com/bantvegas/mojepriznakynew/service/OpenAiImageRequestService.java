package com.bantvegas.mojepriznakynew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAiImageRequestService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String analyzeWithImage(byte[] imageData, String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        String base64Image = Base64.getEncoder().encodeToString(imageData);
        Map<String, Object> imageUrl = Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/jpeg;base64," + base64Image)
        );

        // 🇸🇰 System prompt pre slovenskú odpoveď
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "Si zdravotný asistent. Analyzuj obrázok a odpovedaj výhradne po slovensky."
        );

        // 👤 Používateľský vstup = prompt + obrázok
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", List.of(prompt, imageUrl)
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(systemMessage, userMessage),
                "max_tokens", 1000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, entity, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> messageContent = (Map<String, Object>) choices.get(0).get("message");
                return messageContent.get("content").toString();
            }

            return "❌ AI nevrátila žiadnu odpoveď.";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Chyba pri volaní AI: " + e.getMessage();
        }
    }
}
