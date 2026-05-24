package com.resume.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "global_picklist")
// This is the MongoDB equivalent of your JPA UniqueConstraint
@CompoundIndex(def = "{'category': 1, 'value': 1}", unique = true)
public class GlobalPicklist {

    // Native Spring Data ID
    @Id
    private String id; // Changed from Long to String

    private String category; // e.g., 'UNIVERSITY', 'SKILL', 'COMPANY'

    private String value; // e.g., 'IIT Madras', 'Spring Boot'

    // Keeps the frontend clean. If a user types a typo, it stays false until you fix/approve it.
    private boolean isApproved = false;

    // --- GETTERS AND SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { this.isApproved = approved; }
}