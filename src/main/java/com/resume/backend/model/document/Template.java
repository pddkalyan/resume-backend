package com.resume.backend.model.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Document(collection = "templates")
public class Template {

    @Id
    private String id;
    private String templateId; // e.g., "tpl_modern_001"
    private String name;
    private String thumbnailUrl;
    private boolean isPro;
    private List<String> domains; // e.g., ["IT", "Healthcare"]
    private String baseComponent; // e.g., "ModernLayout"

    // We use a Map because different templates might have different config keys
    // e.g., "primaryColor": "#2563eb", "fontFamily": "Inter"
    private Map<String, Object> config;

    // Constructors
    public Template() {}

    public Template(String templateId, String name, String thumbnailUrl, boolean isPro, List<String> domains, String baseComponent, Map<String, Object> config) {
        this.templateId = templateId;
        this.name = name;
        this.thumbnailUrl = thumbnailUrl;
        this.isPro = isPro;
        this.domains = domains;
        this.baseComponent = baseComponent;
        this.config = config;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public boolean isPro() { return isPro; }
    public void setPro(boolean isPro) { this.isPro = isPro; }

    public List<String> getDomains() { return domains; }
    public void setDomains(List<String> domains) { this.domains = domains; }

    public String getBaseComponent() { return baseComponent; }
    public void setBaseComponent(String baseComponent) { this.baseComponent = baseComponent; }

    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
}