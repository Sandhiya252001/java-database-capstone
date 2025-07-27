package com.example.security;

import com.example.repository.AdminRepository;
import com.example.repository.DoctorRepository;
import com.example.repository.PatientRepository;
import com.example.model.Admin;
import com.example.model.Doctor;
import com.example.model.Patient;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenService {

    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // ✅ Generate JWT token with 7-day expiry
    public String generateToken(String identifier) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 days

        return Jwts.builder()
                .setSubject(identifier)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // ✅ Extract identifier (subject) from token
    public String extractIdentifier(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // ✅ Validate token based on user type
    public boolean validateToken(String token, String user) {
        try {
            String identifier = extractIdentifier(token);
            switch (user.toLowerCase()) {
                case "admin":
                    return adminRepository.findByUsername(identifier) != null;
                case "doctor":
                    return doctorRepository.findByEmail(identifier) != null;
                case "patient":
                    return patientRepository.findByEmail(identifier) != null;
                default:
                    return false;
            }
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Retrieve the signing key from secret
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ✅ Optional convenience method: extract user ID if needed
    public Long extractUserId(String token) {
        String identifier = extractIdentifier(token);

        Doctor doctor = doctorRepository.findByEmail(identifier);
        if (doctor != null) return doctor.getId();

        Patient patient = patientRepository.findByEmail(identifier);
        if (patient != null) return patient.getId();

        Admin admin = adminRepository.findByUsername(identifier);
        if (admin != null) return admin.getId();

        return null;
    }

    // ✅ Optional convenience method: extract email
    public String extractEmail(String token) {
        return extractIdentifier(token); // assuming email is used as subject for Doctor/Patient
    }
}
