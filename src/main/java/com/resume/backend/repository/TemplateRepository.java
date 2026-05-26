package com.resume.backend.repository;

import com.resume.backend.model.document.Template;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends MongoRepository<Template, String> {
    // Allows searching by specific template ID later
    Optional<Template> findByTemplateId(String templateId);

    // Allows searching by industry domain (e.g., "IT")
    List<Template> findByDomainsContainingIgnoreCase(String domain);
}