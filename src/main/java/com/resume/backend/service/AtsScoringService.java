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
public class AtsScoringService {

    // Now reading securely from your application.yml / environment variables
    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateAtsScore(String resumeDataJson, String jobDescription) {
        String prompt = "You are an elite enterprise Applicant Tracking System (ATS). " +
                "Analyze the provided Resume JSON against the provided Job Description. " +
                "Evaluate semantic matches, not just exact keyword strings.\n\n" +
                "Job Description:\n" + jobDescription + "\n\n" +
                "Resume Data:\n" + resumeDataJson + "\n\n" +
                "You MUST return ONLY a raw JSON object with exactly these keys:\n" +
                "- matchScore (integer 0-100)\n" +
                "- missingHardSkills (array of strings)\n" +
                "- semanticAnalysis (a short string evaluating their architectural fit)\n" +
                "- actionableSteps (array of 2-3 specific strings on what to improve)";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(
                Map.of("parts", List.of(
                        Map.of("text", prompt)
                ))
        ));

        // Force strict JSON AND force deterministic, non-creative scoring
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.0
        ));

        // Injecting the key securely via HTTP Headers (The Google fix)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            // Notice we use the raw apiUrl without appending ?key=
            String rawResponse = restTemplate.postForObject(apiUrl, entity, String.class);

            JsonNode rootNode = objectMapper.readTree(rawResponse);
            JsonNode textNode = rootNode.path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");

            return textNode.asText();

        } catch (Exception e) {
            System.err.println("ATS Scoring Failed: " + e.getMessage());
            throw new RuntimeException("Failed to analyze resume against JD");
        }
    }
}