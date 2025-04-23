package com.bantvegas.mojepriznakynew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String askChatGPT(String prompt) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "Si zdravotný asistent. Vždy odpovedaj výhradne po slovensky."
        );

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", prompt
        );

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(systemMessage, userMessage),
                "max_tokens", 500
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
            return "❌ AI nevrátila odpoveď.";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Chyba pri volaní AI: " + e.getMessage();
        }
    }
}
