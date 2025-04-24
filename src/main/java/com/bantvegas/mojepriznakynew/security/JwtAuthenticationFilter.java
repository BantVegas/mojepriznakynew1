package com.bantvegas.mojepriznakynew.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        System.out.println("🛡️ JWT Filter aktivovaný pre cestu: " + request.getRequestURI());
        System.out.println("➡️ Authorization hlavička: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("⛔ Žiadny alebo zlý Bearer token — púšťam ďalej");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7);
            final String email = jwtService.extractUsername(token);
            final List<String> roles = jwtService.extractAuthorities(token);

            System.out.println("📧 Používateľ z tokenu: " + email);
            System.out.println("🔑 Autority z tokenu: " + roles);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ SecurityContextHolder nastavený pre: " + email);
            } else {
                System.out.println("⚠️ Už existuje autentifikácia, preskakujem");
            }
        } catch (Exception e) {
            System.out.println("❌ Výnimka pri spracovaní tokenu:");
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
