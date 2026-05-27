package com.resume.backend.dto;

import java.util.List;
import java.util.Map;

public class TemplateRequest {
    private String templateId;
    private String name;
    private String thumbnailUrl;
    private List<String> domains;
    private String baseComponent;
    private Map<String, Object> config;
    private boolean pro;

    // Getters and Setters
    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public List<String> getDomains() { return domains; }
    public void setDomains(List<String> domains) { this.domains = domains; }

    public String getBaseComponent() { return baseComponent; }
    public void setBaseComponent(String baseComponent) { this.baseComponent = baseComponent; }

    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }

    public boolean isPro() { return pro; }
    public void setPro(boolean pro) { this.pro = pro; }
}