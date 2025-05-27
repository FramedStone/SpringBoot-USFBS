package com.usfbs.springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.usfbs.springboot.service.AuthService;   

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired private AuthService authService;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain
    ) throws ServletException, IOException {
        String path = request.getServletPath();

        // Skip token check for login, refresh & "me"
        if (path.equals("/api/auth/login")
          || path.equals("/api/auth/refresh")
          || path.equals("/api/auth/me")) {
            chain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token == null || authService.isAccessTokenRevoked(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                token==null ? "Missing token" : "Token revoked");
            return;
        }

        // …existing verify & set Authentication…
        chain.doFilter(request, response);
    }

    /**
     * Extract JWT from "accessToken" cookie or "Authorization: Bearer …" header.
     */
    private String resolveToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("accessToken".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}