package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsuarioNotFoundException extends ResourceNotFoundException {

  public UsuarioNotFoundException(Long id) {
    super("Usuario", "id", id);
  }

  public UsuarioNotFoundException(String username) {
    super("Usuario", "username", username);
  }
}