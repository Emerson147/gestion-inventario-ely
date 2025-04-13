package com.emersondev.security.jwt;

import org.springframework.stereotype.Component;

@Component
public class JwtTokenValidator {
  private final JwtProvider jwtProvider;

  public JwtTokenValidator(JwtProvider jwtProvider) {
    this.jwtProvider = jwtProvider;
  }

  public boolean validate(String token) {
    return jwtProvider.validateJwtToken(token);
  }

  public String getUsernameFromToken(String token) {
    return jwtProvider.getUsernameFromJwtToken(token);
  }

}
