package com.resume.backend.controller;

import com.resume.backend.model.User;
import com.resume.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            Map<String, Object> response = new HashMap<>();
            response.put("email", user.getEmail());
            response.put("isPro", user.isProActive()); // Uses our smart validity helper method
            response.put("aiCredits", user.getAiCredits());

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body("User not found");
    }
}