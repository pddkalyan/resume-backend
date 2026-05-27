package com.resume.backend.model.document;

import com.resume.backend.model.Project;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "resumes")
public class Resume {

    @Id
    private String id; // MongoDB automatically generates this String ID

    private String userEmail; // The crucial bridge back to your PostgreSQL User!
    private String title; // e.g., "Java Backend Developer Resume"

    private PersonalInfo personalInfo = new PersonalInfo();
    private List<Experience> experience = new ArrayList<>();
    private List<Education> education = new ArrayList<>();
    private List<String> skills = new ArrayList<>();
    private List<Project> projects;

    // --- Existing: Template Selection ---
    private String selectedTemplate = "modern"; // Default value

    // --- NEW: Public Sharing Fields ---
    private boolean isPublic = false;
    private String shareCode;

    // --- NEW GETTERS & SETTERS FOR SHARING ---
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    // --- EXISTING GETTERS & SETTERS ---
    public String getSelectedTemplate() {
        return selectedTemplate;
    }

    public void setSelectedTemplate(String selectedTemplate) {
        this.selectedTemplate = selectedTemplate;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public PersonalInfo getPersonalInfo() { return personalInfo; }
    public void setPersonalInfo(PersonalInfo personalInfo) { this.personalInfo = personalInfo; }
    public List<Experience> getExperience() { return experience; }
    public void setExperience(List<Experience> experience) { this.experience = experience; }
    public List<Education> getEducation() { return education; }
    public void setEducation(List<Education> education) { this.education = education; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    // --- NESTED CLASSES FOR CLEAN JSON STRUCTURE ---
    public static class PersonalInfo {
        private String fullName;
        private String phone;
        private String linkedInUrl;
        private String githubUrl;
        private String email;

        public String getGithubUrl() { return githubUrl; }
        public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getLinkedInUrl() { return linkedInUrl; }
        public void setLinkedInUrl(String linkedInUrl) { this.linkedInUrl = linkedInUrl; }
    }

    public static class Experience {
        private String company;
        private String role;
        private String duration;
        private String description;

        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getDuration() { return duration; }
        public void setDuration(String duration) { this.duration = duration; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class Education {
        private String institution;
        private String degree;
        private String graduationYear;

        public String getInstitution() { return institution; }
        public void setInstitution(String institution) { this.institution = institution; }
        public String getDegree() { return degree; }
        public void setDegree(String degree) { this.degree = degree; }
        public String getGraduationYear() { return graduationYear; }
        public void setGraduationYear(String graduationYear) { this.graduationYear = graduationYear; }
    }
}