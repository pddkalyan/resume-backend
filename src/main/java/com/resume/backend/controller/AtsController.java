package com.resume.backend.controller;

import com.resume.backend.model.User;
import com.resume.backend.repository.UserRepository;
import com.resume.backend.security.JwtUtil;
import com.resume.backend.service.AtsScoringService;
import com.resume.backend.service.CoverLetterService;
import com.resume.backend.service.InterviewPrepService;
import com.resume.backend.service.UserService; // <-- ADDED
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ats")
public class AtsController {

    @Autowired
    private AtsScoringService atsScoringService;

    @Autowired
    private CoverLetterService coverLetterService;

    @Autowired
    private InterviewPrepService interviewPrepService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // <-- ADDED to check AI Credits

    // --- SECURITY HELPER ---
    private boolean isUserPro(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String email = jwtUtil.extractEmail(jwt);
                return userRepository.findByEmail(email).map(User::isPro).orElse(false);
            }
        } catch (Exception e) {
            System.out.println("Error decoding JWT for Pro check: " + e.getMessage());
        }
        return false;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(@RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {

        // --- PAYWALL CHECK ---
        if (!isUserPro(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "PREMIUM_REQUIRED", "message", "The ATS Scanner is a Pro feature."));
        }

        String resumeJson = payload.get("resumeData");
        String jobDescription = payload.get("jobDescription");

        if (resumeJson == null || jobDescription == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Both resumeData and jobDescription are required."));
        }

        String analysisResultJson = atsScoringService.generateAtsScore(resumeJson, jobDescription);
        return ResponseEntity.ok(analysisResultJson);
    }

    // <-- UPDATED: Added Authorization Header to get the user's token
    @PostMapping("/cover-letter")
    public ResponseEntity<?> generateCoverLetter(@RequestBody Map<String, String> payload, @RequestHeader("Authorization") String token) {

        // 1. Identify the user
        String email = "";
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            email = jwtUtil.extractEmail(jwt);
        }

        // 2. THE BOUNCER: Check and consume 1 AI Credit
        if (!userService.consumeAiCredit(email)) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("error", "Insufficient AI Credits. Please upgrade to PRO or buy credits."));
        }

        String resumeJson = payload.get("resumeData");
        String jobDescription = payload.get("jobDescription");

        if (resumeJson == null || jobDescription == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing data"));
        }

        String coverLetterJson = coverLetterService.generateCoverLetter(resumeJson, jobDescription);
        return ResponseEntity.ok(coverLetterJson);
    }

    @PostMapping("/interview-prep")
    public ResponseEntity<?> generateInterviewPrep(@RequestBody Map<String, String> payload) {
        String resumeJson = payload.get("resumeData");
        String jobDescription = payload.get("jobDescription");

        if (resumeJson == null || jobDescription == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing resumeData or jobDescription"));
        }

        try {
            String prepDataJson = interviewPrepService.generateInterviewPrep(resumeJson, jobDescription);
            return ResponseEntity.ok(prepDataJson);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}