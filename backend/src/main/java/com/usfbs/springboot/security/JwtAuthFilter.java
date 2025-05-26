package com.usfbs.springboot.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

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
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                               "Missing or invalid Authorization header");
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