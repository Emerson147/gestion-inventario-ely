package com.emersondev.domain.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VentaNotFoundException extends ResourceNotFoundException {

  public VentaNotFoundException(Long id) {
    super("No se encontró la venta con ID: " + id);
  }

  public VentaNotFoundException(String field, String value) {
    super("No se encontró la venta con " + field + ": " + value);
  }

}
