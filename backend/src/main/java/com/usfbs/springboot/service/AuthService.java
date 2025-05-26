package com.usfbs.springboot.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Claim;
import com.usfbs.springboot.contracts.Management;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;

import java.util.*;

@Service
public class AuthService {
    private final Management managementContract;
    private final Credentials credentialAdmin;
    private final Credentials credentialModerator;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.accessExpiry}")
    private int accessExpiry; // in seconds

    @Value("${jwt.refreshExpiry}")
    private int refreshExpiry; // in seconds

    @Value("${quorum.admin}")
    private String adminAddresses; // Comma-separated

    @Value("${quorum.moderator}")
    private String moderatorAddresses; // Comma-separated

    @Autowired
    public AuthService(
        Management managementContract,
        Credentials credentialAdmin,       
        Credentials credentialModerator    
    ) {
        this.managementContract = managementContract;
        this.credentialAdmin   = credentialAdmin;
        this.credentialModerator = credentialModerator;
    }

    /** Determine role by comparing addresses derived from private keys or on-chain check */
    public String getUserRole(String userAddress) {
        String normalized = userAddress.toLowerCase();

        String adminAddr = credentialAdmin.getAddress().toLowerCase();
        if (normalized.equals(adminAddr)) {
            return "Admin";
        }

        String modAddr = credentialModerator.getAddress().toLowerCase();
        if (normalized.equals(modAddr)) {
            return "Moderator";
        }

        try {
            Boolean isRegistered = managementContract.getUser(userAddress).send();
            if (Boolean.TRUE.equals(isRegistered)) {
                return "User";
            }
            // auto-register new user
            managementContract.addUser(userAddress).send();
            return "User";
        } catch (Exception e) {
            throw new RuntimeException("Failed to verify/register user on-chain", e);
        }
    }

    /**
     * Returns permissions based on role.
     */
    public List<String> getPermissions(String role) {
        switch (role) {
            case "Admin":
                return Arrays.asList(
                    "createBooking", "rejectBooking", "viewBookingStatus", "viewAllBookingsHistory",
                    "banUser", "unbanUser", "crudAnnouncement", "crudSportFacility"
                );
            case "Moderator":
                return Collections.singletonList("subscribeSmartContractEvents");
            case "User":
                return Arrays.asList(
                    "createBooking", "cancelBooking", "viewBookingStatus", "viewOwnBookingsHistory",
                    "submitFeedback"
                );
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Generates JWT token with email, role, blockchain address, and permissions.
     */
    public String generateAccessToken(String email, String role, String userAddress) {
        List<String> permissions = getPermissions(role);
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("address", userAddress)
                .withClaim("permissions", permissions)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + accessExpiry * 1000L))
                .sign(algorithm);
    }

    public String generateRefreshToken(String email, String role, String userAddress) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withSubject(email)
                .withClaim("role", role)
                .withClaim("address", userAddress)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshExpiry * 1000L))
                .sign(algorithm);
    }

    public Map<String, Claim> verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaims();
    }

    public int getAccessExpiry() {
        return accessExpiry;
    }

    public int getRefreshExpiry() {
        return refreshExpiry;
    }
}