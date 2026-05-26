package com.resume.backend.controller;

import com.resume.backend.model.document.Template;
import com.resume.backend.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}