package com.emersondev.service.interfaces;

import com.emersondev.api.request.CambiarPasswordRequest;
import com.emersondev.api.request.LoginRequest;
import com.emersondev.api.request.RegistroRequest;
import com.emersondev.api.response.JwtResponse;

public interface AuthService {

  JwtResponse login(LoginRequest loginRequest);

  JwtResponse registro(RegistroRequest registroRequest);

  JwtResponse refreshToken(String refreshToken);

  void cambiarPassword(CambiarPasswordRequest cambiarPasswordRequest);

  void logout(String token);

  boolean validarToken(String token);

}
