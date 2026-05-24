package com.resume.backend.repository;

import com.resume.backend.model.document.Resume;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeRepository extends MongoRepository<Resume, String> {

    // Spring Boot automatically translates this exact method name into a MongoDB query!
    List<Resume> findByUserEmail(String userEmail);

}