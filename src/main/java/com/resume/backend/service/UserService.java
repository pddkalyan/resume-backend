package com.resume.backend.service;

import com.resume.backend.model.User;
import com.resume.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Call this method before executing any AI Feature.
     * Returns TRUE if they are allowed to proceed. Returns FALSE if they are out of credits.
     */
    public boolean consumeAiCredit(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            // Rule 1: PRO Users have unlimited access. Do not deduct credits.
            if (user.isProActive()) {
                return true;
            }

            // Rule 2: Free users must have a positive credit balance.
            if (user.getAiCredits() > 0) {
                user.setAiCredits(user.getAiCredits() - 1);
                userRepository.save(user);
                return true;
            }
        }

        // If they reach here, they are out of credits or the user doesn't exist.
        return false;
    }
}