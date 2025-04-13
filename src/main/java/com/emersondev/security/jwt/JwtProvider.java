package com.emersondev.security.jwt;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {
  private static final Logger logger = LoggerFactory.getLogger(JwtProvider.class);

  @Value("${app.jwtSecret}")
  private String jwtSecret;

  @Value("${app.jwtExpirationMs}")
  private int jwtExpirationMs; // 24 hours

  private Key getSigningKey() {
    byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String generateJwtToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .issuedAt(new Date())
            .expiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(getSigningKey())
            .compact();
  }

  public String getUsernameFromJwtToken(String token) {
    return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
  }


  public boolean validateJwtToken(String authToken) {
    try {
      Jwts.parser()
              .setSigningKey(getSigningKey())
              .build()
              .parseSignedClaims(authToken);
      return true;
    } catch (MalformedJwtException e) {
      logger.error("Invalid JWT token: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      logger.error("JWT token is expired: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      logger.error("JWT token is unsupported: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("JWT claims string is empty: {}", e.getMessage());
    }

    return false;
  }
}
