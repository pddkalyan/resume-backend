package com.resume.backend.controller;

import com.resume.backend.service.UserService;
// import com.resume.backend.service.YourAiGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // Match your production CORS setup
public class AiController {

    @Autowired
    private UserService userService;

    // @Autowired
    // private YourAiGenerationService aiGenerationService;

    public static class AiRequest {
        public String jobDescription;
        public String userResumeData;
    }

    @PostMapping("/generate-cover-letter")
    public ResponseEntity<?> generateCoverLetter(@RequestBody AiRequest request, Authentication authentication) {

        // 1. Identify the user making the request
        String userEmail = authentication.getName();

        // 2. THE BOUNCER: Check credits BEFORE running expensive AI tasks
        boolean isAuthorized = userService.consumeAiCredit(userEmail);

        if (!isAuthorized) {
            // HTTP 402 Payment Required is the perfect status code for this
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("error", "Insufficient AI Credits. Please upgrade to PRO or purchase a credit pack."));
        }

        try {
            // 3. EXECUTE AI LOGIC ONLY IF AUTHORIZED
            // String generatedText = aiGenerationService.generate(request.jobDescription, request.userResumeData);

            // Mock response for now
            String generatedText = "Dear Hiring Manager, based on my skills...";

            return ResponseEntity.ok(Map.of("coverLetter", generatedText));

        } catch (Exception e) {
            // If the AI API fails (e.g., OpenAI is down), you might want to refund the credit here!
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "AI Generation failed. Please try again."));
        }
    }
}