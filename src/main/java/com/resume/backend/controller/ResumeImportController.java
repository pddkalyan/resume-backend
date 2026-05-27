package com.resume.backend.controller;

import com.resume.backend.service.ExtractionService;
import com.resume.backend.service.CloneTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeImportController {

    private final ExtractionService extractionService;
    private final CloneTemplateService cloneTemplateService; // <-- NEW

    @Autowired
    public ResumeImportController(ExtractionService extractionService, CloneTemplateService cloneTemplateService) {
        this.extractionService = extractionService;
        this.cloneTemplateService = cloneTemplateService; // <-- NEW
    }

    // Existing auto-fill endpoint
    @PostMapping("/extract")
    public ResponseEntity<String> extractResumeData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"File upload payload is empty.\"}");
        }

        try {
            String extractedJson = extractionService.extractResumeData(file);
            return ResponseEntity.ok(extractedJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"AI data parsing failed: " + e.getMessage() + "\"}");
        }
    }

    // --- NEW: The AI Template Cloner Endpoint ---
    @PostMapping("/clone-design")
    public ResponseEntity<?> cloneResumeDesign(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"File upload payload is empty.\"}");
        }

        try {
            Map<String, Object> safeTemplateData = cloneTemplateService.cloneDesign(file);
            // Returns the JSON object containing name, domains, and the sanitized HTML string
            return ResponseEntity.ok(safeTemplateData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"AI design cloning failed: " + e.getMessage() + "\"}");
        }
    }
}