package com.emersondev.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNotFoundException extends ResourceNotFoundException {

  public ClienteNotFoundException(Long id) {
    super("No se encontró el cliente con ID: " + id);
  }

  public ClienteNotFoundException(String field, String value) {
    super("No se encontró el cliente con " + field + ": " + value);
  }
}