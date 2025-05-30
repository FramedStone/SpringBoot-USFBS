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
import java.util.List;
import java.util.Map;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

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

        try {
            Map<String, com.auth0.jwt.interfaces.Claim> claims = authService.verifyToken(token);
            String role = claims.get("role").asString();
            String email = claims.get("sub").asString();

            // Map role to Spring Security format
            List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + role)
            );

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        }

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