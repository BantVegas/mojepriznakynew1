package com.bantvegas.mojepriznakynew.controller;

import com.bantvegas.mojepriznakynew.dto.LoginRequest;
import com.bantvegas.mojepriznakynew.dto.UserRegistrationDto;
import com.bantvegas.mojepriznakynew.enums.SubscriptionTier;
import com.bantvegas.mojepriznakynew.model.User;
import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.response.AuthResponse;
import com.bantvegas.mojepriznakynew.response.UserResponse;
import com.bantvegas.mojepriznakynew.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistrationDto req) {
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Používateľ už existuje"));
        }

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail())
                .password(req.getPassword())
                .subscriptionTier(Optional.ofNullable(req.getSubscriptionTier()).orElse(SubscriptionTier.PACIENT))
                .referringDoctorCode(req.getReferralCode())
                .enabled(true)
                .aiUsageCount(0)
                .build();

        String token = authService.register(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(UserResponse.of(user))
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Nesprávny email alebo heslo"));
        }

        User user = userOpt.get();

        String token = authService.login(req.getEmail(), req.getPassword());

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .user(UserResponse.of(user))
                .build());
    }
}
