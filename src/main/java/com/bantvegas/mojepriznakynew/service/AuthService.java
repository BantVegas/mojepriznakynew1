package com.bantvegas.mojepriznakynew.service;

import com.bantvegas.mojepriznakynew.model.User;
import com.bantvegas.mojepriznakynew.repository.UserRepository;
import com.bantvegas.mojepriznakynew.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Používateľ neexistuje"));

        String role = "ROLE_" + user.getSubscriptionTier().name();
        return jwtService.generateToken(user.getEmail(), role);
    }

    public String register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setAiUsageCount(0);
        userRepository.save(user);

        String role = "ROLE_" + user.getSubscriptionTier().name();
        return jwtService.generateToken(user.getEmail(), role);
    }
}
