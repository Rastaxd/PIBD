package org.dorixon.springlab4.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.access-secret-key}")
    private String accessSecret;

    @Value("${jwt.access-token-validity-in-min}")
    private long accessValidity;

    @Value("${jwt.refresh-secret-key}")
    private String refreshSecret;

    @Value("${jwt.refresh-token-validity-in-min}")
    private long refreshValidity;

    public String extractUsernameFromAccess(String token) {
        return extractClaim(token, Claims::getSubject, getAccessKey());
    }

    public String extractUsernameFromRefresh(String token) {
        return extractClaim(token, Claims::getSubject, getRefreshKey());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver, Key key) {
        final Claims claims = extractAllClaims(token, key);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, accessValidity * 60 * 1000, getAccessKey());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, refreshValidity * 60 * 1000, getRefreshKey());
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, long expirationMs, Key key) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameFromAccess(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, getAccessKey());
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsernameFromRefresh(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token, getRefreshKey());
    }

    private boolean isTokenExpired(String token, Key key) {
        return extractExpiration(token, key).before(new Date());
    }

    private Date extractExpiration(String token, Key key) {
        return extractClaim(token, Claims::getExpiration, key);
    }

    private Claims extractAllClaims(String token, Key key) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getAccessKey() {
        return Keys.hmacShaKeyFor(accessSecret.getBytes());
    }

    private Key getRefreshKey() {
        return Keys.hmacShaKeyFor(refreshSecret.getBytes());
    }
}
