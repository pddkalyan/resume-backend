package com.resume.backend.controller;

import com.resume.backend.model.User;
import com.resume.backend.model.document.Resume;
import com.resume.backend.repository.ResumeRepository;
import com.resume.backend.repository.UserRepository;
import com.resume.backend.security.JwtUtil;
import com.resume.backend.service.ExtractionService;
import com.resume.backend.service.CloneTemplateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeRepository resumeRepository;
    private final JwtUtil jwtUtil;
    private final ExtractionService extractionService;
    private final CloneTemplateService cloneTemplateService;
    private final UserRepository userRepository;

    @Autowired
    public ResumeController(ResumeRepository resumeRepository,
                            JwtUtil jwtUtil,
                            ExtractionService extractionService,
                            CloneTemplateService cloneTemplateService,
                            UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.jwtUtil = jwtUtil;
        this.extractionService = extractionService;
        this.cloneTemplateService = cloneTemplateService;
        this.userRepository = userRepository;
    }

    // --- FIX: Bulletproof Paywall Check using Explicit JWT parsing ---
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

    // ==========================================
    // 1. AI IMPORT & CLONING LOGIC (Paywalled)
    // ==========================================

    @PostMapping("/extract")
    public ResponseEntity<?> extractResumeData(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        if (!isUserPro(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "PREMIUM_REQUIRED", "message", "The Magic AI Data Assistant is a Pro feature."));
        }

        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "File upload payload is empty."));

        try {
            String extractedJson = extractionService.extractResumeData(file);
            return ResponseEntity.ok(extractedJson);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "AI data parsing failed."));
        }
    }

    @PostMapping("/clone-design")
    public ResponseEntity<?> cloneDesign(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        if (!isUserPro(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "PREMIUM_REQUIRED", "message", "AI Design Cloning is a Pro feature."));
        }

        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "File upload payload is empty."));

        try {
            Map<String, Object> safeTemplateData = cloneTemplateService.cloneDesign(file);
            return ResponseEntity.ok(safeTemplateData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "AI design cloning failed."));
        }
    }

    // ==========================================
    // 2. STANDARD CRUD OPERATIONS (Free)
    // ==========================================

    @PostMapping
    public ResponseEntity<Resume> saveResume(@RequestBody Resume resume, Authentication authentication) {
        String userEmail = authentication.getName();
        resume.setUserEmail(userEmail);
        Resume savedResume = resumeRepository.save(resume);
        return ResponseEntity.ok(savedResume);
    }

    @GetMapping
    public ResponseEntity<List<Resume>> getMyResumes(Authentication authentication) {
        String userEmail = authentication.getName();
        List<Resume> myResumes = resumeRepository.findByUserEmail(userEmail);
        return ResponseEntity.ok(myResumes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getResumeById(@PathVariable String id, @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String userEmail = jwtUtil.extractEmail(jwt);
            Optional<Resume> resumeOpt = resumeRepository.findById(id);

            if (resumeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resume not found");
            }

            Resume resume = resumeOpt.get();
            if (!resume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching resume");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateResume(@PathVariable String id, @RequestBody Resume updatedResume, @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String userEmail = jwtUtil.extractEmail(jwt);
            Optional<Resume> existingResumeOpt = resumeRepository.findById(id);
            if (existingResumeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resume not found");
            }

            Resume existingResume = existingResumeOpt.get();
            if (!existingResume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to edit this resume");
            }

            updatedResume.setId(id);
            updatedResume.setUserEmail(userEmail);
            Resume savedResume = resumeRepository.save(updatedResume);
            return ResponseEntity.ok(savedResume);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating resume: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable String id, @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String userEmail = jwtUtil.extractEmail(jwt);
            Optional<Resume> resumeOpt = resumeRepository.findById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resume not found");
            }

            Resume resume = resumeOpt.get();
            if (!resume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this resume");
            }

            resumeRepository.deleteById(id);
            return ResponseEntity.ok("Resume deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting resume: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. PUBLIC SHARING LOGIC
    // ==========================================

    @PutMapping("/{id}/share")
    public ResponseEntity<?> toggleShareStatus(@PathVariable String id, @RequestBody Map<String, Boolean> payload, @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.substring(7);
            String userEmail = jwtUtil.extractEmail(jwt);
            Optional<Resume> optionalResume = resumeRepository.findById(id);

            if (optionalResume.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Resume resume = optionalResume.get();

            if (!resume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to share this resume"));
            }

            boolean makePublic = payload.getOrDefault("isPublic", false);
            resume.setPublic(makePublic);

            if (makePublic && (resume.getShareCode() == null || resume.getShareCode().isEmpty())) {
                String uniqueCode = UUID.randomUUID().toString().substring(0, 8);
                resume.setShareCode(uniqueCode);
            }

            resumeRepository.save(resume);

            Map<String, Object> response = new HashMap<>();
            response.put("isPublic", resume.isPublic());
            response.put("shareCode", resume.getShareCode());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Error updating sharing status: " + e.getMessage()));
        }
    }

    @GetMapping("/public/{shareCode}")
    public ResponseEntity<?> getPublicResume(@PathVariable String shareCode) {
        Optional<Resume> optionalResume = resumeRepository.findByShareCode(shareCode);

        if (optionalResume.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Resume not found or invalid link."));
        }

        Resume resume = optionalResume.get();

        if (!resume.isPublic()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "This resume is no longer public."));
        }

        resume.setViewCount(resume.getViewCount() + 1);
        resumeRepository.save(resume);

        return ResponseEntity.ok(resume);
    }
}