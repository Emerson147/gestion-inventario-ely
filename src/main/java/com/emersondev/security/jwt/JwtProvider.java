package com.emersondev.security.jwt;


import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtProvider {

  @Value("${app.jwt.secret}")
  private String jwtSecret;

  @Value("${app.jwt.expiration}")
  private int jwtExpirationMs;

  @Value("${app.jwt.refresh-expiration:604800000}") // 7 días por defecto
  private int refreshTokenExpirationMs;

  /**
   * Genera un token JWT para un usuario autenticado
   */
  public String generateJwtToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    // Extrae los roles del usuario
    String roles = userPrincipal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(getSigningKey())
            .compact();
  }

  /**
   * Genera un token de refresco para extender la sesión
   */
  public String generateRefreshToken(Authentication authentication) {
    UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

    return Jwts.builder()
            .subject(userPrincipal.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
            .signWith(getSigningKey())
            .compact();
  }

  /**
   * Obtiene el nombre de usuario del token JWT
   */
  public String getUsernameFromToken(String token) {
    return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
  }

  /**
   * Obtiene el nombre de usuario del token de refresco
   */
  public String getUsernameFromRefreshToken(String token) {
    return Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
  }

  /**
   * Valida un token JWT
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser()
              .setSigningKey(getSigningKey())
              .build()
              .parseSignedClaims(token);
      return true;
    } catch (SignatureException e) {
      log.error("Firma JWT inválida: {}", e.getMessage());
    } catch (MalformedJwtException e) {
      log.error("Token JWT inválido: {}", e.getMessage());
    } catch (ExpiredJwtException e) {
      log.error("Token JWT expirado: {}", e.getMessage());
    } catch (UnsupportedJwtException e) {
      log.error("Token JWT no soportado: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      log.error("La cadena claims JWT está vacía: {}", e.getMessage());
    }
    return false;
  }

  /**
   * Valida un token de refresco
   */
  public boolean validateRefreshToken(String token) {
    return validateToken(token);
  }

  /**
   * Obtiene las autoridades (roles) del token JWT
   */
  public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
    Claims claims = Jwts.parser()
            .setSigningKey(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();

    String roles = claims.get("roles", String.class);
    return Arrays.stream(roles.split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
  }

  /**
   * Crea un objeto Authentication a partir de un nombre de usuario
   */
  public Authentication getAuthenticationFromUsername(String username) {
    // Aquí estamos creando un objeto Authentication simplificado basado solo en el username
    // En una implementación completa, deberías cargar el usuario completo con UserDetailsService
    List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
    );

    User principal = new User(username, "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  /**
   * Obtiene la clave para firmar tokens JWT
   */
  private Key getSigningKey() {
    byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
