package com.bantvegas.mojepriznakynew.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class OcrService {

    private final String OCR_API_URL = "https://api.ocr.space/parse/image";
    private final String API_KEY = "helloworld"; // 🔑 nahraď vlastným

    public String extractText(MultipartFile file) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("apikey", API_KEY);

        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
        body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("language", "slk");

        HttpEntity<?> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(OCR_API_URL, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("OCR API zlyhalo: " + response.getStatusCode());
        }

        var parsedResults = (java.util.List<Map<String, Object>>) response.getBody().get("ParsedResults");
        if (parsedResults == null || parsedResults.isEmpty()) {
            return "";
        }

        return (String) parsedResults.get(0).get("ParsedText");
    }
}
