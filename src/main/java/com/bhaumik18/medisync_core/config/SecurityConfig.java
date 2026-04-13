package com.bhaumik18.medisync_core.config;

import com.bhaumik18.medisync_core.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Explicitly wire the CORS configuration to intercept OPTIONS requests early
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Disable CSRF (safe since we use stateless JWTs)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Allow GKE Health Check, lock down everything else
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/providers/**", "/api/v1/schedules/**", "/api/chaos/**", "/api/v1/core/health", "/api/v1/core/health").permitAll()
                .anyRequest().authenticated()
            )
            
            // 4. Enforce stateless sessions
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 5. Add JWT authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
      
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173", 
                "https://medisync-frontend-vert.vercel.app"
        ));
        
        // Explicitly allowing OPTIONS for preflight, plus standard REST methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Allow necessary headers (Authorization is crucial for JWT)
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow credentials for secure cross-origin requests
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}