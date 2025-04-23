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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🔓 Verejne dostupné endpointy
                        .requestMatchers(
                                "/auth/**",
                                "/stripe/webhook",
                                "/h2-console/**"
                        ).permitAll()

                        // ⚙️ Prehliadka (OPTIONS pre CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🧠 AI voľná analýza
                        .requestMatchers(HttpMethod.POST, "/api/freeform")
                        .hasAnyAuthority("ROLE_PACIENT", "ROLE_PACIENT_PREMIUM")

                        // 📷 Diagnóza text + obrázok
                        .requestMatchers(HttpMethod.POST, "/api/diagnose")
                        .hasAnyAuthority("ROLE_PACIENT", "ROLE_PACIENT_PREMIUM")

                        // 📚 História analýz
                        .requestMatchers(HttpMethod.GET, "/api/diagnose/history")
                        .hasAnyAuthority("ROLE_PACIENT", "ROLE_PACIENT_PREMIUM")

                        // 📩 Odosielanie analýzy doktorovi
                        .requestMatchers(HttpMethod.POST, "/api/diagnose/send")
                        .hasAnyAuthority("ROLE_PACIENT", "ROLE_PACIENT_PREMIUM")

                        // 📤 OCR upload obrázka
                        .requestMatchers(HttpMethod.POST, "/api/ocr/upload").permitAll()

                        // 💳 Stripe Checkout
                        .requestMatchers(HttpMethod.POST, "/stripe/create-checkout-session").authenticated()

                        // 👤 Informácie o používateľovi
                        .requestMatchers("/api/me").authenticated()

                        // 🔒 Všetko ostatné zakázané
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://mojepriznaky-frontend5.vercel.app", // produkčný frontend
                "http://localhost:3000" // vývojársky frontend
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
        config.setAllowCredentials(true); // kvôli cookie/tokenom

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
