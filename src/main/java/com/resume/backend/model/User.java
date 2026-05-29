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

    // --- NEW: Time-bound Pro Pass and AI Credits ---
    private LocalDateTime proValidUntil;

    // Starts with 3 free credits for new sign-ups
    private int aiCredits = 3;

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

    public LocalDateTime getProValidUntil() { return proValidUntil; }
    public void setProValidUntil(LocalDateTime proValidUntil) { this.proValidUntil = proValidUntil; }

    public int getAiCredits() { return aiCredits; }
    public void setAiCredits(int aiCredits) { this.aiCredits = aiCredits; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    // --- HELPER METHODS FOR PAYMENT LOGIC ---

    public void addAiCredits(int amount) {
        this.aiCredits += amount;
    }

    public boolean isProActive() {
        // Returns true if manually marked as Pro OR if they have an active time-bound Razorpay pass
        return isPro || (proValidUntil != null && proValidUntil.isAfter(LocalDateTime.now()));
    }
}