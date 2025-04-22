package com.emersondev.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
  private String token;
  private String tokenType = "Bearer";
  private String refreshToken;
  private Long id;
  private String username;
  private String email;
  private List<String> roles;

  public JwtResponse(String token, String refreshToken, Long id, String username, String email, List<String> roles) {
    this.token = token;
    this.refreshToken = refreshToken;
    this.id = id;
    this.username = username;
    this.email = email;
    this.roles = roles;
  }

  public JwtResponse(String jwt, String requestRefreshToken, String username, List<String> roles) {
    this.token = jwt;
    this.refreshToken = requestRefreshToken;
    this.username = username;
    this.roles = roles;
  }
}
