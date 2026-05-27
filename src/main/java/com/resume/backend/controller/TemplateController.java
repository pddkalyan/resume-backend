package com.resume.backend.controller;

import com.resume.backend.model.document.Template;
import com.resume.backend.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final TemplateRepository templateRepository;

    @Autowired
    public TemplateController(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // 1. Get the entire catalog (for the storefront gallery)
    @GetMapping
    public ResponseEntity<List<Template>> getAllTemplates(
            @RequestParam(required = false) String domain) {

        List<Template> templates;

        // If the user searches by a category (e.g., ?domain=IT), filter it
        if (domain != null && !domain.trim().isEmpty()) {
            templates = templateRepository.findByDomainsContainingIgnoreCase(domain);
        } else {
            templates = templateRepository.findAll();
        }

        return ResponseEntity.ok(templates);
    }

    // 2. Get a single template config by ID (useful when rendering a saved resume)
    @GetMapping("/{templateId}")
    public ResponseEntity<Template> getTemplateById(@PathVariable String templateId) {
        return templateRepository.findByTemplateId(templateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createCustomTemplate(@RequestBody com.resume.backend.dto.TemplateRequest request) {
        try {
            // Instantiate your existing Template domain entity
            com.resume.backend.model.document.Template newTemplate = new com.resume.backend.model.document.Template();

            newTemplate.setTemplateId(request.getTemplateId());
            newTemplate.setName(request.getName());
            newTemplate.setThumbnailUrl(request.getThumbnailUrl());
            newTemplate.setDomains(request.getDomains());
            newTemplate.setBaseComponent(request.getBaseComponent());
            newTemplate.setConfig(request.getConfig());
            newTemplate.setPro(request.isPro());

            // Persist using your existing repository bean instance (e.g., templateRepository.save)
            templateRepository.save(newTemplate);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Template registration successful."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to archive custom layout configuration: " + e.getMessage()));
        }


    }
    // NEW: Public endpoint to fetch a specific template's design rules
    @GetMapping("/public/{templateId}")
    public ResponseEntity<?> getPublicTemplate(@PathVariable String templateId) {
        return templateRepository.findById(templateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}