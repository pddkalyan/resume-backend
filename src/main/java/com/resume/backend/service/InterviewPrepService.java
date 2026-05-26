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
public class InterviewPrepService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateInterviewPrep(String resumeDataJson, String jobDescription) {
        // The prompt forces a strict JSON schema for easy parsing on the frontend
        String prompt = "You are an expert executive technical recruiter and career coach. " +
                "Analyze the provided Resume and Job Description. " +
                "Generate exactly 10 highly targeted, realistic interview questions the candidate is likely to face for this specific role. " +
                "For EACH question, provide a detailed 'How to Answer' strategy. The strategy MUST explicitly reference past experiences, skills, or projects from the candidate's provided resume to form a compelling response. " +
                "\n\nJob Description:\n" + jobDescription +
                "\n\nResume Data:\n" + resumeDataJson +
                "\n\nYou MUST return ONLY a raw JSON object matching this exact structure, with no markdown formatting, no backticks, and no extra text:\n" +
                "{\n" +
                "  \"qnaList\": [\n" +
                "    {\n" +
                "      \"question\": \"String (The interview question)\",\n" +
                "      \"answerStrategy\": \"String (Detailed advice on how to answer using their specific resume details)\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        // Temperature 0.5 balances analytical fact-checking with conversational coaching
        requestBody.put("generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.5
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String rawResponse = restTemplate.postForObject(apiUrl, entity, String.class);
            JsonNode rootNode = objectMapper.readTree(rawResponse);
            // Extract the actual JSON string from Gemini's response structure
            return rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            System.err.println("Interview Prep Generation Failed: " + e.getMessage());
            throw new RuntimeException("Failed to generate interview prep");
        }
    }
}