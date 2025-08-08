package com.aos.fitness_app.auth.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey = "";

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    // Getters and setters
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setJwtExpiration(long jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }

    // Updated buildToken method for JJWT 0.12.x
    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .claims(extraClaims)  // Changed from setClaims()
                .subject(userDetails.getUsername())  // Changed from setSubject()
                .issuedAt(new Date(System.currentTimeMillis()))  // Changed from setIssuedAt()
                .expiration(new Date(System.currentTimeMillis() + expiration))  // Changed from setExpiration()
                .signWith(getSignInKey())  // Simplified - no need for SignatureAlgorithm
                .compact();
    }

    // Updated extractAllClaims method for JJWT 0.12.x
    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()  // Changed from parserBuilder()
                .verifyWith(getSignInKey())  // Changed from setSigningKey()
                .build()
                .parseSignedClaims(token)  // Changed from parseClaimsJws()
                .getPayload();  // Changed from getBody()
    }

    // Updated getSignInKey to return SecretKey instead of Key
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // These methods remain mostly the same
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}