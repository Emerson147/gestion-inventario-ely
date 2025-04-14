package com.emersondev.api.controller;

import com.emersondev.api.request.LoginRequest;
import com.emersondev.api.request.RegistroRequest;
import com.emersondev.api.response.JwtResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/registro")
  public ResponseEntity<JwtResponse> registro(@RequestBody @Valid RegistroRequest registroRequest) {
    JwtResponse jwtResponse = authService.registro(registroRequest);
    return ResponseEntity.ok(jwtResponse);
  }

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    JwtResponse jwtResponse = authService.login(loginRequest);
    return ResponseEntity.ok(jwtResponse);
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<JwtResponse> refreshToken(@RequestBody String refreshToken) {
    JwtResponse jwtResponse = authService.refreshToken(refreshToken);
    return ResponseEntity.ok(jwtResponse);
  }

  @PostMapping("/logout")
  public ResponseEntity<MensajeResponse> logout() {
    authService.logout();
    return ResponseEntity.ok(new MensajeResponse("Sesión cerrada correctamente"));
  }

  //Faltaria agregar para llamar usuarios

}
