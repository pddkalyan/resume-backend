package com.resume.backend.controller;

import com.resume.backend.service.ExtractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeImportController {

    private final ExtractionService extractionService;

    @Autowired
    public ResumeImportController(ExtractionService extractionService) {
        this.extractionService = extractionService;
    }

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
}