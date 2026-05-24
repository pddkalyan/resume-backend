package com.resume.backend.repository;

import com.resume.backend.model.GlobalPicklist;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

// 1. Changed JpaRepository to MongoRepository
// 2. Changed ID type from Long to String for MongoDB ObjectIds
public interface GlobalPicklistRepository extends MongoRepository<GlobalPicklist, String> {

    // Spring Data MongoDB converts these method names into BSON queries automatically!
    List<GlobalPicklist> findByCategoryAndIsApprovedTrue(String category);

    // Checks if an item already exists so we don't save duplicates
    boolean existsByCategoryAndValue(String category, String value);
}