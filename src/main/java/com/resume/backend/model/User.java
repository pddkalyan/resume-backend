package com.resume.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    private boolean saveConsent = false;

    // --- THE FIX: Pro Status Flag ---
    private boolean isPro = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // --- GETTERS AND SETTERS ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isSaveConsent() { return saveConsent; }
    public void setSaveConsent(boolean saveConsent) { this.saveConsent = saveConsent; }

    public boolean isPro() { return isPro; }
    public void setPro(boolean pro) { this.isPro = pro; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}