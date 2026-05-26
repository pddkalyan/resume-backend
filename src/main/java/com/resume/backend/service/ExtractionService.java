package com.resume.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ExtractionService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // --- NEW: Inject the URL from application.yaml ---
    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractResumeData(MultipartFile file) throws IOException {
        // --- NEW: Combine the URL and the Key dynamically ---
        String url = apiUrl + "?key=" + apiKey;

        // Convert file bytes to Base64 format for Gemini inline data
        String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
        String mimeType = file.getContentType();

        // Strict system prompt forcing a specific JSON structure
        String systemPrompt = "You are an expert ATS data extraction system. Analyze the provided resume file or screenshot. " +
                "Extract all professional information and return it strictly as a single JSON object matching this schema. " +
                "Do not wrap the response in markdown blocks like ```json. Return raw JSON text only.\n\n" +
                "Schema:\n" +
                "{\n" +
                "  \"personalInfo\": { \"fullName\": \"\", \"email\": \"\", \"phone\": \"\", \"linkedInUrl\": \"\", \"githubUrl\": \"\" },\n" +
                "  \"skills\": \"Comma separated string of skills\",\n" +
                "  \"experience\": [ { \"company\": \"\", \"role\": \"\", \"duration\": \"\", \"description\": \"\" } ],\n" +
                "  \"education\": [ { \"institution\": \"\", \"degree\": \"\", \"graduationYear\": \"\", \"gpa\": \"\" } ],\n" +
                "  \"projects\": [ { \"title\": \"\", \"technologiesUsed\": \"\", \"link\": \"\", \"description\": \"\" } ]\n" +
                "}";

        // Build the raw JSON payload body for the Gemini API
        Map<String, Object> payload = new HashMap<>();

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", systemPrompt);

        Map<String, Object> filePart = new HashMap<>();
        Map<String, String> inlineData = new HashMap<>();
        inlineData.put("mimeType", mimeType);
        inlineData.put("data", base64Data);
        filePart.put("inlineData", inlineData);

        Map<String, Object> partsMap = new HashMap<>();
        partsMap.put("parts", Arrays.asList(textPart, filePart));

        payload.put("contents", Collections.singletonList(partsMap));

        // Add generation config to enforce a JSON response structure
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("responseMimeType", "application/json");
        payload.put("generationConfig", generationConfig);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return parseGeminiResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Gemini parsing failed during dynamic extraction: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private String parseGeminiResponse(Map<String, Object> responseBody) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            throw new RuntimeException("Failed to navigate Gemini structural payload");
        }
    }
}