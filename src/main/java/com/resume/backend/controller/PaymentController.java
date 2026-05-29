package com.resume.backend.controller;

import com.razorpay.Order;
import com.resume.backend.model.User;
import com.resume.backend.repository.UserRepository;
import com.resume.backend.service.PaymentService;
import com.resume.backend.service.UserService; // <-- Added Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*") // Configure this strictly in production
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService; // <-- Added Autowire

    // DTOs for Incoming Requests
    public static class OrderRequest {
        public String planId; // e.g., "6_MONTHS"
        public int discountPercentage;
    }

    public static class VerificationRequest {
        public String razorpay_order_id;
        public String razorpay_payment_id;
        public String razorpay_signature;
        public String planId;
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        try {
            // Anti-Fraud Check: Prevent API manipulation
            if (request.discountPercentage > 80 || request.discountPercentage < 0) {
                return ResponseEntity.badRequest().body("Fraud detected: Invalid discount.");
            }

            Order order = paymentService.createOrder(request.planId, request.discountPercentage);

            Map<String, Object> response = new HashMap<>();
            response.put("id", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerificationRequest request, Authentication authentication) {

        boolean isValid = paymentService.verifySignature(
                request.razorpay_order_id,
                request.razorpay_payment_id,
                request.razorpay_signature
        );

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payment signature.");
        }

        // Apply the upgrade to the user's MongoDB Document
        String userEmail = authentication.getName(); // Extracts email from JWT
        Optional<User> optionalUser = userRepository.findByEmail(userEmail);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            LocalDateTime now = LocalDateTime.now();

            switch (request.planId) {
                case "1_MONTH":
                    user.setPro(true);
                    user.setProValidUntil(user.isProActive() && user.getProValidUntil() != null
                            ? user.getProValidUntil().plusMonths(1)
                            : now.plusMonths(1));
                    break;
                case "6_MONTHS":
                    user.setPro(true);
                    user.setProValidUntil(user.isProActive() && user.getProValidUntil() != null
                            ? user.getProValidUntil().plusMonths(6)
                            : now.plusMonths(6));
                    break;
                case "PAYG_50":
                    user.addAiCredits(50);
                    break;
            }

            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Account successfully upgraded!"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }
}