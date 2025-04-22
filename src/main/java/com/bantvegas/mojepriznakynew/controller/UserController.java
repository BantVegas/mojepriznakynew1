package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body("Neautorizovaný prístup");
        }

        String email = auth.getName();

        return userRepository.findByEmail(email)
                .<ResponseEntity<Object>>map(user -> ResponseEntity.ok().body(UserResponse.of(user)))
                .orElseGet(() -> ResponseEntity.status(404).body("Používateľ neexistuje"));
    }
}
