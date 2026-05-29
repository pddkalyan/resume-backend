package com.resume.backend.controller;

import com.resume.backend.model.User;
import com.resume.backend.repository.UserRepository;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    @Autowired
    private UserRepository userRepository;

    // A separate, highly secure secret just for webhooks (set this in application.yaml)
    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            // 1. Cryptographically verify the webhook is actually from Razorpay
            boolean isSignatureValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);

            if (!isSignatureValid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Signature");
            }

            // 2. Parse the JSON payload
            JSONObject jsonPayload = new JSONObject(payload);
            String event = jsonPayload.getString("event");

            // 3. Handle a successful payment event
            if ("order.paid".equals(event)) {
                JSONObject paymentEntity = jsonPayload.getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String customerEmail = paymentEntity.getString("email");
                String planId = paymentEntity.getJSONObject("notes").getString("planId");

                // Find user and upgrade them based on what they bought
                Optional<User> optionalUser = userRepository.findByEmail(customerEmail);
                if (optionalUser.isPresent()) {
                    User user = optionalUser.get();

                    if ("PAYG_50".equals(planId)) {
                        user.setAiCredits(user.getAiCredits() + 50);
                    } else if ("1_MONTH".equals(planId) || "6_MONTHS".equals(planId)) {
                        user.setPro(true);
                        // Optional: Set expiration dates based on the plan
                    }

                    userRepository.save(user);
                    System.out.println("Webhook: Successfully upgraded " + customerEmail);
                }
            }

            // Always return 200 OK so Razorpay knows we received it
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            System.err.println("Webhook Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }
}