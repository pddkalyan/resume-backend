package com.resume.backend.controller;

import com.resume.backend.model.document.Resume;
import com.resume.backend.repository.ResumeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import com.resume.backend.security.JwtUtil;
import java.util.List;
import java.util.Map;       // <-- NEW IMPORT
import java.util.HashMap;   // <-- NEW IMPORT
import java.util.UUID;      // <-- NEW IMPORT

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeRepository resumeRepository;
    private final JwtUtil jwtUtil;

    public ResumeController(ResumeRepository resumeRepository, JwtUtil jwtUtil) {
        this.resumeRepository = resumeRepository;
        this.jwtUtil = jwtUtil;
    }

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

    // --- NEW: SECURED ENDPOINT - Toggle visibility and generate URL code ---
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

            // Security Check: Ensure the user actually owns this resume before changing sharing settings
            if (!resume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not have permission to share this resume"));
            }

            boolean makePublic = payload.getOrDefault("isPublic", false);
            resume.setPublic(makePublic);

            // Generate a clean, 8-character unique share code if missing
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

    // --- UPDATED: PUBLIC ENDPOINT - Fetch via share code, checking visibility ---
    @GetMapping("/public/{shareCode}")
    public ResponseEntity<?> getPublicResume(@PathVariable String shareCode) {
        Optional<Resume> optionalResume = resumeRepository.findByShareCode(shareCode);

        if (optionalResume.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Resume not found or invalid link."));
        }

        Resume resume = optionalResume.get();

        // Security Check: Verify the owner hasn't turned sharing off
        if (!resume.isPublic()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "This resume is no longer public."));
        }

        return ResponseEntity.ok(resume);
    }
}