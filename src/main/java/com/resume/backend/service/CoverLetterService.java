package com.resume.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CoverLetterService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateCoverLetter(String resumeDataJson, String jobDescription) {
        String prompt = "You are an expert executive recruiter and copywriter. " +
                "Write a highly tailored, compelling, and professional cover letter based on the provided Resume and Job Description. " +
                "Do not use generic placeholders like [Company Name] if the information is available in the JD. " +
                "Keep it to 3-4 impactful paragraphs.\n\n" +
                "Job Description:\n" + jobDescription + "\n\n" +
                "Resume Data:\n" + resumeDataJson + "\n\n" +
                "You MUST return ONLY a raw JSON object with exactly one key:\n" +
                "- coverLetter (string containing the formatted cover letter text)";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        // Notice the temperature is 0.7 here for CREATIVITY
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.7
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String rawResponse = restTemplate.postForObject(apiUrl, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            System.err.println("Cover Letter Generation Failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate cover letter");
        }
    }
}