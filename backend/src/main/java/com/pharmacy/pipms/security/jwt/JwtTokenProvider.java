package com.pharmacy.pipms.security.jwt;

import com.pharmacy.pipms.common.constants.RoleName;
import com.pharmacy.pipms.security.userdetails.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal principal) {
        long expirationMs = resolveExpiration(principal.getUser().getRoles().stream()
                .map(r -> r.getName()).collect(java.util.stream.Collectors.toSet()));

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .id(UUID.randomUUID().toString()) // jti — used for blacklisting on logout
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("roles", principal.getAuthorities().stream()
                        .map(Object::toString).toList())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private long resolveExpiration(Set<RoleName> roles) {
        if (roles.contains(RoleName.ROLE_ADMIN)) {
            return jwtProperties.getAccessToken().getAdmin().getExpirationMs();
        }
        if (roles.contains(RoleName.ROLE_PATIENT) || roles.contains(RoleName.ROLE_DOCTOR)) {
            return jwtProperties.getAccessToken().getPatientDoctor().getExpirationMs();
        }
        // Pharmacist, Technician, Procurement, Auditor -> staff duration
        return jwtProperties.getAccessToken().getStaff().getExpirationMs();
    }

    public String generateOpaqueRefreshToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getJti(String token) {
        return parseClaims(token).getId();
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }
}