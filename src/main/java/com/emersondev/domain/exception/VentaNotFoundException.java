package com.emersondev.domain.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VentaNotFoundException extends ResourceNotFoundException {

  public VentaNotFoundException(String message) {
    super(message);
  }

  public VentaNotFoundException(Long id) {
    super("Venta", "id", id);
  }

}
