package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends AuthException {

  public TokenRefreshException(String token, String message) {
    super(String.format("Error al refrescar el token [%s]: %s", token, message));
  }
}