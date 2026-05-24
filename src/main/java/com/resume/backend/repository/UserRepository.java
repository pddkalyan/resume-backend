package com.resume.backend.repository;

import com.resume.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// 1. Changed JpaRepository to MongoRepository
// 2. Changed ID type from UUID to String (standard for MongoDB ObjectIds)
public interface UserRepository extends MongoRepository<User, String> {

    // Spring Data MongoDB magically handles the BSON queries for these!
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}