package com.resume.backend.config;

import com.resume.backend.model.document.Template;
import com.resume.backend.repository.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder {

    private final TemplateRepository templateRepository;

    @Autowired
    public DataSeeder(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostConstruct
    public void seedTemplates() {
        // Only seed if the database is currently empty
        if (templateRepository.count() == 0) {
            System.out.println("🌱 Database is empty. Seeding initial Pro Templates...");

            // --- Template 1: Modern IT (Blue) ---
            Map<String, Object> modernConfig = new HashMap<>();
            modernConfig.put("primaryColor", "#2563eb"); // Blue
            modernConfig.put("fontFamily", "Inter, sans-serif");
            modernConfig.put("layoutStyle", "two-column");

            Template modern = new Template(
                    "tpl_modern_001",
                    "Silicon Valley Modern",
                    "https://placehold.co/400x600/2563eb/white?text=Modern+Layout",
                    false, // Free tier
                    Arrays.asList("IT", "Software Engineering", "Product Management"),
                    "ModernLayout",
                    modernConfig
            );

            // --- Template 2: Executive Classic (Dark Red) ---
            Map<String, Object> classicConfig = new HashMap<>();
            classicConfig.put("primaryColor", "#7f1d1d"); // Dark Red
            classicConfig.put("fontFamily", "Merriweather, serif");
            classicConfig.put("layoutStyle", "single-column-centered");

            Template classic = new Template(
                    "tpl_classic_001",
                    "Executive Classic",
                    "https://placehold.co/400x600/7f1d1d/white?text=Classic+Layout",
                    true, // Pro tier
                    Arrays.asList("Finance", "Law", "Management"),
                    "ClassicLayout",
                    classicConfig
            );

            // --- Template 3: Creative Minimalist (Purple) ---
            Map<String, Object> creativeConfig = new HashMap<>();
            creativeConfig.put("primaryColor", "#8b5cf6"); // Purple
            creativeConfig.put("fontFamily", "Poppins, sans-serif");
            creativeConfig.put("layoutStyle", "sidebar-left");

            Template creative = new Template(
                    "tpl_creative_001",
                    "Creative Studio",
                    "https://placehold.co/400x600/8b5cf6/white?text=Creative+Layout",
                    true, // Pro tier
                    Arrays.asList("Design", "Marketing", "UI/UX"),
                    "CreativeLayout",
                    creativeConfig
            );

            // --- Template 4: Healthcare Professional (Teal) ---
            Map<String, Object> healthConfig = new HashMap<>();
            healthConfig.put("primaryColor", "#0f766e"); // Teal
            healthConfig.put("fontFamily", "Roboto, sans-serif");
            healthConfig.put("layoutStyle", "two-column");

            Template health = new Template(
                    "tpl_health_001",
                    "Clinical Professional",
                    "https://placehold.co/400x600/0f766e/white?text=Healthcare+Layout",
                    false, // Free tier
                    Arrays.asList("Healthcare", "Pharmacy", "Nursing"),
                    "ModernLayout", // Reuses the Modern component but with Teal config!
                    healthConfig
            );

            // Save them all to MongoDB
            templateRepository.saveAll(Arrays.asList(modern, classic, creative, health));
            System.out.println("✅ Initial Templates successfully seeded!");
        } else {
            System.out.println("⏭️ Templates already exist in database. Skipping seed.");
        }
    }
}