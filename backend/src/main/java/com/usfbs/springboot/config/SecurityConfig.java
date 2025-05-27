package com.usfbs.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())  
      .csrf(csrf -> csrf.disable())      // disable CSRF for stateless/API
      .sessionManagement(sm -> sm
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        // allow auth endpoints without a token:
        .requestMatchers(
          "/api/auth/login",
          "/api/auth/refresh",
          "/api/auth/me",
          "/api/auth/logout"
        ).permitAll()
        .anyRequest().authenticated()
      );
    return http.build();
  }
}