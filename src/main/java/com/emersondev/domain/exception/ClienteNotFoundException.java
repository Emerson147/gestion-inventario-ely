package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNotFoundException extends ResourceNotFoundException {

  public ClienteNotFoundException(Long id) {
    super("Cliente", "id", id);
  }

  public ClienteNotFoundException(String tipo, String valor) {
    super("Cliente", tipo, valor);
  }
}