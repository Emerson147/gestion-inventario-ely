package com.emersondev.api.controller;

import com.emersondev.api.request.CambiarPasswordRequest;
import com.emersondev.api.request.LoginRequest;
import com.emersondev.api.request.RegistroRequest;
import com.emersondev.api.response.JwtResponse;
import com.emersondev.api.response.MensajeResponse;
import com.emersondev.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/registro")
  public ResponseEntity<JwtResponse> registro(@RequestBody @Valid RegistroRequest registroRequest) {
   return ResponseEntity.ok(authService.registro(registroRequest));
  }

  @PostMapping("/login")
  public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
    return ResponseEntity.ok(authService.login(loginRequest));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<JwtResponse> refreshToken(@RequestBody String refreshToken) {
    return ResponseEntity.ok(authService.refreshToken(refreshToken));
  }

  @PostMapping("/cambiar-password")
  public ResponseEntity<MensajeResponse> cambiarPassword(@RequestBody @Valid CambiarPasswordRequest cambiarPasswordRequest) {
    authService.cambiarPassword(cambiarPasswordRequest);
    return ResponseEntity.ok(new MensajeResponse("Contraseña cambiada correctamente"));
  }

  @PostMapping("/logout")
  public ResponseEntity<MensajeResponse> logout(@RequestHeader("Authorization") String token) {
    authService.logout(token);
    return ResponseEntity.ok(new MensajeResponse("Sesión cerrada correctamente"));
  }


  @GetMapping("/validar-token")
  public ResponseEntity<MensajeResponse> validarToken(@RequestHeader("Authorization") String token) {
    boolean isValid = authService.validarToken(token);
    if (isValid) {
      return ResponseEntity.ok(new MensajeResponse("Token válido"));
    } else {
      return ResponseEntity.status(401).body(new MensajeResponse("Token inválido"));
    }
  }


}
