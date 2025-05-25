package com.usfbs.springboot.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.usfbs.springboot.contracts.Management;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class AuthService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.accessExpiry}")
    private long accessExpiry; 

    @Value("${jwt.refreshExpiry}")
    private long refreshExpiry; 

    public boolean validateEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9._%+-]+@(student\\.)?mmu\\.edu\\.my$");
    }

    public String generateAccessToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpiry * 1000))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public String generateRefreshToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpiry * 1000))
                .sign(Algorithm.HMAC256(jwtSecret));
    }

    public String generateBackendJwt(String email, String role) {
        return JWT.create()
            .withSubject(email)
            .withClaim("role", role)
            .withIssuedAt(new Date())
            .withExpiresAt(new Date(System.currentTimeMillis() + accessExpiry * 1000))
            .sign(Algorithm.HMAC256(jwtSecret));
    }

    public Map<String, Claim> verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(jwtSecret)).build().verify(token).getClaims();
    }

    public boolean verifyWeb3AuthJwt(String web3AuthJwt, String web3AuthPublicKey) {
        try {
            Algorithm algorithm = Algorithm.RSA256(null, web3AuthPublicKey); // Use Web3Auth's public key
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(web3AuthJwt);
            // Extract claims as needed
            return true;
        } catch (Exception e) {
            // Log error
            return false;
        }
    }

    public String getUserRole(String userAddress, Management managementContract) throws Exception {
        if (managementContract.isAdmin(userAddress).send()) {
            return "Admin";
        } else if (managementContract.isModerator(userAddress).send()) {
            return "Moderator";
        } else if (managementContract.isUser(userAddress).send()) { // Only assign "User" if present in contract
            return "User";
        }
        return "Unregistered";
    }
}