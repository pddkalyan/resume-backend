package com.resume.backend.controller;

import com.resume.backend.model.GlobalPicklist;
import com.resume.backend.repository.GlobalPicklistRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picklist")
@CrossOrigin(origins = "http://localhost:5173")
public class PicklistController {

    private final GlobalPicklistRepository picklistRepository;

    public PicklistController(GlobalPicklistRepository picklistRepository) {
        this.picklistRepository = picklistRepository;
    }

    // Endpoint 1: Retrieve approved items for a specific dropdown
    @GetMapping("/{category}")
    public ResponseEntity<List<GlobalPicklist>> getApprovedItems(@PathVariable String category) {
        List<GlobalPicklist> items = picklistRepository.findByCategoryAndIsApprovedTrue(category.toUpperCase());
        return ResponseEntity.ok(items);
    }

    // Endpoint 2: User manually types a new item not in the list
    @PostMapping("/add")
    public ResponseEntity<String> addPendingItem(@RequestBody GlobalPicklist newItem) {
        // Prevent duplicates
        if (picklistRepository.existsByCategoryAndValue(newItem.getCategory().toUpperCase(), newItem.getValue())) {
            return ResponseEntity.badRequest().body("Item already exists.");
        }

        newItem.setCategory(newItem.getCategory().toUpperCase());
        newItem.setApproved(false); // Forces admin review
        picklistRepository.save(newItem);

        return ResponseEntity.ok("Item added to pending review.");
    }
}