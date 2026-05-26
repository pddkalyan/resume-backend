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

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    // 1. THESE ARE THE CLASS FIELDS
    private final ResumeRepository resumeRepository;
    private final JwtUtil jwtUtil;

    // 2. THIS IS THE CONSTRUCTOR
    public ResumeController(ResumeRepository resumeRepository, JwtUtil jwtUtil) {
        this.resumeRepository = resumeRepository;
        this.jwtUtil = jwtUtil;
    }

    // --- CREATE OR UPDATE A RESUME ---
    @PostMapping
    public ResponseEntity<Resume> saveResume(@RequestBody Resume resume, Authentication authentication) {
        // 1. Spring Security automatically reads the JWT and gives us the logged-in user's email
        String userEmail = authentication.getName();

        // 2. We force the resume to belong to this email (prevents users from hacking other accounts)
        resume.setUserEmail(userEmail);

        // 3. Save to MongoDB! (If the resume has an ID, Mongo updates it. If no ID, Mongo creates a new one).
        Resume savedResume = resumeRepository.save(resume);

        return ResponseEntity.ok(savedResume);
    }

    // --- GET ALL RESUMES FOR THE LOGGED-IN USER ---
    @GetMapping
    public ResponseEntity<List<Resume>> getMyResumes(Authentication authentication) {
        String userEmail = authentication.getName();

        // Fetch only the documents from Mongo that match this user's email
        List<Resume> myResumes = resumeRepository.findByUserEmail(userEmail);

        return ResponseEntity.ok(myResumes);
    }

    // --- NEW: GET A SPECIFIC RESUME BY ID ---
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
            // Ensure the user owns this resume
            if (!resume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
            }

            return ResponseEntity.ok(resume);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching resume");
        }
    }

    // --- UPDATE AN EXISTING RESUME ---
    @PutMapping("/{id}")
    public ResponseEntity<?> updateResume(@PathVariable String id, @RequestBody Resume updatedResume, @RequestHeader("Authorization") String token) {
        try {
            // 1. Extract the email from the token (just like your other endpoints)
            String jwt = token.substring(7);
            String userEmail = jwtUtil.extractEmail(jwt);

            // 2. Security Check: Ensure the resume exists AND belongs to this user
            Optional<Resume> existingResumeOpt = resumeRepository.findById(id);

            if (existingResumeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resume not found");
            }

            Resume existingResume = existingResumeOpt.get();
            if (!existingResume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to edit this resume");
            }

            // 3. Update the fields and save
            updatedResume.setId(id); // Force the ID to match the URL so MongoDB knows to overwrite
            updatedResume.setUserEmail(userEmail); // Maintain the ownership link

            Resume savedResume = resumeRepository.save(updatedResume);

            return ResponseEntity.ok(savedResume);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating resume: " + e.getMessage());
        }
    }

    // --- DELETE A SPECIFIC RESUME ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteResume(@PathVariable String id, @RequestHeader("Authorization") String token) {
        try {
            // 1. Extract the authenticated user's email from the JWT
            String jwt = token.substring(7);
            String userEmail = jwtUtil.extractEmail(jwt);

            // 2. Resource Verification: Ensure the document exists
            Optional<Resume> resumeOpt = resumeRepository.findById(id);
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resume not found");
            }

            Resume resume = resumeOpt.get();

            // 3. Authorization Check: Guard against multi-tenant cross-account deletion
            if (!resume.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this resume");
            }

            // 4. Purge from MongoDB
            resumeRepository.deleteById(id);

            return ResponseEntity.ok("Resume deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting resume: " + e.getMessage());
        }
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<?> getPublicResume(@PathVariable String id) {
        // Fetch the resume directly by ID without checking the JWT user ID
        return resumeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}