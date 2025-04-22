package com.bantvegas.mojepriznakynew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiImageRequestService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String analyzeWithImage(byte[] imageData, String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> imageContent = Map.of(
                "type", "image_url",
                "image_url", Map.of("url", "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(imageData))
        );

        Map<String, Object> message = Map.of(
                "role", "user",
                "content", List.of(prompt, imageContent)
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4-vision-preview",
                "messages", List.of(message),
                "max_tokens", 1000
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(OPENAI_URL, HttpMethod.POST, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");

        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> messageContent = (Map<String, Object>) choices.get(0).get("message");
            return messageContent.get("content").toString();
        }

        return "❌ AI nevrátila žiadnu odpoveď.";
    }
}
