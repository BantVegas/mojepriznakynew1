package com.bantvegas.mojepriznakynew.config;

import com.bantvegas.mojepriznakynew.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/stripe/webhook", // verejný pre Stripe
                                "/h2-console/**"
                        ).permitAll()

                        // CORS & OPTIONS requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // AI a Stripe: len pre autentifikovaných s rolou
                        .requestMatchers(HttpMethod.POST, "/api/freeform", "/stripe/create-checkout-session")
                        .hasAnyAuthority("ROLE_PACIENT", "ROLE_PACIENT_PREMIUM")

                        .requestMatchers(HttpMethod.POST, "/api/ocr/upload")
                        .hasAnyAuthority("ROLE_PACIENT", "ROLE_PACIENT_PREMIUM")

                        .requestMatchers("/api/me").authenticated()

                        .anyRequest().denyAll() // všetko ostatné blokovať
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
